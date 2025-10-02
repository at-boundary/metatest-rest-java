package metatest.report;

import lombok.Data;

@Data
public class TestLevelSimulationResults {

    String test;
    boolean caught;
    String error;
}
