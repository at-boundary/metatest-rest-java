package metatest.schemacoverage;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CoverageEntry {
    private Map<String, Details> called = new HashMap<>();
    private Map<String, Details> notCalled = new HashMap<>();

}
