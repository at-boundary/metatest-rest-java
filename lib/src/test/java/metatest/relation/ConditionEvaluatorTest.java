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
 * Tests for ConditionEvaluator
 */
public class ConditionEvaluatorTest {

    private ConditionEvaluator evaluator;

    @BeforeEach
    public void setUp() {
        evaluator = new ConditionEvaluator();
    }

    // ==================== Simple Field Tests ====================

    @Test
    public void testEqualsOperator() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ACTIVE");

        RelationConfig relation = new RelationConfig();
        relation.setField("status");
        relation.setEquals("ACTIVE");

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testEqualsOperatorFails() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "INACTIVE");

        RelationConfig relation = new RelationConfig();
        relation.setField("status");
        relation.setEquals("ACTIVE");

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertFalse(result.isSatisfied());
        assertEquals("INACTIVE", result.getActualValue());
    }

    @Test
    public void testGreaterThanOperator() {
        Map<String, Object> response = new HashMap<>();
        response.put("quantity", 10);

        RelationConfig relation = new RelationConfig();
        relation.setField("quantity");
        relation.setGreaterThan(0);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testGreaterThanOperatorFails() {
        Map<String, Object> response = new HashMap<>();
        response.put("quantity", 0);

        RelationConfig relation = new RelationConfig();
        relation.setField("quantity");
        relation.setGreaterThan(0);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertFalse(result.isSatisfied());
    }

    @Test
    public void testGreaterThanOrEqualOperator() {
        Map<String, Object> response = new HashMap<>();
        response.put("balance", 0);

        RelationConfig relation = new RelationConfig();
        relation.setField("balance");
        relation.setGreaterThanOrEqual(0);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testLessThanOrEqualOperator() {
        Map<String, Object> response = new HashMap<>();
        response.put("created_at", "2024-01-01");
        response.put("updated_at", "2024-01-15");

        RelationConfig relation = new RelationConfig();
        relation.setField("created_at");
        relation.setLessThanOrEqual("$.updated_at"); // Field reference

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testInOperator() {
        Map<String, Object> response = new HashMap<>();
        response.put("order_type", "BUY");

        RelationConfig relation = new RelationConfig();
        relation.setField("order_type");
        relation.setIn(Arrays.asList("BUY", "SELL"));

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testInOperatorFails() {
        Map<String, Object> response = new HashMap<>();
        response.put("order_type", "INVALID");

        RelationConfig relation = new RelationConfig();
        relation.setField("order_type");
        relation.setIn(Arrays.asList("BUY", "SELL"));

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertFalse(result.isSatisfied());
    }

    @Test
    public void testIsNotNullOperator() {
        Map<String, Object> response = new HashMap<>();
        response.put("filled_at", "2024-01-15T10:30:00Z");

        RelationConfig relation = new RelationConfig();
        relation.setField("filled_at");
        relation.setIsNotNull(true);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testIsNotNullOperatorFails() {
        Map<String, Object> response = new HashMap<>();
        response.put("filled_at", null);

        RelationConfig relation = new RelationConfig();
        relation.setField("filled_at");
        relation.setIsNotNull(true);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertFalse(result.isSatisfied());
    }

    @Test
    public void testIsNullOperator() {
        Map<String, Object> response = new HashMap<>();
        response.put("filled_at", null);

        RelationConfig relation = new RelationConfig();
        relation.setField("filled_at");
        relation.setIsNull(true);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testIsNotEmptyOperator() {
        Map<String, Object> response = new HashMap<>();
        response.put("holder_name", "John Doe");

        RelationConfig relation = new RelationConfig();
        relation.setField("holder_name");
        relation.setIsNotEmpty(true);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testIsNotEmptyOperatorFails() {
        Map<String, Object> response = new HashMap<>();
        response.put("holder_name", "");

        RelationConfig relation = new RelationConfig();
        relation.setField("holder_name");
        relation.setIsNotEmpty(true);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertFalse(result.isSatisfied());
    }

    // ==================== Conditional Relation Tests ====================

    @Test
    public void testConditionalRelation_PreconditionMet() {
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

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testConditionalRelation_PreconditionNotMet() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "PENDING");
        response.put("filled_at", null);

        // if status == FILLED then filled_at is_not_null
        // Since status is PENDING, the relation is skipped
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

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied()); // Skipped, so satisfied
        assertTrue(result.getMessage().contains("Skipped"));
    }

    @Test
    public void testConditionalRelation_ThenFails() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "FILLED");
        response.put("filled_at", null); // Missing timestamp!

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

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertFalse(result.isSatisfied()); // Precondition met but assertion failed
    }

    // ==================== Nested Field Tests ====================

    @Test
    public void testNestedField() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John");
        response.put("user", user);

        RelationConfig relation = new RelationConfig();
        relation.setField("user.name");
        relation.setEquals("John");

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    // ==================== Numeric Type Coercion Tests ====================

    @Test
    public void testNumericTypeCoercion() {
        Map<String, Object> response = new HashMap<>();
        response.put("quantity", 10); // Integer

        RelationConfig relation = new RelationConfig();
        relation.setField("quantity");
        relation.setGreaterThan(5.0); // Double comparison

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }

    @Test
    public void testStringNumericComparison() {
        Map<String, Object> response = new HashMap<>();
        response.put("price", "99.99");

        RelationConfig relation = new RelationConfig();
        relation.setField("price");
        relation.setGreaterThan(50);

        ConditionEvaluator.EvaluationResult result = evaluator.evaluate(relation, response);
        assertTrue(result.isSatisfied());
    }
}
