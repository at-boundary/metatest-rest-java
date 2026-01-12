package metatest.relation;

import lombok.Data;

/**
 * Represents a mutation to apply to a response field to violate a relation.
 * Used for fault simulation testing.
 */
@Data
public class Mutation {

    /**
     * Type of mutation
     */
    public enum MutationType {
        /** Set field to null */
        SET_NULL,
        /** Set field to a specific value */
        SET_VALUE,
        /** Remove field entirely */
        REMOVE_FIELD,
        /** Set field to empty string */
        SET_EMPTY_STRING,
        /** Set field to empty list */
        SET_EMPTY_LIST
    }

    /**
     * The relation this mutation is designed to violate
     */
    private String relationName;

    /**
     * The field path to mutate (dot notation)
     */
    private String field;

    /**
     * Type of mutation to apply
     */
    private MutationType type;

    /**
     * The value to set (for SET_VALUE mutations)
     */
    private Object value;

    /**
     * Original value before mutation (for reporting)
     */
    private Object originalValue;

    /**
     * Human-readable description of the mutation
     */
    private String description;

    /**
     * Create a SET_NULL mutation
     */
    public static Mutation setNull(String relationName, String field) {
        Mutation m = new Mutation();
        m.setRelationName(relationName);
        m.setField(field);
        m.setType(MutationType.SET_NULL);
        m.setValue(null);
        m.setDescription("Set " + field + " to null");
        return m;
    }

    /**
     * Create a SET_VALUE mutation
     */
    public static Mutation setValue(String relationName, String field, Object value) {
        Mutation m = new Mutation();
        m.setRelationName(relationName);
        m.setField(field);
        m.setType(MutationType.SET_VALUE);
        m.setValue(value);
        m.setDescription("Set " + field + " to " + value);
        return m;
    }

    /**
     * Create a SET_VALUE mutation with original value tracking
     */
    public static Mutation setValue(String relationName, String field, Object value, Object original) {
        Mutation m = setValue(relationName, field, value);
        m.setOriginalValue(original);
        return m;
    }

    /**
     * Create a REMOVE_FIELD mutation
     */
    public static Mutation removeField(String relationName, String field) {
        Mutation m = new Mutation();
        m.setRelationName(relationName);
        m.setField(field);
        m.setType(MutationType.REMOVE_FIELD);
        m.setDescription("Remove field " + field);
        return m;
    }

    /**
     * Create a SET_EMPTY_STRING mutation
     */
    public static Mutation setEmptyString(String relationName, String field) {
        Mutation m = new Mutation();
        m.setRelationName(relationName);
        m.setField(field);
        m.setType(MutationType.SET_EMPTY_STRING);
        m.setValue("");
        m.setDescription("Set " + field + " to empty string");
        return m;
    }

    /**
     * Create a SET_EMPTY_LIST mutation
     */
    public static Mutation setEmptyList(String relationName, String field) {
        Mutation m = new Mutation();
        m.setRelationName(relationName);
        m.setField(field);
        m.setType(MutationType.SET_EMPTY_LIST);
        m.setValue(java.util.Collections.emptyList());
        m.setDescription("Set " + field + " to empty list");
        return m;
    }

    @Override
    public String toString() {
        return "Mutation{" +
                "relation='" + relationName + '\'' +
                ", field='" + field + '\'' +
                ", type=" + type +
                ", value=" + value +
                '}';
    }
}
