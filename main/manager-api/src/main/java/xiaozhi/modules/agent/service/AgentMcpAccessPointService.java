package xiaozhi.modules.agent.service;


import java.util.List;

/**
 * agentMcpendpointprocessservice
 *
 * @author zjy
 */
public interface AgentMcpAccessPointService {
    /**
     * getagent mcpendpointAddress
     * @param id agentid
     * @return mcpendpointAddress
     */
   String getAgentMcpAccessAddress(String id);

    /**
     * getagent mcpendpointalreadyhas tool list
     * @param id agentid
     * @return tool list
     */
   List<String> getAgentMcpToolsList(String id);
}
