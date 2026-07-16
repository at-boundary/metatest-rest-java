# Antigen

> **Research project — interfaces and configuration formats are unstable.**

Antigen is a test-generation harness for HTTP APIs, reinforced by property-based fault simulation.
It generates a test suite from an API specification, then evaluates that suite by mutating HTTP
responses to violate declared semantic properties and checking whether the suite's assertions detect
the mutations. Escaped mutations are fed back as a reinforcement signal, and generation iterates until
the suite detects a target fraction of them. The fault simulator and the generation loop share one
engine; the simulator is the objective function that the generator optimizes against.

The declared properties — **invariants** — are semantic constraints on responses (`price > 0`,
`status in [PENDING, FILLED]`, `status == FILLED ⇒ filled_at != null`). They play the role of the
oracle: the generator never observes them directly, only the aggregate count of properties its suite
failed to guard.

---

## Method

### Invariants as the oracle

An invariant is a property that must hold on the responses of a given endpoint. Invariants are
authored as committed YAML, independently of the tests, and Antigen loads every invariant that matches
an endpoint the suite exercises. They are the specification of *what to verify*; the generated tests
are one realization of *how to verify it*.

### Property-based fault simulation

Fault simulation is the evaluation procedure. Classical property-based testing generates inputs and
checks that a property holds; Antigen inverts this. It holds the recorded request/response fixtures
fixed and instead **negates** each declared property to produce a concrete counterexample — a mutated
response body that violates the invariant — then replays the affected test against that body:

```
for each test:
    run once, recording every request/response pair            (baseline)
    for each recorded response, for each invariant on its endpoint:
        derive a response that violates the invariant           (negation)
        replay the test against the mutated response
            test now fails → caught   (the suite's assertions detect the violation)
            test still passes → escaped (an assertion gap)
```

This is mutation analysis applied to response data rather than program source, with the mutation
operators derived systematically from the invariants: `> 0` yields the counterexamples `0` and `-1`;
`in [A, B]` yields a value outside the set; `is_not_null` yields `null`. Mutations are served from the
cached baseline, so no network calls occur during replay and server state is never altered. The result
is a detection rate reported per test, per endpoint, and per invariant.

### The generation loop

Generation is driven by an LLM (the `claude` CLI) and closed over by the simulator:

```
for attempt in 1..max_retries:
    generate tests from the specification
    build        → on failure, return compiler diagnostics, retry
    run tests    → on failure, return test failures, retry
    fault-simulate the passing suite
        detection rate ≥ threshold → accept
        detection rate < threshold → return an aggregate escape count, retry
```

The loop does not terminate when the suite compiles and passes; it terminates when the suite
additionally detects at least `fault_detection_threshold` of the injected faults. A threshold below 1.0
is deliberate: some invariants are not derivable from the specification the generator
is given (see Limitations), so an unreachable 100% target would never converge.

### Independence of derivation

The reinforcement signal is only valid if the tests and the invariants are derived from **different
sources**. If the generator could observe which faults were injected, it would fit assertions to those
specific faults, and the detection rate would measure overfitting rather than verification coverage.
Antigen enforces the separation structurally:

- the generator conditions its assertions on the API specification alone;
- the injected faults, the invariant names, and the mutated values are withheld — feedback is a bare
  escape count — and the simulation report is written outside the generator's workspace.

A corollary serves as a diagnostic: a detection rate close to 100% on a suite derived from the same
context as the invariants is evidence that the two were not independent, and that the metric is
measuring agreement rather than correctness.

### Limitations of the claim

Antigen measures one property: whether the suite's assertions encode the declared invariants. It does
not establish that the invariant set is complete, nor that behavior outside the declared invariants is
correct. As in mutation testing, a high detection rate means the suite catches the faults that were
injected, not that the suite is adequate in general.

Relational and temporal invariants (`created_at <= updated_at`) are a specific case worth stating
precisely. They are perfectly checkable — a test can read both fields and compare them — but an
OpenAPI schema describes each field independently and cannot state the relationship between two of
them. A suite the generator derives from the specification alone therefore has no basis to assert it,
and the invariant escapes. This is the independence property in action rather than a defect: the
invariant encodes domain knowledge the specification omits, so a spec-conditioned suite does not guard
it, whereas a domain-aware hand-written suite can. (A secondary obstacle is idiomatic: RestAssured's
inline body matchers operate on one field at a time, so a cross-field check must extract both values
and compare them explicitly.) Distinguishing invariants that a spec-derived suite cannot reasonably be
expected to catch from genuine assertion gaps, when computing the denominator, is future work.

---

## Standalone fault simulation

The simulator is usable on its own, independently of generation, to evaluate any existing suite —
including one written by hand — against the same invariants. This is the generation objective function
run in isolation: no tests are produced, the recorded suite is simply graded and reported.

```bash
./gradlew test                          # normal run, no simulation
./gradlew test -DrunWithAntigen=true    # fault-simulate the existing suite
```

AspectJ weaving intercepts HTTP responses at load time, so no changes to test code are required. The
same independence caveat applies: the evaluation is meaningful to the extent the invariants were
derived separately from the suite under evaluation.

---

## Invariants

Invariants live in committed YAML under `src/test/resources/antigen/simulation/invariants/`, one file
per domain. Antigen loads every `.yml` in that directory and applies each invariant to any test that
exercises its endpoint; no explicit test-to-invariant mapping is required.

```yaml
# simulation/invariants/orders.yml
name: Order Lifecycle
description: Status transitions, quantity/price constraints, temporal ordering.

invariants:
  /api/v1/orders/{id}:
    GET:
      invariants:
        - name: positive_quantity
          field: quantity
          greater_than: 0

        - name: valid_status
          field: status
          in: [PENDING, FILLED, REJECTED, CANCELLED]

        # conditional: the `then` clause is evaluated (and negated) only when `if` holds
        - name: filled_order_has_timestamp
          if:   { field: status,    equals: FILLED }
          then: { field: filled_at, is_not_null: true }

        # cross-field reference: checkable, but absent from the OpenAPI schema, so a
        # spec-derived suite has no basis to assert it and tends to miss it
        - name: created_before_filled
          if:   { field: filled_at,   is_not_null: true }
          then: { field: created_at,  less_than_or_equal: $.filled_at }

  /api/v1/orders:            # array response: $[*] quantifies over elements
    GET:
      invariants:
        - name: all_orders_positive_quantity
          field: $[*].quantity
          greater_than: 0
```

**Operators:** `equals`, `not_equals`, `greater_than(_or_equal)`, `less_than(_or_equal)` (numeric or
cross-field via `$.other_field`), `in`, `not_in`, `is_null`, `is_not_null`, `is_empty`, `is_not_empty`.
Array fields use `$[*].field`; `default_quantifier` (`all` | `any` | `none`) sets their evaluation mode.

Each operator admits a mechanical negation, which is what turns a property into a counterexample. A
construct is admissible only if it can be inverted into a violating response — the operator set grows
no faster than the negation procedure can support it.

**Scoping.** To restrict an invariant to specific tests, add `include_only` at the file or invariant
level:

```yaml
        - name: token_type_bearer
          field: token_type
          equals: bearer
          include_only:
            - class: com.example.AuthApiTest
              methods: [testLogin]
```

---

## Configuration

All configuration lives under `src/test/resources/antigen/`:

```
antigen/
├── antigen.properties              # config source selection (e.g. io.antigen.core.config.source=local)
├── simulation/
│   ├── config.yml                  # simulation scoping & gating (NOT invariants)
│   ├── coverage_config.yml         # optional endpoint coverage + spec gap analysis
│   └── invariants/*.yml            # the invariants (the oracle)
└── generation/
    ├── config.yml                  # generation loop: spec path, model, threshold, timeouts
    ├── api-specs.yaml              # the OpenAPI specification fed to the generator
    └── prompt.txt                  # optional additional generation guidance
```

`simulation/config.yml` scopes and gates fault injection; it does not define invariants and does not
configure generation. Precedence is most-specific-wins for settings, additive union for exclusions:
method `<Class>.antigen.yml` › class `<Class>.antigen.yml` › this file › built-in default.

```yaml
# simulation/config.yml
exclusions:
  endpoints:                        # glob, full-matched against the request path, applied per response
    - "*/accounts*"
settings:
  default_quantifier: all           # all | any — how array-field invariants quantify
  stop_on_first_catch: false        # stop mutating an endpoint once one fault is caught
simulation:
  only_success_responses: true      # skip non-2xx
  skip_collections_response: true   # skip top-level JSON arrays
  min_response_fields: 1            # skip responses with fewer than N fields
```

Endpoint exclusions apply **per recorded response**: a test that creates an account and then places an
order still has its `/orders` response mutated while `/accounts` is skipped.

---

## Usage

Add the plugin and adapter (see [Installation](#installation)). To generate a suite from the
specification, requiring the `claude` CLI on PATH:

```bash
./gradlew generateTests                             # spec/model/threshold from generation/config.yml
./gradlew generateTests -Pspec=path/to/openapi.yaml # override the spec
```

Or invoke the CLI directly:

```bash
antigen generate --spec openapi.yaml --project . [--max-retries 5] [--model sonnet] [--verbose]
```

Exit `0` = detection threshold met; `1` = threshold missed or retries exhausted. Generated tests are
written to `src/test/java/generated/`. To fault-simulate an existing suite instead, see
[Standalone fault simulation](#standalone-fault-simulation).

---

## Reports

A simulation run writes to the project root:

- **`antigen_report.html`** — summary, per-endpoint fault breakdown, a tests × faults matrix, endpoint
  coverage, and specification gap analysis.
- **`fault_simulation_report.json`** — machine-readable; `"caught_by_any_test": false` on an invariant
  marks an assertion gap.
- **Console summary** — caught / total / escaped per test.

```
============================================================
  Antigen — Simulation Run Summary
============================================================
  Test                          Caught   Total   Escaped
------------------------------------------------------------
  OrdersApiTest.testGetOrder      8        12      4
  AuthApiTest.testLogin           3         3      0
------------------------------------------------------------
```

During generation the report is written in JSON-only mode to a temporary path outside the generator's
workspace, so the injected faults do not leak into the loop. A zero-fault or missing report is treated
as an error, never as a pass.

---

## Installation

Published via JitPack as git tags `vX.Y` on `integralquality/antigen`. This is a **multi-module**
build, so the coordinate is `com.github.<owner>.<repo>:<module>:<tag>`.

**settings.gradle.kts**
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**build.gradle.kts**
```kotlin
buildscript {
    repositories { maven { url = uri("https://jitpack.io") } }
    dependencies {
        // the io.antigen Gradle plugin, from the antigen-cli module
        classpath("com.github.integralquality.antigen:antigen-cli:v0.9")
    }
}
apply(plugin = "io.antigen")

dependencies {
    // the JVM adapter; pulls in the engine transitively
    testImplementation("com.github.integralquality.antigen:antigen-test-runner:v0.9")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.rest-assured:rest-assured:5.5.6")
}
```

The `io.antigen` plugin attaches the AspectJ weaver automatically when `-DrunWithAntigen=true`. For
local development against unpublished changes, add `mavenLocal()` and swap the coordinates to
`io.antigen:<module>:1.0.0-SNAPSHOT` (published with `./gradlew publishToMavenLocal`).

A complete working setup lives in the [antigen-example](https://github.com/integralquality/antigen-example)
repository.

---

## Architecture

Three modules around one engine:

| Module | Role |
|---|---|
| `antigen-engine` | The simulator: negates invariants into mutations, scores caught vs. escaped, renders reports. No AspectJ / JUnit / HTTP dependencies. |
| `antigen-test-runner` | JVM adapter: AspectJ interception, baseline recording, mutated-response replay, pass/fail reporting. |
| `antigen-cli` | The generation loop and the `io.antigen` Gradle plugin. |

Two pointcuts are woven at load time (requires `-javaagent:aspectjweaver.jar`): one wraps each `@Test`
method to establish context and trigger simulation after the baseline passes; one intercepts Apache
HttpClient to record and replay responses. See [`docs/knowledge/`](docs/knowledge/v2/idea_update.md)
for the design rationale, the engine/adapter split, and the roadmap.

---

## Requirements

- Java 17+, Gradle 7.3+
- JUnit 5 (Jupiter)
- Apache HttpClient (via RestAssured or direct)
- `claude` CLI on PATH (generation only)

## Building from source

```bash
git clone https://github.com/integralquality/antigen.git
cd antigen
./gradlew build
./gradlew publishToMavenLocal
```

## Troubleshooting

- **No simulation output** — confirm `-DrunWithAntigen=true` and that the AspectJ agent attached.
- **Invariants missing from the report** — the test must call the invariant's endpoint (matching is by
  endpoint). With `include_only`, `class` must be fully qualified.
- **`ConnectException` to `localhost:8080` on startup** — a config API key was auto-detected; set
  `io.antigen.core.config.source=local` in `antigen.properties`.
- **`advice ... has not been applied`** — benign; test classes are woven at load time, not compile time.
