package metatest.gradle;

import lombok.Data;

/**
 * Gradle extension for configuring Metatest
 * Usage in build.gradle.kts:
 *
 * metatest {
 *     enabled = true  // Optional: defaults to -DrunWithMetatest system property
 *     apiKey = "mt_proj_your_api_key_here"  // Optional: for API mode
 *     projectId = "your-project-uuid-here"  // Optional: for API mode
 *     apiUrl = "http://localhost:8080"  // Optional: defaults to http://localhost:8080
 * }
 */

@Data
public class MetatestExtension {
    /**
     * Enable/disable Metatest. If null, falls back to -DrunWithMetatest system property.
     */
    private Boolean enabled;

    /**
     * API key for Metatest API integration (optional, for API mode)
     */
    private String apiKey;

    /**
     * Project ID for Metatest API integration (optional, for API mode)
     */
    private String projectId;

    /**
     * API URL for Metatest API integration (optional, defaults to http://localhost:8080)
     */
    private String apiUrl = "http://localhost:8080";
}