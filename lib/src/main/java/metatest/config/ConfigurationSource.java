package metatest.config;

import java.util.List;


public interface ConfigurationSource {

    List<FaultCollection> getEnabledFaults();


    boolean isEndpointExcluded(String endpoint);


    boolean isTestExcluded(String testName);


    String getSourceName();
}
