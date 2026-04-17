package xiaozhi.modules.agent.service.biz;

import xiaozhi.modules.agent.dto.AgentChatHistoryReportDTO;

/**
 * Agent chat historybusinesslogiclayer
 *
 * @author Goody
 * @version 1.0, 2025/4/30
 * @since 1.0.0
 */
public interface AgentChatHistoryBizService {

    /**
     * chat reportingmethod
     *
     * @param agentChatHistoryReportDTO containchat reportingrequiredinformation inputobject
     *                                  For example：deviceMACAddress、File type、contentetc.
     * @return uploadresult，truerepresentssuccess，falserepresentsfailed
     */
    Boolean report(AgentChatHistoryReportDTO agentChatHistoryReportDTO);
}
