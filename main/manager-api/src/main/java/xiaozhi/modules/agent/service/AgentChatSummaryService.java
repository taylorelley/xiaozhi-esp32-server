package xiaozhi.modules.agent.service;

/**
 * agentChat historysummaryserviceinterface
 */
public interface AgentChatSummaryService {

    /**
     * according toSession IDgenerateChat historysummary并savetoAgent memory
     * 
     * @param sessionId Session ID
     * @return saveresult
     */
    boolean generateAndSaveChatSummary(String sessionId);

    /**
     * according toSession IDgeneratechattitle并save
     *
     * @param sessionId Session ID
     * @return YesNosuccess
     */
    boolean generateAndSaveChatTitle(String sessionId);
}