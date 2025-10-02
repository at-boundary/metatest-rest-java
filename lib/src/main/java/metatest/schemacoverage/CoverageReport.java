package metatest.schemacoverage;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CoverageReport {
    private String dateGenerated;
    private String host;
    private Map<String, CoverageEntry> called = new HashMap<>();

}
