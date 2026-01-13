package metatest.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents all fault simulation results for a single endpoint.
 * Separates results into contract faults (field-level mutations) and relation faults (business rule violations).
 */
@Data
public class EndpointFaultResults {

    /**
     * Contract faults grouped by fault type, then by field name.
     * Structure: { "null_field": { "status": {...}, "id": {...} }, "missing_field": { ... } }
     */
    @JsonProperty("contract_faults")
    private Map<String, Map<String, FaultSimulationResult>> contractFaults;

    /**
     * Relation faults grouped by relation name.
     * Structure: { "positive_quantity": {...}, "filled_order_has_filled_at": {...} }
     */
    @JsonProperty("relation_faults")
    private Map<String, FaultSimulationResult> relationFaults;

    public EndpointFaultResults() {
        this.contractFaults = new ConcurrentHashMap<>();
        this.relationFaults = new ConcurrentHashMap<>();
    }

    /**
     * Records a contract fault result (field-level mutation).
     */
    public void recordContractFault(String faultType, String field, TestLevelSimulationResults result) {
        contractFaults
                .computeIfAbsent(faultType, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(field, k -> new FaultSimulationResult())
                .addTestResult(result);
    }

    /**
     * Records a relation fault result (business rule violation).
     */
    public void recordRelationFault(String relationName, TestLevelSimulationResults result) {
        relationFaults
                .computeIfAbsent(relationName, k -> new FaultSimulationResult())
                .addTestResult(result);
    }

    /**
     * Returns the total number of contract fault types tested.
     */
    public int getContractFaultCount() {
        int count = 0;
        for (Map<String, FaultSimulationResult> fields : contractFaults.values()) {
            count += fields.size();
        }
        return count;
    }

    /**
     * Returns the number of contract faults that were caught.
     */
    public int getContractFaultsCaught() {
        int count = 0;
        for (Map<String, FaultSimulationResult> fields : contractFaults.values()) {
            for (FaultSimulationResult result : fields.values()) {
                if (result.isCaughtByAnyTest()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns the total number of relation faults tested.
     */
    public int getRelationFaultCount() {
        return relationFaults.size();
    }

    /**
     * Returns the number of relation faults that were caught.
     */
    public int getRelationFaultsCaught() {
        int count = 0;
        for (FaultSimulationResult result : relationFaults.values()) {
            if (result.isCaughtByAnyTest()) {
                count++;
            }
        }
        return count;
    }
}
