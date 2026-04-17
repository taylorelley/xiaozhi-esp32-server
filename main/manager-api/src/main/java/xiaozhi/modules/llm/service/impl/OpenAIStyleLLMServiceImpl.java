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
 * OpenAIwindAPI LLMserviceimplement
 * supportincloud、DeepSeek、ChatGLMetc.compatibleOpenAI API model
 */
@Slf4j
@Service
public class OpenAIStyleLLMServiceImpl implements LLMService {

    @Autowired
    private ModelConfigService modelConfigService;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String DEFAULT_SUMMARY_PROMPT = "youYesonerich experience memorysummary，good atlongwillconversationcontentperformsummaryabstractneed to，followtobelowrulethen：\n1、summaryuser re-need toinformation，so that in futureconversation to provide more personalized service\n2、not need tore-re-summary，not need toforgottenbeforememory，dividenon-originalcome memoryexceed1800character，Nothennot need toforget、not need tocompressuser historymemory\n3、useroperatecontrol devicevolume、playmusic、day、backout、not thinkconversationetc.anduserthisbodynorelated content，theseinformationnot needaddintosummary\n4、chatcontent todayday datetime、todayday daysituationandusereventitemnorelated data，theseinformationifwhenmemorystorestorewillaffectaftercontinueconversation，theseinformationnot needaddintosummary\n5、not need todeviceoperatecontrol resultresultandfailedresultaddintosummary，alsonot need touser oneobsoletetalkaddintosummary\n6、not need toassummarywhilesummary，ifuser chatnomeaning，pleasereturnoriginalcome historyrecordalsoYescanto \n7、onlyneedreturnsummaryabstractneed to，strictcontrolin1800characterinternal\n8、not need tocontaincode、xml，not needexplain、commentandDescription，savememorywhenonlyfromconversationextractinformation，not need tomixinexampleexamplecontent\n9、ifprovidehistorymemory，pleasewillnewconversationcontentandhistorymemoryperformintelligentcanmergeand，reservehaspricevalue historyinformation，simultaneouslyaddnew re-need toinformation\n\nhistorymemory：\n{history_memory}\n\nnewconversationcontent：\n{conversation}";

    private static final String DEFAULT_TITLE_PROMPT = "pleaseaccording totobelowconversationcontent，generateonesimpleclean sessiontitle（about15charactertointernal），onlyreturntitle，not need tocontainany explanationorlabelpointsymbolnumber：\n{conversation}";

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
            // fromintelligentcontrolconsolegetLLMModel configuration
            ModelConfigEntity llmConfig;
            if (modelId != null && !modelId.trim().isEmpty()) {
                // viahaveModel IDgetconfiguration
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
            log.error("callLLMservicegenerate summarywhenoccurexception，modelId: {}", modelId, e);
        }

        return "generate summaryfailed，pleaseslightlyafterre-try";
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
            // fromintelligentcontrolconsolegetLLMModel configuration
            ModelConfigEntity llmConfig;
            if (modelId != null && !modelId.trim().isEmpty()) {
                // viahaveModel IDgetconfiguration
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
            log.error("callLLMservicegenerate summarywhenoccurexception，modelId: {}", modelId, e);
        }

        return "generate summaryfailed，pleaseslightlyafterre-try";
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
            log.error("checkLLMserviceavailablewhenoccurexception：", e);
            return false;
        }
    }

    @Override
    public boolean isAvailable(String modelId) {
        try {
            if (modelId == null || modelId.trim().isEmpty()) {
                return isAvailable();
            }

            // viahaveModel IDgetconfiguration
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
            log.error("checkLLMserviceavailablewhenoccurexception，modelId: {}", modelId, e);
            return false;
        }
    }

    /**
     * fromintelligentcontrolconsolegetdefault LLMModel configuration
     */
    private ModelConfigEntity getDefaultLLMConfig() {
        try {
            // get allenable LLMModel configuration
            List<ModelConfigEntity> llmConfigs = modelConfigService.getEnabledModelsByType("LLM");
            if (llmConfigs == null || llmConfigs.isEmpty()) {
                return null;
            }

            // priorityfirstreturndefaultconfiguration，ifnodefaultconfigurationthenreturnno.oneenable configuration
            for (ModelConfigEntity config : llmConfigs) {
                if (config.getIsDefault() != null && config.getIsDefault() == 1) {
                    return config;
                }
            }

            return llmConfigs.get(0);
        } catch (Exception e) {
            log.error("getLLMModel configurationwhenoccurexception：", e);
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
            log.error("callLLMservicegeneratetitlewhenoccurexception，modelId: {}", modelId, e);
        }

        return null;
    }
}