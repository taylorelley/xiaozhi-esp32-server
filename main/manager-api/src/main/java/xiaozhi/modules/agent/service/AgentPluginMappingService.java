package xiaozhi.modules.agent.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import xiaozhi.modules.agent.entity.AgentPluginMapping;

/**
 * @description for table【ai_agent_plugin_mapping(Agentandplugin unique mapping table)】 datalibraryoperationService
 * @createDate 2025-05-25 22:33:17
 */
public interface AgentPluginMappingService extends IService<AgentPluginMapping> {

    /**
     * according toagentidgetpluginparameter
     * 
     * @param agentId
     * @return
     */
    List<AgentPluginMapping> agentPluginParamsByAgentId(String agentId);

    /**
     * according toagentiddeletepluginparameter
     * 
     * @param agentId
     */
    void deleteByAgentId(String agentId);
}
