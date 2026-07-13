# Antigen v2 — Idea Update: from two projects to one loop

> Status: **current direction** (supersedes the "two separate capabilities" framing in the
> top-level `README.md`). Audience: any agent/developer picking up Antigen cold and deciding
> what to build next. This document is the *strategy + context* map; the *how-it-works* details
> live in the docs linked under **Must read first**.

---

## TL;DR

Antigen began as **two things that happened to share a repo**: a *fault-simulation grader* for
existing test suites, and an *AI test generator* from an OpenAPI spec. They have **converged into
one loop**: generation is now gated by simulation — the loop stops when generated tests pass **and**
catch enough injected faults. The strategic bet underneath is a single sentence:

> An **independently-derived, human-ratified semantic oracle** (the invariants) that grades *any*
> suite — human- or AI-written — and, in the converged loop, *drives generation toward that
> oracle* without ever leaking it to the generator.

The load-bearing constraint that makes the convergence legitimate (rather than self-grading
homework) is **independence of derivation**: the tests and the invariant-derived faults must come
from different sources. Most recent work exists to preserve that honesty inside one loop.

---

## Must read first (in order)

1. [`../v1/invariant-derivation.md`](../v1/invariant-derivation.md) — **the** load-bearing principle:
   independence of derivation, the bounded claim, why a suspiciously high catch rate is evidence the
   oracle wasn't independent. Everything below depends on this.
2. [`../v1/architecture.md`](../v1/architecture.md) — the language-neutral **engine + adapter** model, the
   JSON protocol, module split, and migration phases (Phases 0–5 are done).
3. [`../gotchas.md`](../v1/gotchas.md) — recurrent traps (daemon lock, false-uniform reports,
   string-vs-number monetary mutations, nested deep-copy). A green build ≠ a valid report.
4. [`../dsl.md`](../v1/dsl.md) — the (future) invariant authoring DSL and the **invertibility law**
   (every construct must be mechanically negatable into a concrete mutation).
5. [`../roadmap.md`](../v1/roadmap.md) — prioritized feature ideas; several "next steps" below map to it.

---

## The mental model: three "products", one shared brain

The core asset is the **engine** (`antigen-engine`): it turns invariants into concrete response
mutations, scores caught vs. escaped, and renders reports. Everything else is a *consumer* of it.

| # | Product | What it is | Status |
|---|---|---|---|
| A | **Fault-simulation grader** | Score an *existing* suite (human- or AI-written) against invariants. | Working; the original core. |
| B | **Converged generation loop** | Generate a suite from a spec, **gate it with A**, iterate to a detection threshold. | **Primary focus now.** `antigen-cli`. |
| C | **Scaffold: invariant → test** | Emit an asserting test *per invariant* for traceability/cold-start. | **Deferred, by decision** (see below). |

**Critical boundary:** C must **never** be graded by the same invariants it was generated from —
that collapses the metric (affirming X trivially "catches" a violation of X). A and B keep the two
sources independent; C would fold them, so C is a *different* product with a *coverage-by-construction*
claim, not a *quality-measurement* claim. This is why C is postponed, not just unscheduled.

---

## The initial idea (where we started)

Two loosely-coupled capabilities in one repo:

- **Fault simulation** (`io.antigen.core.*`, now `antigen-engine` + `antigen-test-runner`):
  AspectJ intercepts HTTP responses during a JUnit run, mutates them to violate declared
  **invariants**, re-runs each test, and records caught (test fails → good) vs. escaped (test still
  passes → assertion gap). Metric: detection rate.
- **AI test generation** (`io.antigen.ai.*`, now `antigen-cli`): an LLM (Claude CLI) writes a suite
  from an OpenAPI spec; the loop checked only that it **compiled and passed**.

They were conceived as separable deliverables. Simulation graded *your* suite; generation produced
*a* suite. Nothing forced them together.

---

## The current idea (the convergence)

**Generation is gated by simulation.** The loop (`io.antigen.ai.orchestrator.Orchestrator`) is:

```
generate → build → run tests (no faults) → run tests WITH fault simulation
   detection >= threshold  → success (emit proof report)
   detection <  threshold  → feed an aggregate "N escaped, strengthen from the spec" back, retry
```

Simulation stopped being a standalone grader and became the **quality gate** of generation. The loop
does not stop when tests pass; it stops when they pass **and** catch enough faults.

### Why this is only legitimate under independence

If the generator could see *which* faults were injected, it would overfit assertions to the answer
key and the metric would mean nothing (`invariant-derivation.md`). So the converged loop enforces:

- the agent **derives assertions from the spec only** (prompt in
  `io.antigen.ai.llm.PromptBuilder`), and
- the **injected faults are hidden** from the agent — feedback is aggregate-only
  (`io.antigen.ai.phases.AntigenPhase.getFeedback`), and the report is written in `json_only` mode
  to a temp path **outside** the agent's workspace (`io.antigen.ai.runners.GradleRunner.runAntigen`
  + `antigen-test-runner`'s `GlobalTestExecutionListener`).

### Why the threshold exists (a direct consequence of convergence)

As a *grader* (product A) you just report escapes. As a *gate* (product B) you need a **reachable**
target: 100% is impossible when structurally-uncatchable invariants (cross-field / temporal, e.g.
`created_at <= updated_at`) are present — RestAssured can't express relational assertions
(`gotchas.md`). So B added a configurable **`fault_detection_threshold`** in the project's
`config.yml`, honored in `Orchestrator` and loaded in `io.antigen.ai.Antigen`.

---

## Where the code lives (module map)

| Module | Role | Key symbols |
|---|---|---|
| `antigen-engine` | Pure simulation math + protocol server. No AspectJ/JUnit/HTTP deps. | `core.invariant.ViolationGenerator`, `core.plan.FaultPlanner`, `core.simulation.FaultSimulationReport`, `core.report.HtmlReportGenerator`, `core.protocol.*` |
| `antigen-test-runner` | JVM adapter: capture baseline, replay mutated bodies, report pass/fail. | `core.interceptor.AspectExecutor`, `TestContext`, `GlobalTestExecutionListener`, `core.runner.Runner` |
| `antigen-cli` | The converged generation loop + the `io.antigen` Gradle plugin. | `ai.orchestrator.Orchestrator`, `ai.llm.PromptBuilder`, `ai.runners.GradleRunner`, `ai.phases.AntigenPhase`, `ai.Antigen` |
| `e2e/` | A self-contained consumer project (own `gradlew`) that resolves the **local** build via `mavenLocal` — used to exercise the loop end-to-end against the live `oms-demo-api` on `localhost:8000`. | `config.yml`, `simulation/invariants/trading-*.yml` |

Invariants are committed YAML under `<project>/src/test/resources/antigen/simulation/invariants/`.
Run the loop with `./gradlew :antigen-cli:run --args="generate -s <spec> -p <project-root>"`
(publish local engine changes first with `./gradlew publishToMavenLocal`).

---

## What changed recently (so you don't redo it)

All of the following are committed (`c10162a`, `dbb8611`, and the earlier `769d274`):

**Honesty / independence**
- Feedback is aggregate-only; no invariant names, no pointer to the report.
- `json_only` report mode; report written outside the agent workspace (stable temp path, retained
  for developer inspection, hidden from the agent).
- Prompt reframed to "assert the contract from the spec"; catching faults is the *consequence*.

**Trustworthy metric (no false greens)**
- `--rerun-tasks` on the simulation run (Gradle was skipping it as up-to-date → empty report).
- **Zero-fault report = error**, not success. Missing/unparseable report = fail-fast, distinct from a
  real escaped-fault result.
- The proof HTML report is emitted only when the run exited 0 **and** produced faults; it is now
  generated on **all** valid terminal outcomes (success, give-up, max-retries).

**Achievable gate + ops**
- `fault_detection_threshold` via `config.yml` (default engine behavior stays 1.0 if unset).
- Per-phase + outcome progress appended to `ai_logs.txt`; project-root guard on `-p`;
  `application` plugin so `:antigen-cli:run` works; spec-path rendering fix.
- Invariant soundness pass on the e2e fixtures (removed an unsound element-wise cross-field temporal;
  added a spec-backed `order_type in [BUY, SELL]`).

---

## What to tackle next (prioritized)

1. **Test isolation / control runs** *(highest value; roadmap #4)*. Empty-report incidents trace to
   generated tests that aren't idempotent against the live API (re-running creates duplicate
   resources → 409s → failed baseline → no simulation). Implement the **control run**: run each test
   once with the *unmutated* cached response; if it fails, mark it flaky and **exclude** it from the
   score instead of producing an empty/misleading report. This also hardens the grader (A).

2. **Regression guard in the loop.** The agent rewrites the whole test file each retry and can
   regress below its best (compile errors, new test failures). Track the best attempt and stop /
   revert rather than shipping a worse suite. Lives in `Orchestrator` + `shouldRetry`.

3. **Classify uncatchable invariants** *(makes the threshold principled)*. A flat 0.8 lumps
   genuinely-uncatchable rules (cross-field/temporal) with fixable gaps. Have the engine tag
   invariants whose violations no assertion library can catch and **exclude them from the
   denominator**, so the score means "of the *catchable* rules, X% are guarded." Ties to `dsl.md`'s
   invertibility law and `invariant-derivation.md`'s bounded claim.

4. **Scaffold (product C), when picked up.** Emit an asserting test per invariant. Decisions already
   made: assertions should ideally be **deterministically emitted** from the invariant (reuses
   `ViolationGenerator`'s negation machinery, inverted), with the LLM owning only setup; if the LLM
   writes assertions, **hide the fault plan** to avoid overfitting. Do **not** grade C's output with
   the same invariants (circularity — see the boundary above).

5. **Invariant provenance / approval queue** *(the moat; roadmap #2/#3, `invariant-derivation.md`)*.
   Auto-derive candidate invariants from the spec (and later traffic/AI), and build the ranked
   human approval UX. The ratified corpus is the durable asset; keep derivation independent of the
   suite under test.

---

## Guardrails to preserve (do not regress)

- **Never leak the fault set to the generator.** No invariant names or report pointers in feedback;
  keep reports out of the agent's workspace during generation.
- **A zero-fault or missing report is never a pass.**
- **Keep invariants committed YAML**, human-owned; the engine never trusts runtime-generated rules.
- **State the claim honestly:** Antigen measures *"does the suite verify the declared invariants,"*
  not *"the suite is good."* (`invariant-derivation.md`, "Bounded claim".)
