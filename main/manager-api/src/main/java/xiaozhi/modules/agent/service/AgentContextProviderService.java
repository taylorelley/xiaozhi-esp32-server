package xiaozhi.modules.agent.service;

import xiaozhi.common.service.BaseService;
import xiaozhi.modules.agent.entity.AgentContextProviderEntity;

public interface AgentContextProviderService extends BaseService<AgentContextProviderEntity> {
    /**
     * according toAgent IDgetcontextsourceconfiguration
     * @param agentId Agent ID
     * @return contextsourceconfigurationentity
     */
    AgentContextProviderEntity getByAgentId(String agentId);

    /**
     * saveorupdatecontextsourceconfiguration
     * @param entity entity
     */
    void saveOrUpdateByAgentId(AgentContextProviderEntity entity);

    /**
     * according toAgent IDdeletecontextsourceconfiguration
     * @param agentId Agent ID
     */
    void deleteByAgentId(String agentId);
}
