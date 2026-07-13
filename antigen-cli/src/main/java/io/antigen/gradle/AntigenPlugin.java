package io.antigen.gradle;

import io.antigen.ai.config.GenerationConfig;
import io.antigen.ai.config.GenerationConfigLoader;
import io.antigen.core.simulation.FaultSimulationReport;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.testing.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AntigenPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        AntigenExtension extension = project.getExtensions().create("antigen", AntigenExtension.class);

        // Configure the test task to forward Antigen system properties from the Gradle JVM
        // to the test JVM. This removes the need for consumers to do this manually.
        project.getTasks().withType(Test.class).configureEach(testTask -> {
            testTask.doFirst(t -> {
                String runWithAntigen = System.getProperty("runWithAntigen");
                if (runWithAntigen != null) {
                    testTask.jvmArgs("-DrunWithAntigen=" + runWithAntigen);
                }

                String reportPath = System.getProperty(FaultSimulationReport.REPORT_PATH_PROPERTY);
                if (reportPath != null) {
                    testTask.jvmArgs("-D" + FaultSimulationReport.REPORT_PATH_PROPERTY + "=" + reportPath);
                }

                if ("true".equals(runWithAntigen)) {
                    var testRuntimeClasspath = project.getConfigurations().findByName("testRuntimeClasspath");
                    if (testRuntimeClasspath != null) {
                        String agent = testRuntimeClasspath.getFiles().stream()
                                .filter(f -> f.getName().contains("aspectjweaver"))
                                .map(java.io.File::getAbsolutePath)
                                .findFirst().orElse(null);
                        if (agent != null) {
                            testTask.jvmArgs("-javaagent:" + agent);
                        }
                    }
                }
            });
        });

        project.getTasks().register("generateTests", JavaExec.class, task -> {
            task.setGroup("antigen");
            task.setDescription("Generate API tests using Antigen AI");
            task.getMainClass().set("io.antigen.ai.Antigen");

            task.doFirst(t -> {
                // Resolve antigen-cli (plus its transitive deps: picocli, jackson, engine, logback)
                // as a fresh detached configuration against the project's repositories. We deliberately
                // do NOT reuse the buildscript classpath: Gradle serves *instrumented* copies of
                // buildscript jars from its cache, and forking a JavaExec off those yields a classpath
                // that fails to resolve some classes (e.g. NoClassDefFoundError for a class only
                // referenced on the happy path). A detached configuration gives clean, original jars.
                // The CLI shells out to ./gradlew for build/test, so it does not need the consumer's
                // own (test) classes — the consumer build only has to apply the plugin.
                String cliCoords = "io.antigen:antigen-cli:" + antigenCliVersion();
                var cliDep = project.getDependencies().create(cliCoords);
                var cliClasspath = project.getConfigurations().detachedConfiguration(cliDep);
                task.setClasspath(cliClasspath);
                task.setArgs(buildArgs(project, extension));
            });
        });
    }

    // Version of antigen-cli to resolve for the generateTests classpath. Read from the plugin jar's
    // manifest (Implementation-Version, stamped by antigen-cli's build) so it always matches the
    // applied plugin; falls back to the current snapshot if the manifest is unavailable.
    private static final String FALLBACK_VERSION = "1.0.0-SNAPSHOT";

    private static String antigenCliVersion() {
        String v = AntigenPlugin.class.getPackage().getImplementationVersion();
        return (v != null && !v.isBlank()) ? v : FALLBACK_VERSION;
    }

    private List<String> buildArgs(Project project, AntigenExtension extension) {
        Path projectPath = project.getProjectDir().toPath();
        GenerationConfig fileConfig = GenerationConfigLoader.load(projectPath).orElse(new GenerationConfig());

        List<String> args = new ArrayList<>();
        args.add("generate");

        // spec: -Pspec > extension > generation/config.yml
        String spec = (String) project.findProperty("spec");
        if (spec == null || spec.isBlank()) spec = extension.getSpec();
        if (spec == null || spec.isBlank()) spec = fileConfig.spec;
        if (spec == null || spec.isBlank()) {
            throw new IllegalArgumentException(
                "No spec provided. Use -Pspec=path/to/openapi.yaml, set antigen { spec = '...' }, " +
                "or add spec: to antigen/generation/config.yml"
            );
        }
        args.add("--spec");
        args.add(spec);

        args.add("--project");
        args.add(projectPath.toString());

        // requirements: extension + config.yml (additive)
        for (String req : extension.getRequirements()) {
            args.add("--requirements");
            args.add(req);
        }
        if (fileConfig.requirements != null) {
            for (String req : fileConfig.requirements) {
                args.add("--requirements");
                args.add(req);
            }
        }

        // output_dir: -Poutput > config.yml > default
        String outputDir = (String) project.findProperty("output");
        if (outputDir == null || outputDir.isBlank()) outputDir = fileConfig.output_dir;
        if (outputDir == null || outputDir.isBlank()) outputDir = "src/test/java/generated";
        args.add("--output-dir");
        args.add(outputDir);

        // max_retries: extension > config.yml > default (5)
        int maxRetries = extension.getMaxRetries() > 0 ? extension.getMaxRetries()
                : (fileConfig.max_retries != null ? fileConfig.max_retries : 5);
        args.add("--max-retries");
        args.add(String.valueOf(maxRetries));

        // timeouts: extension > config.yml > defaults
        if (fileConfig.timeouts != null) {
            if (fileConfig.timeouts.build != null) {
                args.add("--timeout-build");
                args.add(String.valueOf(fileConfig.timeouts.build));
            }
            if (fileConfig.timeouts.test != null) {
                args.add("--timeout-test");
                args.add(String.valueOf(fileConfig.timeouts.test));
            }
            if (fileConfig.timeouts.antigen != null) {
                args.add("--timeout-antigen");
                args.add(String.valueOf(fileConfig.timeouts.antigen));
            }
        }

        if (extension.isVerbose()) {
            args.add("--verbose");
        }

        return args;
    }
}
