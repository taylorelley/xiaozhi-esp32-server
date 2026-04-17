package xiaozhi.modules.agent.service.biz;

import xiaozhi.modules.agent.dto.AgentChatHistoryReportDTO;

/**
 * Agent chat historybusiness逻辑layer
 *
 * @author Goody
 * @version 1.0, 2025/4/30
 * @since 1.0.0
 */
public interface AgentChatHistoryBizService {

    /**
     * chat reportingmethod
     *
     * @param agentChatHistoryReportDTO containchat reporting所需information 输入object
     *                                  For example：deviceMACAddress、File type、contentetc.
     * @return uploadresult，truerepresentssuccess，falserepresentsfailed
     */
    Boolean report(AgentChatHistoryReportDTO agentChatHistoryReportDTO);
}
