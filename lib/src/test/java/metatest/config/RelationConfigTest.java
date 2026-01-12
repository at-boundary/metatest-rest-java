package metatest.config;

import metatest.core.config.LocalConfigurationSource;
import metatest.core.config.RelationConfig;
import metatest.core.config.SimulatorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for relation config parsing from config.yml
 */
public class RelationConfigTest {

    private LocalConfigurationSource configSource;

    @BeforeEach
    public void setUp() {
        configSource = new LocalConfigurationSource();
    }

    @Test
    public void testRelationConfigParsing() {
        SimulatorConfig config = configSource.getConfig();

        assertNotNull(config);
        assertEquals("1.0", config.getVersion());
        assertNotNull(config.getEndpoints());
        assertFalse(config.getEndpoints().isEmpty(), "Endpoints should not be empty");
    }

    @Test
    public void testGetRelationsForEndpoint() {
        List<RelationConfig> relations = SimulatorConfig.getRelationsForEndpoint(
                "/api/v1/orders/{order_id}", "GET");

        assertNotNull(relations);
        assertFalse(relations.isEmpty(), "Should have relations for /api/v1/orders/{order_id} GET");
        assertEquals(2, relations.size(), "Should have 2 relations");

        // Check first relation
        RelationConfig positiveQuantity = relations.stream()
                .filter(r -> "positive_quantity".equals(r.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(positiveQuantity, "Should have positive_quantity relation");
        assertEquals("quantity", positiveQuantity.getField());
        assertEquals(0, ((Number) positiveQuantity.getGreaterThan()).intValue());

        // Check second relation
        RelationConfig validStatus = relations.stream()
                .filter(r -> "valid_status".equals(r.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(validStatus, "Should have valid_status relation");
        assertEquals("status", validStatus.getField());
        assertNotNull(validStatus.getIn());
        assertEquals(3, validStatus.getIn().size());
        assertTrue(validStatus.getIn().contains("PENDING"));
        assertTrue(validStatus.getIn().contains("FILLED"));
        assertTrue(validStatus.getIn().contains("REJECTED"));
    }

    @Test
    public void testNonExistentEndpoint() {
        List<RelationConfig> relations = SimulatorConfig.getRelationsForEndpoint(
                "/api/v1/nonexistent", "GET");

        assertNotNull(relations);
        assertTrue(relations.isEmpty(), "Should return empty list for non-existent endpoint");
    }

    @Test
    public void testHasRelations() {
        assertTrue(SimulatorConfig.hasRelations("/api/v1/orders/{order_id}", "GET"));
        assertFalse(SimulatorConfig.hasRelations("/api/v1/nonexistent", "GET"));
    }

    @Test
    public void testDefaultQuantifier() {
        String quantifier = SimulatorConfig.getDefaultQuantifier();
        assertEquals("all", quantifier);
    }

    @Test
    public void testRelationConfigIsUnconditional() {
        List<RelationConfig> relations = SimulatorConfig.getRelationsForEndpoint(
                "/api/v1/orders/{order_id}", "GET");

        RelationConfig positiveQuantity = relations.stream()
                .filter(r -> "positive_quantity".equals(r.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(positiveQuantity);
        assertTrue(positiveQuantity.isUnconditional(), "positive_quantity should be unconditional");
        assertFalse(positiveQuantity.isConditional(), "positive_quantity should not be conditional");
    }
}
