package metatest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class LocalConfigurationSource implements ConfigurationSource {

    private final SimulatorConfig config;
    private final List<Pattern> urlExcludePatterns;
    private final List<Pattern> endpointExcludePatterns;
    private final List<Pattern> testExcludePatterns;

    public LocalConfigurationSource() {
        this.config = loadConfig();
        this.urlExcludePatterns = compilePatterns(config.url != null ? config.url.exclude : null);
        this.endpointExcludePatterns = compilePatterns(config.endpoints != null ? config.endpoints.exclude : null);
        this.testExcludePatterns = compilePatterns(config.tests != null ? config.tests.exclude : null);
    }

    private SimulatorConfig loadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (is == null) {
                throw new RuntimeException("config.yml not found in classpath. " +
                        "For local configuration mode, config.yml must be present in src/main/resources/");
            }

            SimulatorConfig config = mapper.readValue(is, SimulatorConfig.class);
            System.out.println("Loaded Metatest configuration from config.yml");
            return config;

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse config.yml", e);
        }
    }

    private List<Pattern> compilePatterns(List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return new ArrayList<>();
        }

        List<Pattern> compiledPatterns = new ArrayList<>();
        for (String pattern : patterns) {
            // Convert glob-like patterns to regex
            // Example: '*/login*' becomes '.*\/login.*'
            String regex = pattern
                    .replace("*", ".*")
                    .replace("?", ".");
            compiledPatterns.add(Pattern.compile(regex));
        }
        return compiledPatterns;
    }

    @Override
    public List<FaultCollection> getEnabledFaults() {
        List<FaultCollection> enabledFaults = new ArrayList<>();

        if (config.faults == null) {
            System.out.println("No faults section found in config.yml");
            return enabledFaults;
        }

        if (config.faults.null_field != null && config.faults.null_field.enabled) {
            enabledFaults.add(FaultCollection.null_field);
        }
        if (config.faults.missing_field != null && config.faults.missing_field.enabled) {
            enabledFaults.add(FaultCollection.missing_field);
        }
        if (config.faults.empty_list != null && config.faults.empty_list.enabled) {
            enabledFaults.add(FaultCollection.empty_list);
        }
        if (config.faults.empty_string != null && config.faults.empty_string.enabled) {
            enabledFaults.add(FaultCollection.empty_string);
        }
        if (config.faults.invalid_value != null && config.faults.invalid_value.enabled) {
            enabledFaults.add(FaultCollection.invalid_value);
        }
        if (config.faults.http_method_change != null && config.faults.http_method_change.enabled) {
            enabledFaults.add(FaultCollection.http_method_change);
        }

        System.out.println("Enabled faults from config.yml: " + enabledFaults);
        return enabledFaults;
    }

    @Override
    public boolean isEndpointExcluded(String endpoint) {
        if (endpoint == null) {
            return false;
        }

        for (Pattern pattern : urlExcludePatterns) {
            if (pattern.matcher(endpoint).matches()) {
                return true;
            }
        }

        for (Pattern pattern : endpointExcludePatterns) {
            if (pattern.matcher(endpoint).matches()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isTestExcluded(String testName) {
        if (testName == null) {
            return false;
        }

        for (Pattern pattern : testExcludePatterns) {
            if (pattern.matcher(testName).matches()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getSourceName() {
        return "Local YAML";
    }
}
