package metatest.schemacoverage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class EndpointCall {
    @JsonProperty("test")
    private String testName;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("url")
    private String url;

    @JsonProperty("headers")
    private Map<String, String> headers;

    @JsonProperty("body")
    private Object body;

    @JsonProperty("urlParameters")
    private Map<String, String> urlParameters;
}
