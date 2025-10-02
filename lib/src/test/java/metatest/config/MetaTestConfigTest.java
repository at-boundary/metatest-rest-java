package metatest.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

public class MetaTestConfigTest {
    
    @BeforeEach
    public void setUp() {
        System.clearProperty("metatest.api.key");
        System.clearProperty("metatest.project.id");
        System.clearProperty("metatest.api.url");
        MetaTestConfig.resetInstance();
    }
    
    @AfterEach 
    public void tearDown() {
        System.clearProperty("metatest.api.key");
        System.clearProperty("metatest.project.id");
        System.clearProperty("metatest.api.url");
        MetaTestConfig.resetInstance();
    }
    
    @Test
    public void testSystemPropertyConfiguration() {
        System.setProperty("metatest.api.key", "mt_proj_secret123");
        System.setProperty("metatest.project.id", "550e8400-e29b-41d4-a716-446655440000");
        System.setProperty("metatest.api.url", "http://localhost:8080");
        
        MetaTestConfig config = MetaTestConfig.getInstance();
        
        assertEquals("mt_proj_secret123", config.getApiKey());
        assertEquals("550e8400-e29b-41d4-a716-446655440000", config.getProjectId());
        assertEquals("http://localhost:8080", config.getApiBaseUrl());
    }
    
    @Test
    public void testUrlGeneration() {
        System.setProperty("metatest.api.key", "mt_proj_secret123");
        System.setProperty("metatest.project.id", "550e8400-e29b-41d4-a716-446655440000");
        System.setProperty("metatest.api.url", "http://localhost:8080");
        
        MetaTestConfig config = MetaTestConfig.getInstance();
        
        String expectedFaultStrategiesUrl = "http://localhost:8080/api/v1/projects/550e8400-e29b-41d4-a716-446655440000/fault-strategies/contract";
        String expectedSimulationResultsUrl = "http://localhost:8080/api/v1/projects/550e8400-e29b-41d4-a716-446655440000/simulation-results";
        
        assertEquals(expectedFaultStrategiesUrl, config.getFaultStrategiesUrl());
        assertEquals(expectedSimulationResultsUrl, config.getSimulationResultsUrl());
    }
    
    @Test
    public void testDefaultApiUrl() {
        System.setProperty("metatest.api.key", "mt_proj_secret123");
        System.setProperty("metatest.project.id", "550e8400-e29b-41d4-a716-446655440000");

        MetaTestConfig config = MetaTestConfig.getInstance();
        
        assertEquals("http://localhost:8080", config.getApiBaseUrl());
    }
    
    @Test
    @Disabled
    public void testMissingApiKeyThrowsException() {
        System.setProperty("metatest.project.id", "550e8400-e29b-41d4-a716-446655440000");
        System.clearProperty("metatest.api.key");
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            MetaTestConfig.getInstance();
        });
        
        assertTrue(exception.getMessage().contains("Metatest API key not configured"),
                "Expected message about API key, but got: " + exception.getMessage());
    }
    
    @Test
    @Disabled
    public void testMissingProjectIdThrowsException() {
        System.setProperty("metatest.api.key", "mt_proj_test123");
        // Don't set project ID - explicitly clear it
        System.clearProperty("metatest.project.id");
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            MetaTestConfig.getInstance();
        });
        
        assertTrue(exception.getMessage().contains("Metatest project ID not configured"),
                "Expected message about project ID, but got: " + exception.getMessage());
    }
    
    @Test
    public void testToStringMasksApiKey() {
        System.setProperty("metatest.api.key", "mt_proj_secret123");
        System.setProperty("metatest.project.id", "550e8400-e29b-41d4-a716-446655440000");
        
        MetaTestConfig config = MetaTestConfig.getInstance();
        String configString = config.toString();
        
        assertFalse(configString.contains("mt_proj_secret123"));
        assertTrue(configString.contains("***CONFIGURED***"));
        assertTrue(configString.contains("550e8400-e29b-41d4-a716-446655440000"));
    }
}