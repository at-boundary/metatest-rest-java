package metatest.http;

import java.util.Map;

public interface Response {

    String getUrl();
    Map<String, Object> getHeaders();
    String getBody();

    void setBody(String body); // Consider deprecating this in favor of withBody

    /**
     * Creates a new Response instance with a modified body.
     * @param newBody The new body for the response.
     * @return A new, immutable Response object.
     */
    Response withBody(String newBody);

    Map<String, Object> getResponseAsMap();
}