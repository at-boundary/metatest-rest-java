package metatest.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import metatest.config.FaultCollection;
import metatest.config.SimulatorConfig;
import metatest.http.Request;
import metatest.http.Response;
import metatest.injection.FaultStrategy;
import metatest.injection.EmptyListStrategy;
import metatest.injection.EmptyStringStrategy;
import metatest.injection.MissingFieldStrategy;
import metatest.injection.NullFieldStrategy;
import metatest.report.FaultSimulationReport;
import metatest.report.TestLevelSimulationResults;
import metatest.utils.EndpointPatternNormalizer;
import org.aspectj.lang.ProceedingJoinPoint;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Runner {

    private static final Map<FaultCollection, FaultStrategy> FAULT_STRATEGIES;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final FaultSimulationReport REPORT = FaultSimulationReport.getInstance();
    private static final List<FaultCollection> ENABLED_FAULTS = SimulatorConfig.getEnabledFaults();

    static {
        Map<FaultCollection, FaultStrategy> strategies = new HashMap<>();
        strategies.put(FaultCollection.null_field, new NullFieldStrategy());
        strategies.put(FaultCollection.missing_field, new MissingFieldStrategy());
        strategies.put(FaultCollection.empty_list, new EmptyListStrategy());
        strategies.put(FaultCollection.empty_string, new EmptyStringStrategy());
        FAULT_STRATEGIES = Collections.unmodifiableMap(strategies);
    }

    private Runner() {}

    public static void executeTestWithSimulatedFaults(ProceedingJoinPoint joinPoint, TestContext context) throws Throwable {
        Response originalResponse = context.getOriginalResponse();
        Request originalRequest = context.getOriginalRequest();

        if (originalResponse == null || originalRequest == null) {
            System.err.println("[METATEST-WARN] Original request or response was not captured. Skipping fault simulation.");
            return;
        }

        String testName = joinPoint.getSignature().getName();
        String endpointPath = URI.create(originalRequest.getUrl()).getPath();
        String endpointPattern = EndpointPatternNormalizer.normalize(endpointPath);
        String requestBody = originalRequest.getBody();

        System.out.printf("%n[Metatest-Sim] === Starting simulations for test: '%s' on endpoint: '%s' (pattern: '%s') ===%n",
                testName, endpointPath, endpointPattern);
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            System.out.printf("[Metatest-Sim] Original request body: %s%n", requestBody);
        }

        // Check if we should simulate this response based on status code and content
        int statusCode = originalResponse.getStatusCode();
        Map<String, Object> responseMap = originalResponse.getResponseAsMap();

        if (!SimulatorConfig.shouldSimulateResponse(statusCode, responseMap)) {
            System.out.printf("[Metatest-Sim] === Skipping simulations for test: '%s' (status: %d) ===%n%n", testName, statusCode);
            return;
        }

        System.out.printf("[Metatest-Sim] Response status: %d (simulation will proceed)%n", statusCode);

        for (String field : originalResponse.getResponseAsMap().keySet()) {
            for (FaultCollection fault : ENABLED_FAULTS) {
                setFieldFault(context, field, fault);

                System.out.printf("  -> Rerunning test '%s' with fault: %s on field: '%s'%n", testName, fault, field);
                TestLevelSimulationResults testLevelResults = new TestLevelSimulationResults();
                testLevelResults.setTest(testName);

                try {
                    joinPoint.proceed(); // Re-run the test method
                    testLevelResults.setCaught(false);
                    System.err.printf("  [FAULT NOT DETECTED] Test '%s' passed for fault '%s' on field '%s'%n", testName, fault, field);
                } catch (Throwable t) {
                    testLevelResults.setCaught(true);
                    testLevelResults.setError(t.getMessage());
                    System.out.printf("  [FAULT DETECTED] Test '%s' failed as expected for fault '%s' on field '%s'%n", testName, fault, field);
                } finally {
                    context.clearSimulation();
                }

                REPORT.recordResult(endpointPattern, field, fault.name(), testLevelResults);
            }
        }
        System.out.printf("[Metatest-Sim] === Completed all simulations for test: '%s' ===%n%n", testName);
    }

    /**
     * Creates a simulated faulty response and sets it on the current TestContext.
     */
    private static void setFieldFault(TestContext context, String field, FaultCollection fault) {
        Response originalResponse = context.getOriginalResponse();
        if (originalResponse == null) {
            throw new IllegalStateException("Original response is null. Cannot create fault.");
        }

        FaultStrategy strategy = FAULT_STRATEGIES.get(fault);
        if (strategy == null) {
            return;
        }

        try {
            Map<String, Object> responseMap = new HashMap<>(originalResponse.getResponseAsMap());
            String originalBody = originalResponse.getBody();
            
            strategy.apply(responseMap, field);
            String faultyBody = OBJECT_MAPPER.writeValueAsString(responseMap);

            System.out.printf("    [FAULT-INJECTION] Original response body: %s%n", originalBody);
            System.out.printf("    [FAULT-INJECTION] Simulated response body: %s%n", faultyBody);

            Response simulatedResponse = originalResponse.withBody(faultyBody);
            context.setSimulatedResponse(simulatedResponse);

        } catch (IOException e) {
            System.err.println("Failed to create simulated response body for fault " + fault + ". Error: " + e.getMessage());
        }
    }
}