# Metatest - REST API Mutation Testing Framework

> **Experimental Project (v0.1.0)**
>
> This project is currently in experimental/development phase. Features and APIs may change between versions.

Metatest validates REST API test reliability through systematic fault injection and bytecode-level response manipulation.

## Table of Contents

- [Overview](#overview)
- [Problem Statement](#problem-statement)
- [Architecture](#architecture)
- [Technical Implementation](#technical-implementation)
- [Configuration](#configuration)
  - [Local Mode (YAML)](#local-mode-yaml)
  - [API Mode (Cloud)](#api-mode-cloud)
- [Invariants DSL Reference](#invariants-dsl-reference)
  - [Supported Operators](#supported-operators)
  - [Field References](#field-references)
  - [Array Fields](#array-fields)
  - [Conditional Invariants](#conditional-invariants-ifthen)
  - [Complete Example](#complete-example-for-any-api)
- [Installation](#installation)
- [Reports and Analytics](#reports-and-analytics)
- [Integration with CI/CD](#integration-with-cicd)
- [Use Cases](#use-cases)
- [Performance Considerations](#performance-considerations)
- [Requirements](#requirements)
- [Troubleshooting](#troubleshooting)
- [Building from Source](#building-from-source)

## Overview

Metatest applies mutation testing principles to REST API integration tests. It intercepts HTTP responses during test execution, injects faults into response payloads, and re-executes tests to verify they detect the injected failures. Tests that pass despite injected faults indicate weak assertions or incomplete validation logic.

The framework operates transparently through AspectJ bytecode weaving, requiring no modifications to existing test code.

## Problem Statement

Traditional test coverage metrics measure code execution paths but fail to assess assertion quality. A test with 100% code coverage may still pass when the API returns incorrect data, null values, or missing fields.

Metatest addresses this by answering: "Would your tests catch real API contract violations?"

## Architecture

### Core Components

```
metatest-rest-java/
├── lib/                          # Core library
│   ├── core/
│   │   ├── interceptor/          # AspectJ interception layer
│   │   │   ├── AspectExecutor    # @Test and HTTP client interception
│   │   │   └── TestContext       # Thread-local execution context
│   │   ├── config/               # Configuration management
│   │   │   ├── LocalConfigurationSource   # YAML-based config
│   │   │   └── ApiConfigurationSource     # Cloud API config
│   │   └── normalizer/           # Endpoint pattern normalization
│   ├── injection/                # Fault injection strategies
│   │   ├── NullFieldStrategy     # Set fields to null
│   │   ├── MissingFieldStrategy  # Remove fields entirely
│   │   ├── EmptyListStrategy     # Empty arrays/collections
│   │   └── EmptyStringStrategy   # Empty string values
│   ├── simulation/               # Test execution engine
│   │   ├── Runner                # Fault simulation orchestrator
│   │   └── FaultSimulationReport # Results aggregation
│   ├── coverage/                 # Endpoint coverage tracking
│   │   ├── Collector             # HTTP call logging
│   │   └── EndpointMethodCoverage # Per-endpoint metrics
│   ├── analytics/                # Test quality analysis
│   │   ├── GapAnalyzer           # Identify untested paths
│   │   └── AssertionAnalytics    # Assertion strength metrics
│   ├── http/                     # HTTP abstraction layer
│   │   └── HTTPFactory           # Request/Response wrappers
│   └── api/                      # Cloud API integration
└── gradle-plugin/                # Gradle plugin for zero-config setup
    └── MetatestPlugin            # Automatic AspectJ configuration
```

### Execution Flow

```
┌─────────────────────────────────────────────────────────────┐
│  Test Execution Phase                                       │
│  (Baseline capture)                                         │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │   @Test method invoked         │
         │   AspectExecutor intercepts    │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │   HTTP client makes request    │
         │   Request captured to context  │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │   Response received            │
         │   Response captured to context │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │   Test completes (original)    │
         │   Baseline established         │
         └────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Fault Simulation Phase                                     │
│  (Mutation testing)                                         │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │   For each response field:     │
         │     For each fault type:       │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │   Apply fault to response      │
         │   (null, missing, empty, etc)  │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │   Re-execute @Test method      │
         │   with faulty response         │
         └────────────────────────────────┘
                          │
                    ┌─────┴─────┐
                    │           │
                  Pass        Fail
                    │           │
                    ▼           ▼
            ┌──────────┐  ┌──────────┐
            │  ESCAPED │  │ DETECTED │
            │  FAULT   │  │  FAULT   │
            └──────────┘  └──────────┘
                    │           │
                    └─────┬─────┘
                          ▼
         ┌────────────────────────────────┐
         │   Record result in report      │
         └────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │   Generate fault_simulation    │
         │   _report.json                 │
         └────────────────────────────────┘
```

## Technical Implementation

### AspectJ Interception

Metatest uses compile-time and load-time weaving to intercept:

1. **Test method execution** - `@Around("execution(@org.junit.jupiter.api.Test * *(..))")`
   - Establishes thread-local test context
   - Captures baseline test execution
   - Triggers fault simulation after successful baseline

2. **HTTP client calls** - `@Around("execution(* org.apache.http.impl.client.CloseableHttpClient.execute(..))")`
   - Intercepts Apache HttpClient requests
   - Captures request/response pairs
   - Injects faulty responses during simulation runs

### Thread Safety

Each test execution maintains isolated state via `ThreadLocal<TestContext>`:
- Original request/response pair
- Simulated faulty response
- Test metadata (name, endpoint, method)

Context is cleared after each test to prevent memory leaks and cross-test contamination.

### Fault Injection Strategies

| Strategy | Mutation | Use Case |
|----------|----------|----------|
| `NullFieldStrategy` | Set field value to `null` | Tests assertion: `assertNotNull(response.field)` |
| `MissingFieldStrategy` | Remove field from JSON | Tests field existence checks |
| `EmptyListStrategy` | Replace array with `[]` | Tests collection size assertions |
| `EmptyStringStrategy` | Replace string with `""` | Tests non-empty string validation |

Each strategy operates on the parsed JSON response map before re-serialization.

### Simulation Algorithm

```java
for (String field : responseFields) {
    for (FaultType fault : enabledFaults) {
        Response faultyResponse = applyFault(originalResponse, field, fault);
        context.setSimulatedResponse(faultyResponse);

        try {
            testMethod.rerun();
            // Test passed with faulty data → ESCAPED FAULT
            report.recordEscapedFault(endpoint, field, fault, testName);
        } catch (AssertionError e) {
            // Test failed as expected → DETECTED FAULT
            report.recordDetectedFault(endpoint, field, fault, testName);
        }
    }
}
```

## Configuration

### Local Mode (YAML)

Create `src/main/resources/config.yml`:

```yaml
version: "1.0"

# Global settings
settings:
  default_quantifier: all  # For array fields: all, any, none

# =============================================================================
# INVARIANTS - Business Rule Validation (NEW)
# =============================================================================
# Define invariants (business rules) that your API responses must satisfy.
# Metatest will generate mutations that violate these rules and verify
# your tests catch the violations.

endpoints:
  /api/v1/orders/{id}:
    GET:
      invariants:
        # Simple field constraint
        - name: positive_quantity
          field: quantity
          greater_than: 0

        # Enum validation
        - name: valid_status
          field: status
          in: [PENDING, FILLED, REJECTED, CANCELLED]

        # Conditional invariant (if/then)
        - name: filled_order_has_timestamp
          if:
            field: status
            equals: FILLED
          then:
            field: filled_at
            is_not_null: true

        # Cross-field comparison
        - name: created_before_updated
          field: created_at
          less_than_or_equal: $.updated_at

  /api/v1/orders:
    GET:
      invariants:
        # Array field validation (applies to all items)
        - name: all_orders_positive_quantity
          field: $[*].quantity
          greater_than: 0

# =============================================================================
# CONTRACT FAULTS - Field-Level Mutations
# =============================================================================
faults:
  null_field:
    enabled: true
  missing_field:
    enabled: true
  empty_list:
    enabled: false
  empty_string:
    enabled: false
  invalid_value:
    enabled: false

# =============================================================================
# EXCLUSIONS
# =============================================================================
exclusions:
  urls:
    - '*/login*'
    - '*/auth/*'
    - '*/health'
  tests:
    - '*IntegrationTest*'
    - '*LoginTest*'

# Simulation filters
simulation:
  only_success_responses: true
  skip_collections_response: true
  min_response_fields: 1

# Report configuration
report:
  format: json
  output_path: "./fault_simulation_report.json"
```

---

## Invariants DSL Reference

Invariants define business rules that API responses must satisfy. Metatest generates mutations that violate these rules and verifies your tests detect the violations.

### Supported Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `equals` | Exact match | `equals: "ACTIVE"` |
| `not_equals` | Not equal to | `not_equals: "DELETED"` |
| `greater_than` | Numeric > | `greater_than: 0` |
| `greater_than_or_equal` | Numeric >= | `greater_than_or_equal: 0` |
| `less_than` | Numeric < | `less_than: 100` |
| `less_than_or_equal` | Numeric <= | `less_than_or_equal: $.other_field` |
| `in` | Value in list | `in: [BUY, SELL]` |
| `not_in` | Value not in list | `not_in: [DELETED, ARCHIVED]` |
| `is_null` | Must be null | `is_null: true` |
| `is_not_null` | Must not be null | `is_not_null: true` |
| `is_empty` | Must be empty | `is_empty: true` |
| `is_not_empty` | Must not be empty | `is_not_empty: true` |

### Field References

Use `$.field_name` to reference other fields in the same response:

```yaml
- name: filled_after_created
  field: created_at
  less_than_or_equal: $.filled_at
```

### Array Fields

Use `$[*].field` syntax to validate all items in an array:

```yaml
- name: all_items_positive_price
  field: $[*].price
  greater_than: 0
```

The `default_quantifier` setting controls how array validations work:
- `all` (default): All items must satisfy the condition
- `any`: At least one item must satisfy
- `none`: No items should satisfy

### Conditional Invariants (if/then)

Define rules that only apply when a precondition is met:

```yaml
- name: filled_order_has_filled_at
  if:
    field: status
    equals: FILLED
  then:
    field: filled_at
    is_not_null: true
```

This invariant only applies when `status == FILLED`. Metatest will:
1. Skip this invariant if status is not FILLED
2. Generate a mutation setting `filled_at` to null when status is FILLED
3. Verify your test catches this violation

### Complete Example for Any API

Here's a template you can adapt for your own API:

```yaml
version: "1.0"

settings:
  default_quantifier: all

endpoints:
  # ============================================
  # USER MANAGEMENT
  # ============================================
  /api/users/{id}:
    GET:
      invariants:
        - name: user_has_email
          field: email
          is_not_empty: true

        - name: valid_user_status
          field: status
          in: [ACTIVE, SUSPENDED, PENDING]

        - name: active_user_has_verified_email
          if:
            field: status
            equals: ACTIVE
          then:
            field: email_verified
            equals: true

  /api/users:
    GET:
      invariants:
        - name: all_users_have_id
          field: $[*].id
          is_not_null: true

    POST:
      invariants:
        - name: new_user_is_pending
          field: status
          equals: PENDING

  # ============================================
  # E-COMMERCE
  # ============================================
  /api/products/{id}:
    GET:
      invariants:
        - name: positive_price
          field: price
          greater_than: 0

        - name: non_negative_stock
          field: stock_quantity
          greater_than_or_equal: 0

        - name: discounted_price_less_than_original
          if:
            field: discount_price
            is_not_null: true
          then:
            field: discount_price
            less_than: $.price

  /api/orders/{id}:
    GET:
      invariants:
        - name: order_total_positive
          field: total_amount
          greater_than: 0

        - name: shipped_order_has_tracking
          if:
            field: status
            equals: SHIPPED
          then:
            field: tracking_number
            is_not_empty: true

        - name: all_items_positive_quantity
          field: $[*].items.quantity
          greater_than: 0

  # ============================================
  # AUTHENTICATION
  # ============================================
  /api/auth/login:
    POST:
      invariants:
        - name: login_returns_token
          field: access_token
          is_not_empty: true

        - name: token_type_is_bearer
          field: token_type
          equals: bearer

faults:
  null_field:
    enabled: true
  missing_field:
    enabled: true
```

### API Mode (Cloud)

Create `src/main/resources/metatest.properties`:

```properties
metatest.api.key=mt_proj_xxxxxxxxxxxxxxxx
metatest.project.id=your-project-id
metatest.api.url=https://api.metatest.io
```

Or set environment variables:
```bash
export METATEST_API_KEY=mt_proj_xxxxxxxxxxxxxxxx
export METATEST_PROJECT_ID=your-project-id
```

API mode fetches fault strategies from a centralized service and submits simulation results for team-wide analysis.

## Installation

### Step 1: Add JitPack Repository

**settings.gradle.kts:**
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add Dependency

**build.gradle.kts:**
```kotlin
plugins {
    java
}

dependencies {
    testImplementation("com.github.at-boundary:metatest-rest-java:v0.1.0")

    // Your existing test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("io.rest-assured:rest-assured:5.3.0")
}
```

### Step 3: Configure AspectJ Agent

**build.gradle.kts:**
```kotlin
tasks.test {
    useJUnitPlatform()

    val aspectjAgent = configurations.testRuntimeClasspath.get()
        .files.find { it.name.contains("aspectjweaver") }

    if (aspectjAgent != null) {
        jvmArgs(
            "-javaagent:$aspectjAgent",
            "-DrunWithMetatest=${System.getProperty("runWithMetatest") ?: "false"}"
        )
    }
}
```

### Step 4: Run Tests

```bash
# Normal test execution (no mutation testing)
./gradlew test

# With Metatest fault simulation enabled
./gradlew test -DrunWithMetatest=true
```

## Reports and Analytics

MetaTest generates both JSON and HTML reports after test execution.

### HTML Report (Human-Readable)

Generated at `metatest_report.html` - Open this file in any web browser for an interactive, visual report.

**Features:**
- Modern, responsive UI with tabs for different views
- Summary dashboard with key metrics (detection rate, escaped faults, coverage)
- Interactive fault simulation results with expandable details
- Gap analysis showing tested vs untested endpoints
- Schema coverage with detailed HTTP call logs
- Color-coded status indicators (green = detected, red = escaped)

Simply open `metatest_report.html` in your browser after running tests with MetaTest enabled.

### Fault Simulation Report (JSON)

Generated at `fault_simulation_report.json` for programmatic access:

```json
{
  "/api/users/{id}": {
    "contract_faults": {
      "null_field": {
        "username": {
          "caught_by_any_test": true,
          "tested_by": ["UserApiTest.testGetUserById"],
          "caught_by": [
            {
              "test": "UserApiTest.testGetUserById",
              "caught": true,
              "error": "Expected non-null but was: null"
            }
          ]
        },
        "email": {
          "caught_by_any_test": false,
          "tested_by": ["UserApiTest.testGetUserById"],
          "caught_by": []
        }
      },
      "missing_field": {
        "username": {
          "caught_by_any_test": false,
          "tested_by": ["UserApiTest.testGetUserById"],
          "caught_by": []
        }
      }
    },
    "invariant_faults": {
      "positive_balance": {
        "caught_by_any_test": true,
        "tested_by": ["UserApiTest.testGetUserById"],
        "caught_by": [
          {
            "test": "UserApiTest.testGetUserById",
            "caught": true,
            "error": "Expected balance > 0"
          }
        ]
      },
      "active_user_has_email": {
        "caught_by_any_test": false,
        "tested_by": ["UserApiTest.testGetUserById"],
        "caught_by": []
      }
    }
  }
}
```

**Report Structure:**
- `contract_faults`: Field-level mutations (null, missing, empty)
  - Grouped by fault type → field name
- `invariant_faults`: Business rule violations
  - Grouped by invariant name

**Key Fields:**
- `caught_by_any_test`: `false` = Critical weakness, no test detected this fault
- `tested_by`: List of all tests that ran with this mutation
- `caught_by`: Details of tests that successfully detected the fault

### Schema Coverage Report

Generated at `schema_coverage.json`:

```json
{
  "/api/users/{id}": {
    "GET": {
      "response_fields": ["id", "username", "email", "created_at"],
      "tested_by": ["UserApiTest.testGetUserById"],
      "coverage_percentage": 100.0
    }
  }
}
```

### Gap Analysis Report

Generated at `gap_analysis.json`:

Identifies endpoints defined in OpenAPI specification but not covered by tests.

```json
{
  "untested_endpoints": [
    {
      "path": "/api/users/{id}",
      "method": "DELETE",
      "reason": "No test execution captured"
    }
  ],
  "coverage_summary": {
    "total_endpoints": 12,
    "tested_endpoints": 10,
    "coverage_percentage": 83.33
  }
}
```

## Integration with CI/CD

### GitHub Actions

```yaml
name: Metatest Validation

on: [push, pull_request]

jobs:
  test-quality:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: read

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run tests with MetaTest
        env:
          GPR_USER: ${{ github.actor }}
          GPR_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: ./gradlew test -DrunWithMetatest=true

      - name: Analyze fault detection rate
        run: |
          DETECTED=$(jq '[.[][] | .[] | select(.caught_by_any_test == true)] | length' fault_simulation_report.json)
          TOTAL=$(jq '[.[][] | .[] | length] | add' fault_simulation_report.json)
          RATE=$(echo "scale=2; $DETECTED / $TOTAL * 100" | bc)
          echo "Fault Detection Rate: $RATE%"

          if (( $(echo "$RATE < 95" | bc -l) )); then
            echo "::error::Fault detection rate below 95% threshold"
            exit 1
          fi

      - name: Upload reports
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: metatest-reports
          path: |
            fault_simulation_report.json
            schema_coverage.json
            gap_analysis.json
```

## Use Cases

### 1. Validating API Client Libraries

When building SDK/client libraries that wrap REST APIs, Metatest ensures error handling is robust:

```java
@Test
void testGetUser_handlesNullFields() {
    User user = apiClient.getUser(123);
    assertNotNull(user.getEmail(), "Email should never be null");
    assertNotNull(user.getUsername(), "Username should never be null");
}
```

Metatest will inject `"email": null` and verify the test catches it.

### 2. Contract Testing

Verify your tests would catch breaking API changes:

```java
@Test
void testUserResponse_requiresIdField() {
    Response response = RestAssured.get("/users/123");
    assertNotNull(response.jsonPath().get("id"), "id field is required");
}
```

Metatest removes the `id` field and confirms the test fails appropriately.

### 3. Integration Test Hardening

Identify weak assertions in existing test suites without manual review:

```java
// Weak test - only checks status code
@Test
void testGetOrder() {
    Response response = RestAssured.get("/orders/456");
    assertEquals(200, response.statusCode());
    // Missing: field validations, null checks, type assertions
}
```

Metatest will show this test passes even when response fields are null or missing.

## Comparison with Traditional Testing

| Aspect | Traditional Testing | Metatest |
|--------|-------------------|----------|
| **Measures** | Code execution paths | Assertion quality |
| **Answers** | "Is this code executed?" | "Would tests catch bugs?" |
| **Coverage** | Line/branch coverage | Fault detection coverage |
| **False confidence** | High coverage, weak assertions | Exposes weak assertions |
| **Implementation** | Source instrumentation | Response mutation |

## Performance Considerations

### Execution Time

Simulation time increases linearly with:
- Number of response fields: `F`
- Number of enabled fault types: `T`
- Number of tests: `N`

**Total test re-executions**: `N × F × T`

Example: 10 tests, 5 fields per response, 4 fault types = 200 additional test runs.

**Optimization strategies:**
- Use `simulation.only_success_responses: true` to skip error responses
- Exclude slow/integration tests via `tests.exclude` patterns
- Run Metatest on CI only, not during local development
- Configure `simulation.min_response_fields` to skip simple responses

### Memory Usage

AspectJ weaving requires additional heap space. The Gradle plugin automatically configures:
- `-Xmx2g` - Maximum heap size
- `-Xms512m` - Initial heap size

Adjust if needed for large test suites:

```kotlin
metatest {
    jvmArgs = listOf("-Xmx4g", "-Xms1g")
}
```

## Requirements

- **Java**: 17 or higher
- **Gradle**: 7.3 or higher
- **JUnit**: 5.x (Jupiter)
- **HTTP Client**: Apache HttpClient (via RestAssured or direct usage)

## Troubleshooting

### AspectJ Weaver Not Found

**Symptom**: Tests run but no fault simulation occurs.

**Solution**: Ensure `aspectjweaver` is on test classpath:
```kotlin
testImplementation("org.aspectj:aspectjweaver:1.9.19")
```

### No HTTP Responses Captured

**Symptom**: Log shows "No interceptable HTTP response was captured."

**Solution**: Verify you're using Apache HttpClient (RestAssured uses it by default). Metatest does not support Java's native `HttpURLConnection` or OkHttp without custom adapters.

### Tests Fail Only with Metatest Enabled

**Symptom**: Tests pass normally but fail with `-DrunWithMetatest=true`.

**Solution**: This indicates tests have weak assertions. Review the fault simulation report to identify which fields are not being validated.

### Configuration File Not Found

**Symptom**: `FileNotFoundException: config.yml`

**Solution**: Ensure `config.yml` is in `src/main/resources/` or `src/test/resources/`. Check file name spelling.

### GitHub Packages Authentication Failed

**Symptom**: `Could not resolve io.metatest:metatest:x.x.x`

**Solution**:
1. Set `GPR_USER` and `GPR_TOKEN` environment variables
2. Or create `~/.gradle/gradle.properties`:
   ```properties
   gpr.user=your-github-username
   gpr.token=ghp_xxxxxxxxxxxxxxxxxxxxx
   ```
3. Token requires `read:packages` scope

## Project Structure

```
metatest-rest-java/
├── lib/                       # Core library module
│   ├── src/main/java/
│   │   └── metatest/
│   │       ├── core/         # Core framework
│   │       ├── injection/    # Fault strategies
│   │       ├── simulation/   # Execution engine
│   │       ├── coverage/     # Coverage tracking
│   │       ├── analytics/    # Analysis tools
│   │       ├── http/         # HTTP abstractions
│   │       └── api/          # Cloud API client
│   └── src/test/java/        # Framework tests
├── gradle-plugin/             # Gradle plugin module
│   └── src/main/java/
│       └── metatest/gradle/
│           ├── MetatestPlugin      # Plugin entry point
│           └── MetatestExtension   # Configuration DSL
└── settings.gradle.kts        # Multi-module configuration
```

## Building from Source

```bash
# Clone repository
git clone https://github.com/at-boundary/metatest-rest-java.git
cd metatest-rest-java

# Build and publish to local Maven
./gradlew publishToMavenLocal

# Run framework tests
./gradlew :lib:test

# Run example project tests with MetaTest
cd ../metatest-rest-java-example
./gradlew test -DrunWithMetatest=true
```

## Publishing

### GitHub Packages

Configured in `lib/build.gradle.kts` and `gradle-plugin/build.gradle.kts`:

```bash
export GPR_USER=your-github-username
export GPR_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxx

./gradlew publish
```

Publishes three artifacts:
- `io.metatest:metatest` - Core library
- `io.metatest:gradle-plugin` - Plugin implementation
- `io.metatest.gradle.plugin:io.metatest.gradle.plugin` - Plugin marker artifact

## Related Projects

- **Antigen** - AI-powered test generation with Metatest validation
- **PIT** - Mutation testing for Java source code (not API responses)
- **RestAssured** - REST API testing framework
- **WireMock** - HTTP mocking for testing

## Research Background

Metatest applies mutation testing principles to integration testing. Traditional mutation testing modifies source code; Metatest modifies API responses at runtime.

**Key Difference**: Metatest validates external contract adherence rather than internal logic coverage.



## Support

- **Issues**: https://github.com/at-boundary/metatest-rest-java/issues
- **Example Project**: https://github.com/at-boundary/metatest-rest-java-example
- **Documentation**: See `/docs` directory (coming soon)

## Changelog

### 1.0.0-dev (Current)

- Initial release
- Support for JUnit 5 + Apache HttpClient
- Local configuration via YAML
- API configuration via properties
- Four fault injection strategies
- Gradle plugin for zero-config setup
- Multi-module architecture (lib + gradle-plugin)
- Fault simulation reports
- Schema coverage tracking
- Gap analysis with OpenAPI specs
