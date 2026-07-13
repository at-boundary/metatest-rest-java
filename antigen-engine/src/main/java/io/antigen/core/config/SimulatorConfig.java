package io.antigen.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simulator configuration model, doubling as the loader for the single global
 * {@code antigen/simulation/config.yml}. That file is the base layer of the config cascade:
 * its {@code exclusions.endpoints} union into every test's exclusions, and its {@code settings}
 * / {@code simulation} gating are the fallback the per-class {@code .antigen.yml} overrides.
 *
 * <p>The global instance is loaded once from the classpath ({@link #global()}); when the file is
 * absent every accessor returns the same hardcoded defaults as before, so behaviour is unchanged
 * for consumers that don't ship a config.yml.
 */
@Data
public class SimulatorConfig {

    public String version;
    public Settings settings;
    public Map<String, Map<String, MethodInvariantsConfig>> endpoints = new HashMap<>();
    public Exclusions exclusions;
    public Simulation simulation;
    public Report report;

    // Legacy fields kept for YAML deserialization compatibility
    public Url url;
    public Tests tests;

    @Data
    public static class Settings {
        public String default_quantifier = "all";
        public boolean stop_on_first_catch = false;
    }

    @Data
    public static class Exclusions {
        public List<String> urls;
        public List<String> endpoints;
        public List<String> tests;
    }

    @Data
    public static class Simulation {
        public List<Integer> allowed_status_codes;
        public boolean only_success_responses = true;
        public boolean skip_collections_response = true;
        public int min_response_fields = 1;
        public List<String> skip_if_contains_fields;
        public MultipleEndpointsStrategy multiple_endpoints_strategy;
    }

    @Data
    public static class MultipleEndpointsStrategy {
        public boolean test_only_last_endpoint = false;
        public List<String> exclude_endpoints;
    }

    @Data
    public static class Report {
        public String format;
        public String output_path;
    }

    @Data
    public static class Url {
        public List<String> exclude;
    }

    @Data
    public static class Tests {
        public List<String> exclude;
    }

    // ── Global instance (antigen/simulation/config.yml) ──────────────────────

    private static final Simulation DEFAULT_SIMULATION = new Simulation();
    private static final Settings DEFAULT_SETTINGS = new Settings();
    private static final MultipleEndpointsStrategy DEFAULT_STRATEGY = new MultipleEndpointsStrategy();

    private static final String GLOBAL_RESOURCE = "antigen/simulation/config.yml";
    private static volatile SimulatorConfig global;

    /**
     * The global config loaded from {@code antigen/simulation/config.yml} on the classpath, cached
     * for the JVM lifetime. Never null — a missing or unparseable file yields an all-defaults
     * instance so accessors keep their historical behaviour.
     */
    public static SimulatorConfig global() {
        SimulatorConfig g = global;
        if (g == null) {
            synchronized (SimulatorConfig.class) {
                g = global;
                if (g == null) {
                    g = loadGlobal();
                    global = g;
                }
            }
        }
        return g;
    }

    private static SimulatorConfig loadGlobal() {
        ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        InputStream is = ctx != null ? ctx.getResourceAsStream(GLOBAL_RESOURCE) : null;
        if (is == null) is = SimulatorConfig.class.getClassLoader().getResourceAsStream(GLOBAL_RESOURCE);
        if (is == null) return new SimulatorConfig();
        try (InputStream stream = is) {
            SimulatorConfig cfg = new ObjectMapper(new YAMLFactory())
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(stream, SimulatorConfig.class);
            System.out.println("[Antigen] Loaded global simulation config: " + GLOBAL_RESOURCE);
            return cfg != null ? cfg : new SimulatorConfig();
        } catch (IOException e) {
            System.err.println("[Antigen] Failed to parse " + GLOBAL_RESOURCE + ": " + e.getMessage());
            return new SimulatorConfig();
        }
    }

    private Settings settings() { return settings != null ? settings : DEFAULT_SETTINGS; }
    private Simulation simulation() { return simulation != null ? simulation : DEFAULT_SIMULATION; }

    // ── Static accessors used at runtime (read the global instance) ──────────

    public static boolean shouldSimulateResponse(int statusCode, Map<String, Object> responseMap, String responseBody) {
        Simulation sim = global().simulation();
        if (sim.only_success_responses && (statusCode < 200 || statusCode >= 300)) {
            System.out.println("[Antigen-Sim] Skipping — non-success status: " + statusCode);
            return false;
        }
        if (sim.skip_collections_response && isCollectionResponse(responseBody)) {
            System.out.println("[Antigen-Sim] Skipping — response is a collection");
            return false;
        }
        if (responseMap == null || responseMap.size() < Math.max(1, sim.min_response_fields)) {
            System.out.println("[Antigen-Sim] Skipping — response has fewer than " + sim.min_response_fields + " field(s)");
            return false;
        }
        return true;
    }

    private static boolean isCollectionResponse(String body) {
        return body != null && body.trim().startsWith("[");
    }

    /** Global endpoint-exclusion globs (base layer; class/method exclusions union on top of these). */
    public static List<String> globalExcludedEndpointGlobs() {
        SimulatorConfig g = global();
        return (g.exclusions != null && g.exclusions.endpoints != null)
                ? g.exclusions.endpoints : List.of();
    }

    public static boolean isTestExcluded(String testName) {
        return false;
    }

    public static String getDefaultQuantifier() {
        String q = global().settings().default_quantifier;
        return q != null ? q : "all";
    }

    public static boolean isStopOnFirstCatchEnabled() {
        return global().settings().stop_on_first_catch;
    }

    public static MultipleEndpointsStrategy getMultipleEndpointsStrategy() {
        return DEFAULT_STRATEGY;
    }

    public static Map<String, Map<String, MethodInvariantsConfig>> getAllEndpointInvariants() {
        return new HashMap<>();
    }

    public static List<InvariantConfig> getInvariantsForEndpoint(String endpointPath, String httpMethod) {
        return new ArrayList<>();
    }

    public static boolean hasInvariants(String endpointPath, String httpMethod) {
        return false;
    }
}
