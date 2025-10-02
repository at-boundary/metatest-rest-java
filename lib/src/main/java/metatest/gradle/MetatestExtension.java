package metatest.gradle;

import lombok.Data;

/**
 * Gradle extension for configuring Metatest
 * Usage in build.gradle:
 * 
 * metatest {
 *     apiKey = "mt_proj_your_api_key_here"
 *     projectId = "your-project-uuid-here"  
 *     apiUrl = "http://localhost:8080"
 * }
 */

@Data
public class MetatestExtension {
    private String apiKey;
    private String projectId;
    private String apiUrl = "http://localhost:8080";
}