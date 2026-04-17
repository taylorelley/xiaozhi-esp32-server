package xiaozhi.modules.config.service;

import java.util.Map;

public interface ConfigService {
    /**
     * getserviceconfiguration
     * 
     * @param isCache YesNocache
     * @return configurationinformation
     */
    Object getConfig(Boolean isCache);

    /**
     * getagentModel configuration
     * 
     * @param macAddress     MACAddress
     * @param selectedModule clientalreadyexample model
     * @return Model configurationinformation
     */
    Map<String, Object> getAgentModels(String macAddress, Map<String, String> selectedModule);
}