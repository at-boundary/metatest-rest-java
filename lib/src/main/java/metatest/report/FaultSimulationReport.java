package metatest.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import metatest.api.FaultStrategyApiClient;
import metatest.api.dto.SubmitSimulationResultsRequest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FaultSimulationReport {
    private static final FaultSimulationReport INSTANCE = new FaultSimulationReport();
    private static final String DEFAULT_REPORT_PATH = "fault_simulation_report.json";

    private final Map<String, Map<String, Map<String, List<TestLevelSimulationResults>>>> report = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final FaultStrategyApiClient apiClient;
    private LocalDateTime executionStartTime;

    private FaultSimulationReport() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.apiClient = new FaultStrategyApiClient();
        this.executionStartTime = LocalDateTime.now(); // Set when first result is recorded
    }

    public static FaultSimulationReport getInstance() {
        return INSTANCE;
    }


    public void recordResult(String endpoint, String field, String faultType, TestLevelSimulationResults result) {
        if (endpoint == null || field == null || faultType == null || result == null) {
            System.err.println("[METATEST-WARN] Attempted to record a result with null data. Skipping.");
            return;
        }

        report.computeIfAbsent(endpoint, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(field, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(faultType, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(result);
    }

    public void sendResultsToAPI() {
        try {
            SubmitSimulationResultsRequest request = convertToApiRequest();
            
            apiClient.submitSimulationResults(request);
            
            System.out.println("Successfully sent simulation results to API");
            
        } catch (Exception e) {
            System.err.println("Failed to send results to API: " + e.getMessage());
            e.printStackTrace();
            
            System.out.println("Falling back to JSON file...");
            createJSONReport();
        }
    }
    
    public void createJSONReport() {
        try {
            File reportFile = new File(DEFAULT_REPORT_PATH);
            if (reportFile.getParentFile() != null) {
                reportFile.getParentFile().mkdirs();
            }
            objectMapper.writeValue(reportFile, report);
            System.out.println("Saving fault simulation report to JSON file: " + reportFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save report: " + e.getMessage());
        }
    }
    
    private SubmitSimulationResultsRequest convertToApiRequest() {
        SubmitSimulationResultsRequest request = new SubmitSimulationResultsRequest();
        
        request.setTestSuiteName(extractTestSuiteName());
        request.setStrategyId(getStrategyIdFromAPI()); // Get the strategy ID that was used
        request.setExecutionStartTime(executionStartTime);
        request.setExecutionEndTime(LocalDateTime.now());
        request.setAgentVersion("1.0.0-dev");
        
        Map<String, SubmitSimulationResultsRequest.EndpointResults> convertedResults = new HashMap<>();
        
        for (Map.Entry<String, Map<String, Map<String, List<TestLevelSimulationResults>>>> endpointEntry : report.entrySet()) {
            String endpoint = endpointEntry.getKey();
            Map<String, Map<String, List<TestLevelSimulationResults>>> fieldsData = endpointEntry.getValue();
            
            SubmitSimulationResultsRequest.EndpointResults endpointResults = new SubmitSimulationResultsRequest.EndpointResults();
            Map<String, SubmitSimulationResultsRequest.FieldResults> fieldResultsMap = new HashMap<>();
            
            for (Map.Entry<String, Map<String, List<TestLevelSimulationResults>>> fieldEntry : fieldsData.entrySet()) {
                String fieldName = fieldEntry.getKey();
                Map<String, List<TestLevelSimulationResults>> faultTypesData = fieldEntry.getValue();
                
                SubmitSimulationResultsRequest.FieldResults fieldResults = new SubmitSimulationResultsRequest.FieldResults();
                
                for (Map.Entry<String, List<TestLevelSimulationResults>> faultTypeEntry : faultTypesData.entrySet()) {
                    String faultType = faultTypeEntry.getKey();
                    List<TestLevelSimulationResults> testResults = faultTypeEntry.getValue();
                    
                    SubmitSimulationResultsRequest.FaultTypeResult faultTypeResult = convertFaultTypeResult(testResults);
                    
                    switch (faultType) {
                        case "null_field":
                            fieldResults.setNullField(faultTypeResult);
                            break;
                        case "missing_field":
                            fieldResults.setMissingField(faultTypeResult);
                            break;
                        case "empty_string":
                            fieldResults.setEmptyString(faultTypeResult);
                            break;
                        case "empty_list":
                            fieldResults.setEmptyList(faultTypeResult);
                            break;
                        case "invalid_value":
                            fieldResults.setInvalidValue(faultTypeResult);
                            break;
                    }
                }
                
                fieldResultsMap.put(fieldName, fieldResults);
            }
            
            endpointResults.setFields(fieldResultsMap);
            convertedResults.put(endpoint, endpointResults);
        }
        
        request.setResults(convertedResults);
        
        SubmitSimulationResultsRequest.SimulationSummary summary = calculateSummary(convertedResults);
        request.setSummary(summary);
        
        return request;
    }
    
    private SubmitSimulationResultsRequest.FaultTypeResult convertFaultTypeResult(List<TestLevelSimulationResults> testResults) {
        SubmitSimulationResultsRequest.FaultTypeResult faultTypeResult = new SubmitSimulationResultsRequest.FaultTypeResult();
        
        List<SubmitSimulationResultsRequest.TestResult> convertedTestResults = new ArrayList<>();
        boolean caughtByAny = false;
        
        for (TestLevelSimulationResults testResult : testResults) {
            SubmitSimulationResultsRequest.TestResult apiTestResult = new SubmitSimulationResultsRequest.TestResult();
            apiTestResult.setTest(testResult.getTest());
            apiTestResult.setCaught(testResult.isCaught());
            apiTestResult.setError(testResult.getError());
            
            if (testResult.isCaught()) {
                caughtByAny = true;
            }
            
            convertedTestResults.add(apiTestResult);
        }
        
        faultTypeResult.setCaughtByAny(caughtByAny);
        faultTypeResult.setResults(convertedTestResults);
        
        return faultTypeResult;
    }
    
    private SubmitSimulationResultsRequest.SimulationSummary calculateSummary(Map<String, SubmitSimulationResultsRequest.EndpointResults> results) {
        int totalFaultTypes = 0;
        int faultTypesCaught = 0;
        int totalTestExecutions = 0;
        int successfulTestExecutions = 0;
        
        for (SubmitSimulationResultsRequest.EndpointResults endpointResults : results.values()) {
            for (SubmitSimulationResultsRequest.FieldResults fieldResults : endpointResults.getFields().values()) {
                
                if (fieldResults.getNullField() != null) {
                    totalFaultTypes++;
                    if (fieldResults.getNullField().getCaughtByAny()) {
                        faultTypesCaught++;
                    }
                    totalTestExecutions += fieldResults.getNullField().getResults().size();
                    successfulTestExecutions += (int) fieldResults.getNullField().getResults().stream()
                            .mapToInt(result -> result.getCaught() ? 1 : 0).sum();
                }
                
                if (fieldResults.getMissingField() != null) {
                    totalFaultTypes++;
                    if (fieldResults.getMissingField().getCaughtByAny()) {
                        faultTypesCaught++;
                    }
                    totalTestExecutions += fieldResults.getMissingField().getResults().size();
                    successfulTestExecutions += (int) fieldResults.getMissingField().getResults().stream()
                            .mapToInt(result -> result.getCaught() ? 1 : 0).sum();
                }
                
                if (fieldResults.getEmptyString() != null) {
                    totalFaultTypes++;
                    if (fieldResults.getEmptyString().getCaughtByAny()) {
                        faultTypesCaught++;
                    }
                    totalTestExecutions += fieldResults.getEmptyString().getResults().size();
                    successfulTestExecutions += (int) fieldResults.getEmptyString().getResults().stream()
                            .mapToInt(result -> result.getCaught() ? 1 : 0).sum();
                }
                
                if (fieldResults.getEmptyList() != null) {
                    totalFaultTypes++;
                    if (fieldResults.getEmptyList().getCaughtByAny()) {
                        faultTypesCaught++;
                    }
                    totalTestExecutions += fieldResults.getEmptyList().getResults().size();
                    successfulTestExecutions += (int) fieldResults.getEmptyList().getResults().stream()
                            .mapToInt(result -> result.getCaught() ? 1 : 0).sum();
                }
                
                if (fieldResults.getInvalidValue() != null) {
                    totalFaultTypes++;
                    if (fieldResults.getInvalidValue().getCaughtByAny()) {
                        faultTypesCaught++;
                    }
                    totalTestExecutions += fieldResults.getInvalidValue().getResults().size();
                    successfulTestExecutions += (int) fieldResults.getInvalidValue().getResults().stream()
                            .mapToInt(result -> result.getCaught() ? 1 : 0).sum();
                }
            }
        }
        
        SubmitSimulationResultsRequest.SimulationSummary summary = new SubmitSimulationResultsRequest.SimulationSummary();
        summary.setTotalFaultTypes(totalFaultTypes);
        summary.setFaultTypesCaught(faultTypesCaught);
        summary.setFaultTypesMissed(totalFaultTypes - faultTypesCaught);
        summary.setFaultCoverageScore(totalFaultTypes > 0 ? (double) faultTypesCaught / totalFaultTypes : 0.0);
        summary.setTotalTestExecutions(totalTestExecutions);
        summary.setTestExecutionSuccessRate(totalTestExecutions > 0 ? (double) successfulTestExecutions / totalTestExecutions : 0.0);
        
        return summary;
    }
    
    private String extractTestSuiteName() {
        for (Map<String, Map<String, List<TestLevelSimulationResults>>> endpointData : report.values()) {
            for (Map<String, List<TestLevelSimulationResults>> fieldData : endpointData.values()) {
                for (List<TestLevelSimulationResults> testResults : fieldData.values()) {
                    if (!testResults.isEmpty()) {
                        String fullTestName = testResults.get(0).getTest();
                        // Extract class name from method name (e.g., "testCreatePayment" -> "PaymentTest")
                        return fullTestName.replaceAll("test.*", "") + "Test";
                    }
                }
            }
        }
        return "UnknownTestSuite";
    }
    
    private UUID getStrategyIdFromAPI() {

        try {
            var response = apiClient.getContractFaultStrategies();
            return response.getStrategies().stream()
                    .filter(s -> s.getEnabled())
                    .findFirst()
                    .map(s -> s.getId())
                    .orElseThrow(() -> new RuntimeException("No enabled strategy found"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get strategy ID", e);
        }
    }
}