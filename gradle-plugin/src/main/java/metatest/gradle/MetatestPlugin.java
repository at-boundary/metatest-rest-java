package metatest.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.testing.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gradle plugin for Metatest that automatically configures AspectJ weaving for tests.
 *
 * Usage in build.gradle.kts:
 *
 * plugins {
 *     id("io.metatest") version "1.0.0"
 * }
 *
 * metatest {
 *     enabled = true  // default: controlled by -DrunWithMetatest system property
 *     apiKey = "your_api_key"  // optional
 *     projectId = "your_project_id"  // optional
 *     apiUrl = "http://localhost:8080"  // optional
 * }
 */
public class MetatestPlugin implements Plugin<Project> {

    private static final Logger logger = LoggerFactory.getLogger(MetatestPlugin.class);

    @Override
    public void apply(Project project) {
        // Create the extension
        MetatestExtension extension = project.getExtensions().create("metatest", MetatestExtension.class);

        // Configure test tasks after project evaluation
        project.afterEvaluate(p -> {
            configureTestTasks(p, extension);
        });
    }

    private void configureTestTasks(Project project, MetatestExtension extension) {
        project.getTasks().withType(Test.class).configureEach(test -> {
            // Check if metatest should be enabled
            boolean runWithMetatest = shouldEnableMetatest(extension);

            if (!runWithMetatest) {
                logger.info("[Metatest] Skipping configuration (not enabled). Use -DrunWithMetatest=true to enable.");
                return;
            }

            // Find the aspectjweaver jar
            File aspectjAgent = findAspectjWeaver(project);

            if (aspectjAgent == null) {
                logger.warn("[Metatest] Could not find aspectjweaver in classpath. " +
                           "Make sure 'io.metatest:metatest' is in your dependencies.");
                return;
            }

            logger.info("[Metatest] Configuring test task: {} with AspectJ weaver: {}",
                       test.getName(), aspectjAgent.getAbsolutePath());

            // Configure JVM arguments
            List<String> jvmArgs = new ArrayList<>(test.getJvmArgs());

            // Add memory settings if not already present
            if (jvmArgs.stream().noneMatch(arg -> arg.startsWith("-Xmx"))) {
                jvmArgs.add("-Xmx2g");
            }
            if (jvmArgs.stream().noneMatch(arg -> arg.startsWith("-Xms"))) {
                jvmArgs.add("-Xms512m");
            }

            // Add AspectJ javaagent
            jvmArgs.add("-javaagent:" + aspectjAgent.getAbsolutePath());

            // Add metatest system property
            jvmArgs.add("-DrunWithMetatest=true");

            // Add optional configuration properties
            if (extension.getApiKey() != null && !extension.getApiKey().isEmpty()) {
                jvmArgs.add("-Dmetatest.api.key=" + extension.getApiKey());
            }
            if (extension.getProjectId() != null && !extension.getProjectId().isEmpty()) {
                jvmArgs.add("-Dmetatest.project.id=" + extension.getProjectId());
            }
            if (extension.getApiUrl() != null && !extension.getApiUrl().isEmpty()) {
                jvmArgs.add("-Dmetatest.api.url=" + extension.getApiUrl());
            }

            test.setJvmArgs(jvmArgs);

            logger.info("[Metatest] Test task configured successfully");
        });
    }

    private boolean shouldEnableMetatest(MetatestExtension extension) {
        // First check the extension's enabled property
        if (extension.getEnabled() != null) {
            return extension.getEnabled();
        }

        // Fall back to system property
        String sysProp = System.getProperty("runWithMetatest");
        return "true".equalsIgnoreCase(sysProp);
    }

    private File findAspectjWeaver(Project project) {
        // Try to find in runtimeClasspath
        Configuration runtimeClasspath = project.getConfigurations().findByName("runtimeClasspath");
        if (runtimeClasspath != null) {
            for (File file : runtimeClasspath.getResolvedConfiguration().getResolvedArtifacts()
                    .stream()
                    .map(artifact -> artifact.getFile())
                    .toList()) {
                if (file.getName().contains("aspectjweaver")) {
                    return file;
                }
            }
        }

        // Try testRuntimeClasspath
        Configuration testRuntimeClasspath = project.getConfigurations().findByName("testRuntimeClasspath");
        if (testRuntimeClasspath != null) {
            for (File file : testRuntimeClasspath.getResolvedConfiguration().getResolvedArtifacts()
                    .stream()
                    .map(artifact -> artifact.getFile())
                    .toList()) {
                if (file.getName().contains("aspectjweaver")) {
                    return file;
                }
            }
        }

        return null;
    }
}
