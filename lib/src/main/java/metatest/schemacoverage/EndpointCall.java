package metatest.schemacoverage;
import lombok.Data;

import java.util.Map;

@Data
public class EndpointCall {
    private Map<String, String> headers;
    private Object body;
    private Map<String, String> urlParameters;

}
