package metatest.core.interceptor;

import metatest.simulation.FaultSimulationReport;
import metatest.coverage.Collector;
import metatest.analytics.GapAnalyzer;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class GlobalTestExecutionListener implements TestExecutionListener {

    private static boolean executed = false;
    private final boolean runWithMetatest = Boolean.parseBoolean(System.getProperty("runWithMetatest"));
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (!executed && runWithMetatest) {
            executed = true;
            System.out.println("All tests completed - Sending results to API...");
//            FaultSimulationReport.getInstance().sendResultsToAPI();
            FaultSimulationReport.getInstance().createJSONReport();
            Collector.saveCoverageReport();
            GapAnalyzer.generateGapReport();
        }
    }
}