package xiaozhi.modules.agent.service;

/**
 * agentChat historysummaryserviceinterface
 */
public interface AgentChatSummaryService {

    /**
     * according toSession IDgenerateChat historysummaryandsavetoAgent memory
     * 
     * @param sessionId Session ID
     * @return saveresult
     */
    boolean generateAndSaveChatSummary(String sessionId);

    /**
     * according toSession IDgeneratechattitleandsave
     *
     * @param sessionId Session ID
     * @return YesNosuccess
     */
    boolean generateAndSaveChatTitle(String sessionId);
}