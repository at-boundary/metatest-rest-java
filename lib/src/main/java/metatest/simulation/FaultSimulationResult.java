package metatest.simulation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the aggregated result of simulating a specific fault on a specific field.
 * Contains a flag indicating if ANY test caught the fault, plus detailed results from all tests.
 */
@Data
public class FaultSimulationResult {

    /**
     * True if at least one test caught this fault, false if all tests missed it.
     */
    @JsonProperty("caught_by_any_test")
    private boolean caughtByAnyTest;

    /**
     * Detailed results from each individual test that ran with this fault simulation.
     */
    @JsonProperty("details")
    private List<TestLevelSimulationResults> details;

    public FaultSimulationResult() {
        this.caughtByAnyTest = false;
        this.details = new ArrayList<>();
    }

    /**
     * Adds a test result to this fault simulation result.
     * Updates caughtByAnyTest flag if the test caught the fault.
     *
     * @param testResult The result from a single test execution
     */
    public void addTestResult(TestLevelSimulationResults testResult) {
        if (testResult == null) {
            return;
        }

        details.add(testResult);

        if (testResult.isCaught()) {
            caughtByAnyTest = true;
        }
    }

    /**
     * Returns true if no tests have been added yet.
     * This is a utility method not included in JSON serialization.
     */
    @JsonIgnore
    public boolean isEmpty() {
        return details.isEmpty();
    }

    /**
     * Returns the number of test results.
     * This is a utility method not included in JSON serialization.
     */
    @JsonIgnore
    public int size() {
        return details.size();
    }
}
