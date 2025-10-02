package metatest.injection;

import metatest.config.FaultCollection;

import java.util.Map;

public interface FaultStrategy {


    void apply(Map<String, Object> responseMap, String field);

    FaultCollection getFaultType();
}
