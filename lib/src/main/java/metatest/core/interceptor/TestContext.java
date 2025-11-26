package metatest.core.interceptor;

import lombok.Data;
import metatest.http.Request;
import metatest.http.Response;

@Data
public class TestContext {

    private String testName;
    private Request originalRequest;
    private Response originalResponse;
    private Response simulatedResponse;

    public void clearSimulation() {
        this.simulatedResponse = null;
    }

}
