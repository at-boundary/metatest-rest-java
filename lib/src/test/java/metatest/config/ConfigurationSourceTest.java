package metatest.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationSourceTest {

    @BeforeEach
    public void setUp() {
        MetaTestConfig.resetInstance();
        System.clearProperty("metatest.config.source");
        System.clearProperty("metatest.api.key");
        System.clearProperty("metatest.project.id");
    }

    @Test
    public void testLocalConfigurationSource() {
        LocalConfigurationSource localConfig = new LocalConfigurationSource();

        assertEquals("Local YAML", localConfig.getSourceName());

        List<FaultCollection> enabledFaults = localConfig.getEnabledFaults();
        assertNotNull(enabledFaults);
        assertTrue(enabledFaults.contains(FaultCollection.null_field),
                "null_field should be enabled in test config.yml");
        assertTrue(enabledFaults.contains(FaultCollection.missing_field),
                "missing_field should be enabled in test config.yml");
        assertTrue(enabledFaults.contains(FaultCollection.invalid_value),
                "invalid_value should be enabled in test config.yml");
        assertFalse(enabledFaults.contains(FaultCollection.empty_list),
                "empty_list should be disabled in test config.yml");
        assertFalse(enabledFaults.contains(FaultCollection.empty_string),
                "empty_string should be disabled in test config.yml");

    }

    @Test
    public void testLocalConfigurationSourceExclusions() {
        LocalConfigurationSource localConfig = new LocalConfigurationSource();


        assertTrue(localConfig.isEndpointExcluded("/api/login"),
                "Endpoints matching */login* should be excluded");
        assertTrue(localConfig.isEndpointExcluded("/auth/validate"),
                "Endpoints matching */auth/* should be excluded");
        assertFalse(localConfig.isEndpointExcluded("/api/users"),
                "Regular endpoints should not be excluded");

        assertTrue(localConfig.isTestExcluded("UserLoginTest"),
                "Tests matching *LoginTest* should be excluded");
        assertFalse(localConfig.isTestExcluded("UserRegistrationTest"),
                "Regular tests should not be excluded");
    }

    @Test
    public void testConfigurationSourceInterface() {
        ConfigurationSource localConfig = new LocalConfigurationSource();

        assertNotNull(localConfig.getEnabledFaults());
        assertNotNull(localConfig.getSourceName());
        assertFalse(localConfig.isEndpointExcluded("/api/test"));
        assertFalse(localConfig.isTestExcluded("TestClass"));

        System.out.println("ConfigurationSource interface implemented correctly");
    }

    @Test
    public void testMetaTestConfigOptionalApiKey() {
        MetaTestConfig config = MetaTestConfig.getInstance();

        assertNotNull(config);
        assertFalse(config.isApiConfigured(),
                "API should not be configured when no API key is present");
        assertFalse(config.hasApiKey());
        assertFalse(config.hasProjectId());

        System.out.println("MetaTestConfig works without API key (for local mode)");
    }

    @Test
    public void testMetaTestConfigWithApiKey() {
        System.setProperty("metatest.api.key", "mt_proj_test123");
        System.setProperty("metatest.project.id", "test-project-id");

        MetaTestConfig config = MetaTestConfig.getInstance();

        assertNotNull(config);
        assertTrue(config.isApiConfigured(),
                "API should be configured when API key and project ID are present");
        assertTrue(config.hasApiKey());
        assertTrue(config.hasProjectId());
        assertEquals("mt_proj_test123", config.getApiKey());
        assertEquals("test-project-id", config.getProjectId());
    }

    @Test
    public void testMetaTestConfigUrlGenerationFailsWithoutApiConfig() {
        MetaTestConfig config = MetaTestConfig.getInstance();

        assertThrows(IllegalStateException.class, () -> config.getFaultStrategiesUrl(),
                "Should throw when getting fault strategies URL without API config");
        assertThrows(IllegalStateException.class, () -> config.getSimulationResultsUrl());
    }
}
