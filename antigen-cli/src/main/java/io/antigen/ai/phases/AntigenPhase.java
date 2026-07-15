package io.antigen.ai.phases;

import io.antigen.ai.model.EscapedFault;
import lombok.Value;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

@Value
public class AntigenPhase implements PhaseResult {
    boolean success;
    List<EscapedFault> escapedFaults;
    double faultDetectionRate;
    int totalFaults;
    int caughtFaults;
    // Non-null only for an infrastructure error: the simulation produced no usable report
    // (missing/empty/unparseable), as opposed to a real escaped-fault result. The loop must
    // abort on this rather than feed the agent bogus "strengthen your assertions" guidance.
    String errorMessage;

    public static AntigenPhase success(double detectionRate, int total, int caught) {
        return new AntigenPhase(true, List.of(), detectionRate, total, caught, null);
    }

    public static AntigenPhase failed(List<EscapedFault> escaped, double detectionRate, int total, int caught) {
        return new AntigenPhase(false, escaped, detectionRate, total, caught, null);
    }

    public static AntigenPhase error(String message) {
        return new AntigenPhase(false, List.of(), 0.0, 0, 0, message);
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public boolean hasEscapedFaults() {
        return !escapedFaults.isEmpty();
    }

    @Override
    public String getFeedback() {
        if (isError()) {
            return errorMessage;
        }
        if (success) {
            return String.format("Antigen passed - %.1f%% fault detection rate (%d/%d faults caught)",
                faultDetectionRate * 100, caughtFaults, totalFaults);
        }

        return String.format("""
            ANTIGEN FAILURE - Your tests passed but did NOT catch %d out of %d injected faults (%.1f%% detection rate).

            Each test method below let a mutated response field through: the value was made invalid and
            the test still passed. The SAME escaped fault can be missed by several tests that exercise
            the same endpoint, so the per-test counts below OVERLAP -- they will not sum to the %d
            escaped faults above. Read each line as "this test needs stronger assertions", not as a
            separate fault.
            %s
            Strengthen ONLY the assertions in the test methods listed above. Do NOT modify any test that
            is not listed -- those already fully verify their responses, and changing them risks
            regressing working assertions.

            You are NOT told which specific fields or values were mutated -- that is withheld so your
            assertions verify the API's real contract derived from the specification, not a leaked
            answer key. Do not open or read the Antigen report or anything under build/.

            For each listed test, re-derive its assertions from the API specification:
            - Assert every field in the response body, not just the status code.
            - For each field, assert it is present, non-null, and of the correct JSON type.
            - Assert the value constraints the spec implies: allowed enum values, numeric ranges,
              non-empty strings and arrays, and required formats.
            - Validate nested objects and array elements, not only top-level fields.
            """,
            escapedFaults.size(),
            totalFaults,
            faultDetectionRate * 100,
            escapedFaults.size(),
            formatEscapedTests());
    }

    /**
     * Groups escaped faults by the test method that failed to catch them, revealing only the test
     * name and how many faults escaped it -- never the field, fault type, or expected value. This
     * points the agent at the tests it needs to strengthen (so it leaves its passing tests alone)
     * without leaking the injected fault set it must not overfit to.
     */
    private String formatEscapedTests() {
        // A fault's testName holds the tests that exercised its endpoint but did not catch it
        // (comma-joined). Attribute the escape to each of those tests individually, so a test that
        // ran against a mutated response and stayed green is counted as having missed that fault.
        // A fault counted against several tests raises each of their tallies (counts may sum to
        // more than the total escaped -- each test is independently responsible for its own gap).
        Map<String, Long> byTest = new LinkedHashMap<>();
        long noCoverage = 0;
        for (EscapedFault fault : escapedFaults) {
            String testName = fault.getTestName();
            if (testName == null || testName.isBlank()) {
                noCoverage++;
                continue;
            }
            for (String test : testName.split(",")) {
                String name = test.trim();
                if (!name.isBlank()) {
                    byTest.merge(name, 1L, Long::sum);
                }
            }
        }

        String lines = byTest.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(e -> String.format("  - %s: missed %d escaped fault(s)", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));

        if (noCoverage > 0) {
            String noCoverageLine = String.format(
                    "  - %d escaped fault(s) had NO covering test -- add a test that exercises and asserts that response",
                    noCoverage);
            lines = lines.isEmpty() ? noCoverageLine : lines + "\n" + noCoverageLine;
        }
        return lines;
    }
}
