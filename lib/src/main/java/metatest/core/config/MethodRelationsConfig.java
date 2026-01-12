package metatest.core.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for relations specific to an HTTP method on an endpoint.
 *
 * Example usage in config.yml:
 * <pre>
 * endpoints:
 *   /api/v1/orders/{order_id}:
 *     GET:
 *       relations:
 *         - name: positive_quantity
 *           field: quantity
 *           greater_than: 0
 * </pre>
 */
@Data
public class MethodRelationsConfig {

    /**
     * List of relation rules for this HTTP method
     */
    private List<RelationConfig> relations = new ArrayList<>();

    /**
     * Check if there are any relations configured
     */
    public boolean hasRelations() {
        return relations != null && !relations.isEmpty();
    }
}
