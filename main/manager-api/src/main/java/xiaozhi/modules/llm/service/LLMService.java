package xiaozhi.modules.llm.service;

/**
 * LLMserviceinterface
 * support多种largemodelcall
 */
public interface LLMService {

    /**
     * generateChat historysummary
     * 
     * @param conversation   conversationcontent
     * @param promptTemplate prompttemplate
     * @return summaryresult
     */
    String generateSummary(String conversation, String promptTemplate);

    /**
     * generateChat historysummary（usedefaultprompt）
     * 
     * @param conversation conversationcontent
     * @return summaryresult
     */
    String generateSummary(String conversation);

    /**
     * generateChat historysummary（specifiedModel ID）
     * 
     * @param conversation conversationcontent
     * @param modelId      Model ID
     * @return summaryresult
     */
    String generateSummaryWithModel(String conversation, String modelId);

    /**
     * generateChat historysummary（specifiedModel IDandprompttemplate）
     * 
     * @param conversation   conversationcontent
     * @param promptTemplate prompttemplate
     * @param modelId        Model ID
     * @return summaryresult
     */
    String generateSummary(String conversation, String promptTemplate, String modelId);

    /**
     * generateChat historysummary（containhistorymemory合and）
     * 
     * @param conversation   conversationcontent
     * @param historyMemory  historymemory
     * @param promptTemplate prompttemplate
     * @param modelId        Model ID
     * @return summaryresult
     */
    String generateSummaryWithHistory(String conversation, String historyMemory, String promptTemplate, String modelId);

    /**
     * checkserviceYesNoavailable
     * 
     * @return YesNoavailable
     */
    boolean isAvailable();

    /**
     * checkspecifiedmodel serviceYesNoavailable
     * 
     * @param modelId Model ID
     * @return YesNoavailable
     */
    boolean isAvailable(String modelId);

    /**
     * generatesessiontitle
     * 
     * @param conversation conversationcontent
     * @param modelId      Model ID
     * @return title（约15字）
     */
    String generateTitle(String conversation, String modelId);
}