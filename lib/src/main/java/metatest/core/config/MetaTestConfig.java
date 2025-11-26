package metatest.core.config;

import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Data
public class MetaTestConfig {
    private static MetaTestConfig instance;
    private final String apiKey;
    private final String apiBaseUrl;
    private final String projectId;
    
    private MetaTestConfig() {
        Properties props = loadProperties();
        this.apiKey = getConfigValue(props, "metatest.api.key", "METATEST_API_KEY");
        this.apiBaseUrl = getConfigValue(props, "metatest.api.url", "METATEST_API_URL", "http://localhost:8080");
        this.projectId = getConfigValue(props, "metatest.project.id", "METATEST_PROJECT_ID");
    }
    
    public static synchronized MetaTestConfig getInstance() {
        if (instance == null) {
            instance = new MetaTestConfig();
        }
        return instance;
    }
    
    // For testing purposes - allows resetting the singleton
    public static synchronized void resetInstance() {
        instance = null;
    }
    
    private Properties loadProperties() {
        Properties props = new Properties();
        
        // Try to load from classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("metatest.properties")) {
            if (is != null) {
                props.load(is);
                System.out.println("Loaded Metatest configuration from metatest.properties");
            }
        } catch (IOException e) {
            // Properties file not found or not readable, continue with other methods
            System.out.println("No metatest.properties found, using system properties and environment variables");
        }
        
        return props;
    }
    
    private String getConfigValue(Properties props, String propertyName, String envVarName) {
        return getConfigValue(props, propertyName, envVarName, null);
    }
    
    private String getConfigValue(Properties props, String propertyName, String envVarName, String defaultValue) {
        // Priority: System property > Environment variable > Properties file > Default value
        String value = System.getProperty(propertyName);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        
        value = System.getenv(envVarName);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        
        value = props.getProperty(propertyName);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        
        return defaultValue;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public boolean hasProjectId() {
        return projectId != null && !projectId.isEmpty();
    }

    public boolean isApiConfigured() {
        return hasApiKey() && hasProjectId();
    }

    public String getFaultStrategiesUrl() {
        if (!isApiConfigured()) {
            throw new IllegalStateException("API is not configured. Cannot get fault strategies URL.");
        }
        return apiBaseUrl + "/api/v1/projects/" + projectId + "/fault-strategies/contract";
    }

    public String getSimulationResultsUrl() {
        if (!isApiConfigured()) {
            throw new IllegalStateException("API is not configured. Cannot get simulation results URL.");
        }
        return apiBaseUrl + "/api/v1/projects/" + projectId + "/simulation-results";
    }
    
    @Override
    public String toString() {
        return "MetatestConfig{" +
                "apiBaseUrl='" + apiBaseUrl + '\'' +
                ", projectId='" + projectId + '\'' +
                ", apiKey='" + (apiKey != null ? "CONFIGURED" : "NOT_SET") + '\'' +
                '}';
    }
}