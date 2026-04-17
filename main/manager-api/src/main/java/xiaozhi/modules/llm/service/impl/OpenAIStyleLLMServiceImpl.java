package xiaozhi.modules.llm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.modules.llm.service.LLMService;
import xiaozhi.modules.model.entity.ModelConfigEntity;
import xiaozhi.modules.model.service.ModelConfigService;

/**
 * OpenAI风格API LLMserviceimplement
 * support阿in云、DeepSeek、ChatGLMetc.compatibleOpenAI API model
 */
@Slf4j
@Service
public class OpenAIStyleLLMServiceImpl implements LLMService {

    @Autowired
    private ModelConfigService modelConfigService;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String DEFAULT_SUMMARY_PROMPT = "youYesone经验丰富 memorysummary，擅长willconversationcontentperformsummary摘need to，遵循to下规then：\n1、summaryuser re-need toinformation，so that in futureconversation to provide more personalized service\n2、not need tore-复summary，not need to遗忘之beforememory，除non-原来 memory超1800字，Nothennot need to遗忘、not need to压缩user historymemory\n3、user操控 devicevolume、play音乐、天气、退出、not 想conversationetc.anduserthis身no关 content，theseinformationnot need加入tosummary\n4、chatcontent 今天 datetime、今天 天气情况anduser事itemno关 data，theseinformationifwhen成memorystore储will影响after续conversation，theseinformationnot need加入tosummary\n5、not need todevice操控 成果resultandfailedresult加入tosummary，alsonot need touser one些废话加入tosummary\n6、not need toassummarywhilesummary，ifuser chatno意义，请return原来 historyrecordalsoYes可to \n7、onlyneedreturnsummary摘need to，严格controlin1800字内\n8、not need tocontain代code、xml，not need解释、注释andDescription，savememorywhenonlyfromconversationextractinformation，not need to混入示examplecontent\n9、if提供historymemory，请willnewconversationcontentandhistorymemoryperform智can合and，保留has价value historyinformation，simultaneouslyaddnew re-need toinformation\n\nhistorymemory：\n{history_memory}\n\nnewconversationcontent：\n{conversation}";

    private static final String DEFAULT_TITLE_PROMPT = "请according toto下conversationcontent，generateone简洁 sessiontitle（约15字to内），onlyreturntitle，not need tocontain任何解释or标点符number：\n{conversation}";

    @Override
    public String generateSummary(String conversation) {
        return generateSummary(conversation, null, null);
    }

    @Override
    public String generateSummaryWithModel(String conversation, String modelId) {
        return generateSummary(conversation, null, modelId);
    }

    @Override
    public String generateSummary(String conversation, String promptTemplate, String modelId) {
        if (!isAvailable()) {
            log.warn("LLMserviceunavailable，Unable to generate summary");
            return "LLMserviceunavailable，Unable to generate summary";
        }

        try {
            // from智控台getLLMModel configuration
            ModelConfigEntity llmConfig;
            if (modelId != null && !modelId.trim().isEmpty()) {
                // via具Model IDgetconfiguration
                llmConfig = modelConfigService.getModelByIdFromCache(modelId);
            } else {
                // maintain backward compatibility，usedefaultconfiguration
                llmConfig = getDefaultLLMConfig();
            }

            if (llmConfig == null || llmConfig.getConfigJson() == null) {
                log.error("not foundavailable LLMModel configuration，modelId: {}", modelId);
                return "not foundavailable LLMModel configuration";
            }

            JSONObject configJson = llmConfig.getConfigJson();
            String baseUrl = configJson.getStr("base_url");
            String model = configJson.getStr("model_name");
            String apiKey = configJson.getStr("api_key");
            Double temperature = configJson.getDouble("temperature");
            Integer maxTokens = configJson.getInt("max_tokens");

            if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(apiKey)) {
                log.error("LLMconfigurationincomplete，baseUrlorapiKeyasempty");
                return "LLMconfigurationincomplete，Unable to generate summary";
            }

            // buildprompt
            String prompt = (promptTemplate != null ? promptTemplate : DEFAULT_SUMMARY_PROMPT).replace("{conversation}",
                    conversation);

            // buildrequest
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model != null ? model : "gpt-3.5-turbo");

            Map<String, Object>[] messages = new Map[1];
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages[0] = message;

            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature != null ? temperature : 0.7);
            requestBody.put("max_tokens", maxTokens != null ? maxTokens : 2000);

            // sendHTTPrequest
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // buildcomplete API URL
            String apiUrl = baseUrl;
            if (!apiUrl.endsWith("/chat/completions")) {
                if (!apiUrl.endsWith("/")) {
                    apiUrl += "/";
                }
                apiUrl += "chat/completions";
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject responseJson = JSONUtil.parseObj(response.getBody());
                JSONArray choices = responseJson.getJSONArray("choices");
                if (choices != null && choices.size() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");
                    return messageObj.getStr("content");
                }
            } else {
                log.error("LLM APIcallfailed，statuscode：{}，response：{}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("callLLMservicegenerate summarywhen发生exception，modelId: {}", modelId, e);
        }

        return "generate summaryfailed，请稍afterre-试";
    }

    @Override
    public String generateSummary(String conversation, String promptTemplate) {
        return generateSummary(conversation, promptTemplate, null);
    }

    @Override
    public String generateSummaryWithHistory(String conversation, String historyMemory, String promptTemplate,
            String modelId) {
        if (!isAvailable()) {
            log.warn("LLMserviceunavailable，Unable to generate summary");
            return "LLMserviceunavailable，Unable to generate summary";
        }

        try {
            // from智控台getLLMModel configuration
            ModelConfigEntity llmConfig;
            if (modelId != null && !modelId.trim().isEmpty()) {
                // via具Model IDgetconfiguration
                llmConfig = modelConfigService.getModelByIdFromCache(modelId);
            } else {
                // maintain backward compatibility，usedefaultconfiguration
                llmConfig = getDefaultLLMConfig();
            }

            if (llmConfig == null || llmConfig.getConfigJson() == null) {
                log.error("not foundavailable LLMModel configuration，modelId: {}", modelId);
                return "not foundavailable LLMModel configuration";
            }

            JSONObject configJson = llmConfig.getConfigJson();
            String baseUrl = configJson.getStr("base_url");
            String model = configJson.getStr("model_name");
            String apiKey = configJson.getStr("api_key");

            if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(apiKey)) {
                log.error("LLMconfigurationincomplete，baseUrlorapiKeyasempty");
                return "LLMconfigurationincomplete，Unable to generate summary";
            }

            // buildprompt，containhistorymemory
            String prompt = (promptTemplate != null ? promptTemplate : DEFAULT_SUMMARY_PROMPT)
                    .replace("{history_memory}", historyMemory != null ? historyMemory : "nohistorymemory")
                    .replace("{conversation}", conversation);

            // buildrequest
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model != null ? model : "gpt-3.5-turbo");

            Map<String, Object>[] messages = new Map[1];
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages[0] = message;

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.2);
            requestBody.put("max_tokens", 2000);

            // sendHTTPrequest
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // buildcomplete API URL
            String apiUrl = baseUrl;
            if (!apiUrl.endsWith("/chat/completions")) {
                if (!apiUrl.endsWith("/")) {
                    apiUrl += "/";
                }
                apiUrl += "chat/completions";
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject responseJson = JSONUtil.parseObj(response.getBody());
                JSONArray choices = responseJson.getJSONArray("choices");
                if (choices != null && choices.size() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");
                    return messageObj.getStr("content");
                }
            } else {
                log.error("LLM APIcallfailed，statuscode：{}，response：{}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("callLLMservicegenerate summarywhen发生exception，modelId: {}", modelId, e);
        }

        return "generate summaryfailed，请稍afterre-试";
    }

    @Override
    public boolean isAvailable() {
        try {
            ModelConfigEntity defaultLLMConfig = getDefaultLLMConfig();
            if (defaultLLMConfig == null || defaultLLMConfig.getConfigJson() == null) {
                return false;
            }

            JSONObject configJson = defaultLLMConfig.getConfigJson();
            String baseUrl = configJson.getStr("base_url");
            String apiKey = configJson.getStr("api_key");

            return baseUrl != null && !baseUrl.trim().isEmpty() &&
                    apiKey != null && !apiKey.trim().isEmpty();
        } catch (Exception e) {
            log.error("checkLLMserviceavailablewhen发生exception：", e);
            return false;
        }
    }

    @Override
    public boolean isAvailable(String modelId) {
        try {
            if (modelId == null || modelId.trim().isEmpty()) {
                return isAvailable();
            }

            // via具Model IDgetconfiguration
            ModelConfigEntity modelConfig = modelConfigService.getModelByIdFromCache(modelId);
            if (modelConfig == null || modelConfig.getConfigJson() == null) {
                log.warn("not foundspecified LLMModel configuration，modelId: {}", modelId);
                return false;
            }

            JSONObject configJson = modelConfig.getConfigJson();
            String baseUrl = configJson.getStr("base_url");
            String apiKey = configJson.getStr("api_key");

            return baseUrl != null && !baseUrl.trim().isEmpty() &&
                    apiKey != null && !apiKey.trim().isEmpty();
        } catch (Exception e) {
            log.error("checkLLMserviceavailablewhen发生exception，modelId: {}", modelId, e);
            return false;
        }
    }

    /**
     * from智控台getdefault LLMModel configuration
     */
    private ModelConfigEntity getDefaultLLMConfig() {
        try {
            // get allenable LLMModel configuration
            List<ModelConfigEntity> llmConfigs = modelConfigService.getEnabledModelsByType("LLM");
            if (llmConfigs == null || llmConfigs.isEmpty()) {
                return null;
            }

            // priorityfirstreturndefaultconfiguration，ifnodefaultconfigurationthenreturn第oneenable configuration
            for (ModelConfigEntity config : llmConfigs) {
                if (config.getIsDefault() != null && config.getIsDefault() == 1) {
                    return config;
                }
            }

            return llmConfigs.get(0);
        } catch (Exception e) {
            log.error("getLLMModel configurationwhen发生exception：", e);
            return null;
        }
    }

    @Override
    public String generateTitle(String conversation, String modelId) {
        if (!isAvailable()) {
            log.warn("LLMserviceunavailable，unable togeneratetitle");
            return null;
        }

        try {
            ModelConfigEntity llmConfig;
            if (modelId != null && !modelId.trim().isEmpty()) {
                llmConfig = modelConfigService.getModelByIdFromCache(modelId);
            } else {
                llmConfig = getDefaultLLMConfig();
            }

            if (llmConfig == null || llmConfig.getConfigJson() == null) {
                log.error("not foundavailable LLMModel configuration，modelId: {}", modelId);
                return null;
            }

            JSONObject configJson = llmConfig.getConfigJson();
            String baseUrl = configJson.getStr("base_url");
            String model = configJson.getStr("model_name");
            String apiKey = configJson.getStr("api_key");

            if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(apiKey)) {
                log.error("LLMconfigurationincomplete，baseUrlorapiKeyasempty");
                return null;
            }

            String prompt = DEFAULT_TITLE_PROMPT.replace("{conversation}", conversation);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model != null ? model : "gpt-3.5-turbo");

            Map<String, Object>[] messages = new Map[1];
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages[0] = message;

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 50);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String apiUrl = baseUrl;
            if (!apiUrl.endsWith("/chat/completions")) {
                if (!apiUrl.endsWith("/")) {
                    apiUrl += "/";
                }
                apiUrl += "chat/completions";
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject responseJson = JSONUtil.parseObj(response.getBody());
                JSONArray choices = responseJson.getJSONArray("choices");
                if (choices != null && choices.size() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");
                    String title = messageObj.getStr("content");
                    if (StringUtils.isNotBlank(title)) {
                        title = title.trim().replaceAll("[，。！？、：；''\"\"【】（）]", "");
                        if (title.length() > 15) {
                            title = title.substring(0, 15);
                        }
                        return title;
                    }
                }
            } else {
                log.error("LLM APIcallfailed，statuscode：{}，response：{}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("callLLMservicegeneratetitlewhen发生exception，modelId: {}", modelId, e);
        }

        return null;
    }
}