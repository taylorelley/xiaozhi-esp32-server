package xiaozhi.modules.agent.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.RequiredArgsConstructor;
import xiaozhi.common.constant.Constant;
import xiaozhi.modules.agent.dto.AgentChatHistoryDTO;
import xiaozhi.modules.agent.dto.AgentChatSummaryDTO;
import xiaozhi.modules.agent.dto.AgentMemoryDTO;
import xiaozhi.modules.agent.dto.AgentUpdateDTO;
import xiaozhi.modules.agent.entity.AgentChatHistoryEntity;
import xiaozhi.modules.agent.service.AgentChatHistoryService;
import xiaozhi.modules.agent.service.AgentChatSummaryService;
import xiaozhi.modules.agent.service.AgentChatTitleService;
import xiaozhi.modules.agent.service.AgentService;
import xiaozhi.modules.agent.vo.AgentInfoVO;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.llm.service.LLMService;
import xiaozhi.modules.model.entity.ModelConfigEntity;
import xiaozhi.modules.model.service.ModelConfigService;

/**
 * agentChat historysummaryserviceimplementclass
 * implementPythonendmem_local_short.py summarylogic
 */
@Service
@RequiredArgsConstructor
public class AgentChatSummaryServiceImpl implements AgentChatSummaryService {

    private static final Logger log = LoggerFactory.getLogger(AgentChatSummaryServiceImpl.class);

    private final AgentChatHistoryService agentChatHistoryService;
    private final AgentService agentService;
    private final AgentChatTitleService agentChatTitleService;
    private final DeviceService deviceService;
    private final LLMService llmService;
    private final ModelConfigService modelConfigService;

    // summaryrulethenconstant
    private static final int MAX_SUMMARY_LENGTH = 1800; // mostlargesummarylength
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);
    private static final Pattern DEVICE_CONTROL_PATTERN = Pattern.compile("devicecontrol|deviceoperation|controldevice|Device status",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern WEATHER_PATTERN = Pattern.compile("day|warm|humid|rainfall|object", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("date|time|starperiod|month|year", Pattern.CASE_INSENSITIVE);

    private AgentChatSummaryDTO generateChatSummary(String sessionId) {
        try {
            System.out.println("startgeneratesession " + sessionId + "  Chat historysummary");

            // 1. according tosessionIdgetChat history
            List<AgentChatHistoryDTO> chatHistory = getChatHistoryBySessionId(sessionId);
            if (chatHistory == null || chatHistory.isEmpty()) {
                return new AgentChatSummaryDTO(sessionId, "not foundthissession Chat history");
            }

            // 2. getAgent information
            String agentId = getAgentIdFromSession(sessionId, chatHistory);
            if (StringUtils.isBlank(agentId)) {
                return new AgentChatSummaryDTO(sessionId, "unable togetAgent information");
            }

            // 3. extractrelatedkeyconversationcontent
            List<String> meaningfulMessages = extractMeaningfulMessages(chatHistory);
            if (meaningfulMessages.isEmpty()) {
                return new AgentChatSummaryDTO(sessionId, "novalid conversationcontentcansummary");
            }

            // 4. generate summary（generateSummaryFromMessagesmethodalreadycontainlengthlimitlogic）
            String summary = generateSummaryFromMessages(meaningfulMessages, agentId);

            log.info("successgeneratesession {}  Chat historysummary，length: {} charactersymbol", sessionId, summary.length());
            return new AgentChatSummaryDTO(sessionId, agentId, summary);

        } catch (Exception e) {
            log.error("generatesession {}  Chat historysummarywhenoccurerror: {}", sessionId, e.getMessage());
            return new AgentChatSummaryDTO(sessionId, "generate summarywhenoccurerror: " + e.getMessage());
        }
    }

    @Override
    public boolean generateAndSaveChatSummary(String sessionId) {
        try {
            DeviceEntity device = getDeviceBySessionId(sessionId);
            if (device == null) {
                log.info("not foundandsession {} associated device", sessionId);
                return false;
            }

            String agentId = device.getAgentId();
            String memModelId = agentService.getAgentById(agentId).getMemModelId();

            if (memModelId == null || memModelId.equals(Constant.MEMORY_MEM_REPORT_ONLY)) {
                log.info("session {} useonlyreportChat historymode，skipmemorysummary", sessionId);
                return true;
            }

            boolean shouldSummarizeMemory = !memModelId.equals(Constant.MEMORY_NO_MEM)
                    && !memModelId.equals(Constant.MEMORY_MEM0AI)
                    && !memModelId.equals(Constant.MEMORY_POWERMEM);

            if (shouldSummarizeMemory) {
                AgentChatSummaryDTO summaryDTO = generateChatSummary(sessionId);
                if (summaryDTO.isSuccess()) {
                    agentService.updateAgentById(agentId, new AgentUpdateDTO() {
                        {
                            setSummaryMemory(summaryDTO.getSummary());
                        }
                    });
                    log.info("successsavesession {}  Chat historysummarytoagent {}", sessionId, agentId);
                } else {
                    log.info("generate summaryfailed: {}", summaryDTO.getErrorMessage());
                }
            } else {
                log.info("session {} use {} mode，skipmemorysummary", sessionId, memModelId);
            }

            return true;

        } catch (Exception e) {
            log.error("savesession {}  Chat historysummarywhenoccurerror: {}", sessionId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean generateAndSaveChatTitle(String sessionId) {
        try {
            // automaticgetagentId
            String agentId = findAgentIdBySessionId(sessionId);
            if (StringUtils.isBlank(agentId)) {
                log.warn("session {} unable togetAgent information，skiptitlegenerate", sessionId);
                return false;
            }

            List<AgentChatHistoryDTO> chatHistory = getChatHistoryBySessionId(sessionId);
            if (chatHistory == null || chatHistory.isEmpty()) {
                return false;
            }

            List<String> meaningfulMessages = extractMeaningfulMessages(chatHistory);
            if (meaningfulMessages.isEmpty()) {
                return false;
            }

            StringBuilder conversation = new StringBuilder();
            for (int i = 0; i < meaningfulMessages.size(); i++) {
                conversation.append("message").append(i + 1).append(": ").append(meaningfulMessages.get(i)).append("\n");
            }

            String slmModelId = getSlmModelId(agentId);
            String title = llmService.generateTitle(conversation.toString(), slmModelId);

            if (StringUtils.isNotBlank(title)) {
                agentChatTitleService.saveOrUpdateTitle(sessionId, title);
                log.info("successsavesession {}  title: {}", sessionId, title);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("generatesession {}  titlewhenoccurerror: {}", sessionId, e.getMessage());
            return false;
        }
    }

    private String getSlmModelId(String agentId) {
        try {
            if (StringUtils.isBlank(agentId)) {
                return null;
            }

            AgentInfoVO agentInfo = agentService.getAgentById(agentId);
            if (agentInfo == null) {
                return null;
            }

            String slmModelId = agentInfo.getSlmModelId();
            if (StringUtils.isNotBlank(slmModelId)) {
                log.info("session {} useSLMmodel: {}", agentId, slmModelId);
                return slmModelId;
            }

            ModelConfigEntity defaultLlmConfig = getDefaultLLMConfig();
            if (defaultLlmConfig != null) {
                log.info("session {} usedefaultLLMmodel: {}", agentId, defaultLlmConfig.getId());
                return defaultLlmConfig.getId();
            }

            String llmModelId = agentInfo.getLlmModelId();
            log.info("session {} useLLMmodel(mostfinalreturnback): {}", agentId, llmModelId);
            return llmModelId;
        } catch (Exception e) {
            log.error("getagentslmModel IDfailed，agentId: {}, error: {}", agentId, e.getMessage());
            return null;
        }
    }

    private ModelConfigEntity getDefaultLLMConfig() {
        try {
            List<ModelConfigEntity> llmConfigs = modelConfigService.getEnabledModelsByType("LLM");
            if (llmConfigs == null || llmConfigs.isEmpty()) {
                return null;
            }

            for (ModelConfigEntity config : llmConfigs) {
                if (config.getIsDefault() != null && config.getIsDefault() == 1) {
                    return config;
                }
            }

            return llmConfigs.get(0);
        } catch (Exception e) {
            log.error("getdefaultLLMconfigurationfailed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * according toSession IDgetChat history
     */
    private List<AgentChatHistoryDTO> getChatHistoryBySessionId(String sessionId) {
        try {
            // thisinneedaccording tosessionIdgetChat history
            // bytocurrenthasinterfaceneedagentId，Isneedfirstfindtoassociated agentId
            String agentId = findAgentIdBySessionId(sessionId);
            if (StringUtils.isBlank(agentId)) {
                return null;
            }
            return agentChatHistoryService.getChatHistoryBySessionId(agentId, sessionId);
        } catch (Exception e) {
            log.error("getsession {}  Chat historyfailed: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * according toSession IDfindassociated Agent ID
     */
    private String findAgentIdBySessionId(String sessionId) {
        try {
            // querythissession no.oneitemsrecordgetagentId
            QueryWrapper<AgentChatHistoryEntity> wrapper = new QueryWrapper<>();
            wrapper.select("agent_id")
                    .eq("session_id", sessionId)
                    .last("LIMIT 1");

            AgentChatHistoryEntity entity = agentChatHistoryService.getOne(wrapper);
            return entity != null ? entity.getAgentId() : null;
        } catch (Exception e) {
            log.error("according toSession ID {} findAgent IDfailed: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * fromsessiongetAgent ID
     */
    private String getAgentIdFromSession(String sessionId, List<AgentChatHistoryDTO> chatHistory) {
        // directlyfromdatalibraryqueryAgent ID
        return findAgentIdBySessionId(sessionId);
    }

    /**
     * extracthasmeaning conversationcontent（onlyextractusermessage，excludeAIreturnre-）
     */
    private List<String> extractMeaningfulMessages(List<AgentChatHistoryDTO> chatHistory) {
        List<String> meaningfulMessages = new ArrayList<>();

        for (AgentChatHistoryDTO message : chatHistory) {
            // onlyprocessusermessage（chatType = 1）
            if (message.getChatType() != null && message.getChatType() == 1) {
                String content = extractContentFromMessage(message);
                if (isMeaningfulMessage(content)) {
                    meaningfulMessages.add(content);
                }
            }
        }

        return meaningfulMessages;
    }

    /**
     * frommessageextractcontent（processJSONformat）
     */
    private String extractContentFromMessage(AgentChatHistoryDTO message) {
        String content = message.getContent();
        if (StringUtils.isBlank(content)) {
            return "";
        }

        // processJSONformatcontent（andbeforeendChatHistoryDialog.vuelogicconsistent）
        Matcher matcher = JSON_PATTERN.matcher(content);
        if (matcher.find()) {
            String jsonContent = matcher.group();
            // simpleprocess：extractJSON textcontent
            return extractTextFromJson(jsonContent);
        }

        return content;
    }

    /**
     * fromJSONextracttextcontent
     */
    private String extractTextFromJson(String jsonContent) {
        // simpleprocess：extract"content"field value
        Pattern contentPattern = Pattern.compile("\"content\"\s*:\s*\"([^\"]*)\"");
        Matcher matcher = contentPattern.matcher(jsonContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return jsonContent;
    }

    /**
     * determineYesNoashasmeaning message
     */
    private boolean isMeaningfulMessage(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }

        // excludedevicecontrolinformation
        if (DEVICE_CONTROL_PATTERN.matcher(content).find()) {
            return false;
        }

        // excludedatedayetc.norelatedcontent
        if (WEATHER_PATTERN.matcher(content).find() || DATE_PATTERN.matcher(content).find()) {
            return false;
        }

        // excludeshort message
        return content.length() >= 5;
    }

    /**
     * frommessagegenerate summary
     */
    private String generateSummaryFromMessages(List<String> messages, String agentId) {
        if (messages.isEmpty()) {
            return "thistimesconversationcontentfewer，noneedsummary re-need toinformation。";
        }

        // buildcomplete conversationcontent
        StringBuilder conversation = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            conversation.append("message").append(i + 1).append(": ").append(messages.get(i)).append("\n");
        }

        try {
            // get currentagent historymemory
            String historyMemory = getCurrentAgentMemory(agentId);

            // callLLMserviceperformintelligentcansummary，transferdeliveragentIdtogetexact Model configuration
            String summary = callJavaLLMForSummaryWithHistory(conversation.toString(), historyMemory, agentId);

            // shouldusesummaryrulethen：limitmostlargelength
            if (summary.length() > MAX_SUMMARY_LENGTH) {
                summary = summary.substring(0, MAX_SUMMARY_LENGTH) + "...";
            }

            return summary;
        } catch (Exception e) {
            log.error("callJavaendLLMservicefailed: {}", e.getMessage());
            throw new RuntimeException("LLMserviceunavailable，unable togeneratechatsummary");
        }
    }

    /**
     * get currentagent historymemory
     */
    private String getCurrentAgentMemory(String agentId) {
        try {
            if (StringUtils.isBlank(agentId)) {
                return null;
            }

            // getAgent information
            AgentInfoVO agentInfo = agentService.getAgentById(agentId);
            if (agentInfo == null) {
                return null;
            }

            // returnagent currentsummary memory
            return agentInfo.getSummaryMemory();
        } catch (Exception e) {
            log.error("getagenthistorymemoryfailed，agentId: {}, error: {}", agentId, e.getMessage());
            return null;
        }
    }

    /**
     * callJavaendLLMserviceperformintelligentcansummary（supporthistorymemorymergeand）
     */
    private String callJavaLLMForSummaryWithHistory(String conversation, String historyMemory, String agentId) {
        try {
            String modelId = getSlmModelId(agentId);

            if (StringUtils.isBlank(modelId)) {
                log.info("not foundSLMmodel，usedefaultLLMservice");
                return llmService.generateSummaryWithHistory(conversation, historyMemory, null, null);
            }

            String summary = llmService.generateSummaryWithHistory(conversation, historyMemory, null, modelId);

            if (StringUtils.isNotBlank(summary) && !summary.equals("servicetemporarilyunavailable") && !summary.equals("summarygeneratefailed")) {
                return summary;
            }

            throw new RuntimeException("JavaendLLMservicereturnexception: " + summary);

        } catch (Exception e) {
            log.error("callJavaendLLMserviceexception，agentId: {}, error: {}", agentId, e.getMessage());
            throw e;
        }
    }

    /**
     * callJavaendLLMserviceperformintelligentcansummary
     */
    private String callJavaLLMForSummary(String conversation, String agentId) {
        try {
            String modelId = getSlmModelId(agentId);

            if (StringUtils.isBlank(modelId)) {
                log.info("not foundSLMmodel，usedefaultLLMservice");
                return llmService.generateSummary(conversation);
            }

            String summary = llmService.generateSummaryWithModel(conversation, modelId);

            if (StringUtils.isNotBlank(summary) && !summary.equals("servicetemporarilyunavailable") && !summary.equals("summarygeneratefailed")) {
                return summary;
            }

            throw new RuntimeException("JavaendLLMservicereturnexception: " + summary);

        } catch (Exception e) {
            log.error("callJavaendLLMserviceexception，agentId: {}, error: {}", agentId, e.getMessage());
            throw e;
        }
    }

    /**
     * getmemorysummary LLMModel ID
     */
    private String getMemorySummaryModelId(String agentId) {
        try {
            if (StringUtils.isBlank(agentId)) {
                return null;
            }

            // getAgent information
            AgentInfoVO agentInfo = agentService.getAgentById(agentId);
            if (agentInfo == null) {
                return null;
            }

            // getagent memoryModel ID
            String memModelId = agentInfo.getMemModelId();
            if (StringUtils.isBlank(memModelId)) {
                return null;
            }

            // getmemoryModel configuration
            ModelConfigEntity memModelConfig = modelConfigService.getModelByIdFromCache(memModelId);
            if (memModelConfig == null || memModelConfig.getConfigJson() == null) {
                return null;
            }

            // frommemoryModel configurationextractcorresponding LLMModel ID
            Map<String, Object> configMap = memModelConfig.getConfigJson();
            String llmModelId = (String) configMap.get("llm");

            if (StringUtils.isBlank(llmModelId)) {
                // ifmemorymodelnoconfigurationindependent LLM，thenuseagent defaultLLMmodel
                return agentInfo.getLlmModelId();
            }

            return llmModelId;
        } catch (Exception e) {
            log.error("getmemorysummaryLLMModel IDfailed，agentId: {}, error: {}", agentId, e.getMessage());
            return null;
        }
    }

    /**
     * according toSession IDgetDevice information
     */
    private DeviceEntity getDeviceBySessionId(String sessionId) {
        try {
            // querythissession no.oneitemsrecordgetmacAddress
            QueryWrapper<AgentChatHistoryEntity> wrapper = new QueryWrapper<>();
            wrapper.select("mac_address")
                    .eq("session_id", sessionId)
                    .last("LIMIT 1");

            AgentChatHistoryEntity entity = agentChatHistoryService.getOne(wrapper);
            if (entity != null && StringUtils.isNotBlank(entity.getMacAddress())) {
                return deviceService.getDeviceByMacAddress(entity.getMacAddress());
            }
            return null;
        } catch (Exception e) {
            log.error("according toSession ID {} findDevice informationfailed: {}", sessionId, e.getMessage());
            return null;
        }
    }
}