package io.antigen.ai.config;

import java.util.ArrayList;
import java.util.List;

public class GenerationConfig {

    public String spec;
    public String output_dir;
    public Integer max_retries;
    // Claude model passed to the CLI's --model flag (e.g. "claude-opus-4-8", "sonnet"). Null = let
    // the Claude CLI use its own configured default.
    public String model;
    // Minimum fraction of injected faults the generated suite must catch for the run to succeed
    // (0.0-1.0). Below 1.0 lets the loop accept structurally-uncatchable invariants (cross-field /
    // temporal) instead of chasing an impossible 100%. Null = use the engine default (1.0).
    public Double fault_detection_threshold;
    public Timeouts timeouts;
    public List<String> requirements = new ArrayList<>();

    public static class Timeouts {
        public Integer build;
        public Integer test;
        public Integer antigen;
        public Integer llm;
    }
}
