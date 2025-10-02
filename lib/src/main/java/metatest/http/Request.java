package metatest.http;

import java.util.Map;

public interface Request {

    String getUrl();
    Map<String, Object> getHeaders();
    String getBody();


}
