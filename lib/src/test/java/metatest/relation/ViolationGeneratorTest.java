package metatest.relation;

import metatest.core.config.ConditionConfig;
import metatest.core.config.RelationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ViolationGenerator
 */
public class ViolationGeneratorTest {

    private ViolationGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new ViolationGenerator();
    }

    @Test
    public void testGenerateViolationForIsNotNull() {
        Map<String, Object> response = new HashMap<>();
        response.put("filled_at", "2024-01-15T10:30:00Z");

        RelationConfig relation = new RelationConfig();
        relation.setName("filled_at_required");
        relation.setField("filled_at");
        relation.setIsNotNull(true);

        List<Mutation> mutations = generator.generateViolations(relation, response);

        assertEquals(1, mutations.size());
        Mutation m = mutations.get(0);
        assertEquals("filled_at", m.getField());
        assertEquals(Mutation.MutationType.SET_NULL, m.getType());
        assertNull(m.getValue());
    }

    @Test
    public void testGenerateViolationForIsNotEmpty() {
        Map<String, Object> response = new HashMap<>();
        response.put("holder_name", "John Doe");

        RelationConfig relation = new RelationConfig();
        relation.setName("holder_name_required");
        relation.setField("holder_name");
        relation.setIsNotEmpty(true);

        List<Mutation> mutations = generator.generateViolations(relation, response);

        assertEquals(1, mutations.size());
        Mutation m = mutations.get(0);
        assertEquals("holder_name", m.getField());
        assertEquals(Mutation.MutationType.SET_EMPTY_STRING, m.getType());
        assertEquals("", m.getValue());
    }

    @Test
    public void testGenerateViolationForGreaterThan() {
        Map<String, Object> response = new HashMap<>();
        response.put("quantity", 10);

        RelationConfig relation = new RelationConfig();
        relation.setName("positive_quantity");
        relation.setField("quantity");
        relation.setGreaterThan(0);

        List<Mutation> mutations = generator.generateViolations(relation, response);

        // Should generate two mutations: exactly at threshold and below threshold
        assertEquals(2, mutations.size());

        // First mutation: exactly 0 (boundary)
        Mutation m1 = mutations.get(0);
        assertEquals("quantity", m1.getField());
        assertEquals(0.0, m1.getValue());

        // Second mutation: -1 (below threshold)
        Mutation m2 = mutations.get(1);
        assertEquals("quantity", m2.getField());
        assertEquals(-1.0, m2.getValue());
    }

    @Test
    public void testGenerateViolationForGreaterThanOrEqual() {
        Map<String, Object> response = new HashMap<>();
        response.put("balance", 100.0);

        RelationConfig relation = new RelationConfig();
        relation.setName("non_negative_balance");
        relation.setField("balance");
        relation.setGreaterThanOrEqual(0);

        List<Mutation> mutations = generator.generateViolations(relation, response);

        assertEquals(1, mutations.size());
        Mutation m = mutations.get(0);
        assertEquals("balance", m.getField());
        assertEquals(-1.0, m.getValue()); // Just below 0
    }

    @Test
    public void testGenerateViolationForIn() {
        Map<String, Object> response = new HashMap<>();
        response.put("order_type", "BUY");

        RelationConfig relation = new RelationConfig();
        relation.setName("valid_order_type");
        relation.setField("order_type");
        relation.setIn(Arrays.asList("BUY", "SELL"));

        List<Mutation> mutations = generator.generateViolations(relation, response);

        assertEquals(1, mutations.size());
        Mutation m = mutations.get(0);
        assertEquals("order_type", m.getField());
        assertEquals("INVALID_VALUE", m.getValue()); // Not in allowed list
    }

    @Test
    public void testGenerateViolationForEquals() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ACTIVE");

        RelationConfig relation = new RelationConfig();
        relation.setName("status_must_be_active");
        relation.setField("status");
        relation.setEquals("ACTIVE");

        List<Mutation> mutations = generator.generateViolations(relation, response);

        assertEquals(1, mutations.size());
        Mutation m = mutations.get(0);
        assertEquals("status", m.getField());
        assertEquals("ACTIVE_INVALID", m.getValue()); // Different from ACTIVE
    }

    @Test
    public void testGenerateViolationForConditionalRelation() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "FILLED");
        response.put("filled_at", "2024-01-15T10:30:00Z");

        // if status == FILLED then filled_at is_not_null
        RelationConfig relation = new RelationConfig();
        relation.setName("filled_order_has_timestamp");

        ConditionConfig ifCondition = new ConditionConfig();
        ifCondition.setField("status");
        ifCondition.setEquals("FILLED");
        relation.setIfCondition(ifCondition);

        ConditionConfig thenCondition = new ConditionConfig();
        thenCondition.setField("filled_at");
        thenCondition.setIsNotNull(true);
        relation.setThenCondition(thenCondition);

        List<Mutation> mutations = generator.generateViolations(relation, response);

        // Precondition (status=FILLED) is met, so generate violation for filled_at
        assertEquals(1, mutations.size());
        Mutation m = mutations.get(0);
        assertEquals("filled_at", m.getField());
        assertEquals(Mutation.MutationType.SET_NULL, m.getType());
    }

    @Test
    public void testNoViolationWhenPreconditionNotMet() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "PENDING");
        response.put("filled_at", null);

        // if status == FILLED then filled_at is_not_null
        RelationConfig relation = new RelationConfig();
        relation.setName("filled_order_has_timestamp");

        ConditionConfig ifCondition = new ConditionConfig();
        ifCondition.setField("status");
        ifCondition.setEquals("FILLED");
        relation.setIfCondition(ifCondition);

        ConditionConfig thenCondition = new ConditionConfig();
        thenCondition.setField("filled_at");
        thenCondition.setIsNotNull(true);
        relation.setThenCondition(thenCondition);

        List<Mutation> mutations = generator.generateViolations(relation, response);

        // Precondition not met, no violations should be generated
        assertTrue(mutations.isEmpty());
    }

    @Test
    public void testMutationDescription() {
        Mutation m = Mutation.setNull("test_relation", "test_field");
        assertEquals("Set test_field to null", m.getDescription());

        Mutation m2 = Mutation.setValue("test_relation", "price", 99.99);
        assertEquals("Set price to 99.99", m2.getDescription());
    }
}
