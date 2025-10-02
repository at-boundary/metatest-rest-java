package metatest.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SimulatorConfig {
    public Faults faults;
    public Semantics semantics;
    public SemanticFaults semanticFaults;
    public QualityAnalysis qualityAnalysis;
    public Url url;
    public Endpoints endpoints;
    public Tests tests;
    public Report report;

    @Data
    public static class Faults {
        static class Fault {
            public boolean enabled;
        }

        static class DelayInjection extends Fault {
            public int delay_ms;
        }

        public Fault null_field;
        public Fault missing_field;
        public Fault empty_list;
        public Fault empty_string;
        public Fault invalid_data_type;
        public Fault invalid_value;
        public Fault http_method_change;
        public Fault status_code_change;
        public DelayInjection delay_injection;
    }

    @Data
    public static class Semantics {
        public boolean enabled;
        public IntentAnalysis intent_analysis;
        public BusinessLogic business_logic;
        public Completeness completeness;
        public EdgeCases edge_cases;
        public TestLogic test_logic;
        public Mutations mutations;
        public QualityScoring quality_scoring;
    }

    @Data
    public static class IntentAnalysis {
        public boolean enabled;
        public boolean analyze_assertions;
        public boolean analyze_test_description;
        public boolean extract_business_rules;
    }

    @Data
    public static class BusinessLogic {
        public boolean enabled;
        public boolean validate_data_consistency;
        public boolean validate_state_transitions;
        public boolean validate_authorization;
        public boolean validate_data_integrity;
    }

    @Data
    public static class Completeness {
        public boolean enabled;
        public boolean detect_missing_scenarios;
        public boolean calculate_coverage_percentage;
        public boolean suggest_additional_tests;
    }

    @Data
    public static class EdgeCases {
        public boolean enabled;
        public boolean boundary_conditions;
        public boolean special_characters;
        public boolean null_handling;
        public boolean empty_handling;
        public boolean large_data_handling;
    }

    @Data
    public static class TestLogic {
        public boolean enabled;
        public boolean detect_hardcoded_values;
        public boolean check_incomplete_assertions;
        public boolean validate_test_isolation;
        public boolean detect_race_conditions;
    }

    @Data
    public static class Mutations {
        public boolean enabled;
        public boolean business_logic_mutations;
        public boolean data_relationship_mutations;
        public boolean authorization_mutations;
    }

    @Data
    public static class QualityScoring {
        public boolean enabled;
        public double semantic_coverage_threshold;
        public double edge_case_coverage_threshold;
        public double business_logic_coverage_threshold;
        public double maintainability_threshold;
        public double reliability_threshold;
    }

    @Data
    public static class SemanticFaults {
        public boolean enabled;
        public StatusCodes status_codes;
        public FieldValues field_values;
        public Performance performance;
        public BusinessLogicFaults business_logic;
        public DataRelationships data_relationships;
    }

    @Data
    public static class StatusCodes {
        public boolean enabled;
        public List<Integer> codes;
    }
    @Data
    public static class FieldValues {
        public boolean enabled;
        public List<String> invalid_values;
    }

    @Data
    public static class Performance {
        public boolean enabled;
        public List<Integer> delays_ms;
    }

    @Data
    public static class BusinessLogicFaults {
        public boolean enabled;
        public boolean reverse_order_status;
        public boolean swap_user_roles;
        public boolean invert_payment_amount;
    }

    @Data
    public static class DataRelationships {
        public boolean enabled;
        public boolean swap_parent_child;
        public boolean break_foreign_key;
    }

    @Data
    public static class QualityAnalysis {
        public boolean enabled;
        public AntiPatterns anti_patterns;
        public Recommendations recommendations;
    }

    @Data
    public static class AntiPatterns {
        public boolean enabled;
        public boolean hardcoded_values;
        public boolean incomplete_assertions;
        public boolean missing_cleanup;
        public boolean race_conditions;
    }

    @Data
    public static class Recommendations {
        public boolean enabled;
        public boolean suggest_semantic_assertions;
        public boolean suggest_edge_case_tests;
        public boolean suggest_missing_scenarios;
        public boolean suggest_improvements;
    }

    @Data
    public static class Url {
        public List<String> exclude;
    }

    @Data
    public static class Endpoints {
        public List<String> exclude;
    }

    @Data
    public static class Tests {
        public List<String> exclude;
    }

    @Data
    public static class Report {
        public String format;
        public String output_path;
        public SemanticReport semantic_report;
    }

    @Data
    public static class SemanticReport {
        public boolean enabled;
        public boolean include_quality_score;
        public boolean include_recommendations;
        public boolean include_business_logic_report;
        public boolean include_completeness_report;
        public boolean include_edge_case_report;
    }

    private static final ConfigurationSource configSource = initializeConfigurationSource();

    private static ConfigurationSource initializeConfigurationSource() {
        String configSourceType = System.getProperty("metatest.config.source");

        if (configSourceType == null) {
            configSourceType = System.getenv("METATEST_CONFIG_SOURCE");
        }

        ConfigurationSource source;

        if ("local".equalsIgnoreCase(configSourceType)) {
            System.out.println("Using LOCAL configuration source (explicitly set via metatest.config.source)");
            source = new LocalConfigurationSource();
        } else if ("api".equalsIgnoreCase(configSourceType)) {
            System.out.println("Using API configuration source (explicitly set via metatest.config.source)");
            source = new ApiConfigurationSource();
        } else {
            // Auto-detect based on API configuration
            MetaTestConfig metaTestConfig = MetaTestConfig.getInstance();
            if (metaTestConfig.isApiConfigured()) {
                System.out.println("Using API configuration source (auto-detected - API key present)");
                source = new ApiConfigurationSource();
            } else {
                System.out.println("Using LOCAL configuration source (auto-detected - no API key)");
                source = new LocalConfigurationSource();
            }
        }

        System.out.println("Configuration source initialized: " + source.getSourceName());
        return source;
    }

    public static List<FaultCollection> getEnabledFaults(){
        return configSource.getEnabledFaults();
    }

    // TODO
    public static boolean isSemanticsEnabled() {
        return false; // Disabled for now, will be implemented with business-rules and semantic endpoints
    }

    public static boolean isIntentAnalysisEnabled() {
        return false;
    }

    public static boolean isBusinessLogicValidationEnabled() {
        return false;
    }

    public static boolean isCompletenessAnalysisEnabled() {
        return false;
    }

    public static boolean isEdgeCaseDetectionEnabled() {
        return false;
    }

    public static boolean isTestLogicAnalysisEnabled() {
        return false;
    }

    public static boolean isSemanticMutationsEnabled() {
        return false;
    }

    public static boolean isQualityScoringEnabled() {
        return false;
    }

    public static boolean isSemanticFaultsEnabled() {
        return false;
    }

    public static boolean isQualityAnalysisEnabled() {
        return false;
    }

    public static List<Integer> getSemanticStatusCodes() {
        return new ArrayList<>();
    }

    public static List<String> getSemanticInvalidValues() {
        return new ArrayList<>();
    }

    public static List<Integer> getSemanticDelays() {
        return new ArrayList<>();
    }

    public static double getSemanticCoverageThreshold() {
        return 0.7; // default
    }

    public static double getEdgeCaseCoverageThreshold() {
        return 0.5; // default
    }

    public static boolean isEndpointExcluded(String endpoint){
        return configSource.isEndpointExcluded(endpoint);
    }

    public static boolean isTestExcluded(String testName){
        return configSource.isTestExcluded(testName);
    }
}
