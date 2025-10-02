package metatest.schemacoverage;

import io.swagger.v3.oas.models.Operation;
import lombok.Data;

@Data
public class EndpointDefinition {
    private Operation operation;

    public EndpointDefinition(Operation operation) {
        this.operation = operation;
    }

}
