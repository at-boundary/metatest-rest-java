package metatest.schemacoverage;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class CollectorData {
    private volatile String dateCollected;
    private volatile String host;
    private final Map<String, Map<String, EndpointCall>> paths;

    public CollectorData() {
        this.paths = new ConcurrentHashMap<>();
    }


    public void setPaths(Map<String, Map<String, EndpointCall>> paths) {
        this.paths.clear();
        this.paths.putAll(paths);
    }
}
