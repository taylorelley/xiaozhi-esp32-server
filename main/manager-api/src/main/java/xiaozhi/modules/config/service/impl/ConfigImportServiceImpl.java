package xiaozhi.modules.config.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.modules.agent.entity.AgentTemplateEntity;
import xiaozhi.modules.agent.service.AgentTemplateService;
import xiaozhi.modules.config.dto.ConfigImportItemDTO;
import xiaozhi.modules.config.dto.ConfigImportResultDTO;
import xiaozhi.modules.config.service.ConfigImportService;
import xiaozhi.modules.model.entity.ModelConfigEntity;
import xiaozhi.modules.model.service.ModelConfigService;
import xiaozhi.modules.sys.service.SysParamsService;

@Slf4j
@Service
@AllArgsConstructor
public class ConfigImportServiceImpl implements ConfigImportService {

    /**
     * Top-level YAML keys treated as provider-type sections. Must match the
     * modelType column values in ai_model_config so the provider lookup by
     * (modelType, modelCode) succeeds.
     */
    private static final Set<String> MODULE_TYPES = Set.of(
            "VAD", "ASR", "LLM", "VLLM", "TTS", "Memory", "Intent");

    /**
     * server.* paramCodes the importer is allowed to update. Anything else
     * under the `server:` block is reported as a warning and ignored, so the
     * wizard cannot unexpectedly flip security-sensitive sys_params.
     */
    private static final Set<String> ALLOWED_SERVER_PARAM_CODES = Set.of(
            "server.websocket",
            "server.ota",
            "server.mcp_endpoint",
            "server.voice_print",
            "server.name",
            "server.beian_icp_num",
            "server.beian_ga_num");

    private final ModelConfigService modelConfigService;
    private final AgentTemplateService agentTemplateService;
    private final SysParamsService sysParamsService;
    private final RedisUtils redisUtils;

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    @Override
    public ConfigImportResultDTO importYaml(byte[] yamlBytes, boolean dryRun, String mode) {
        if (yamlBytes == null || yamlBytes.length == 0) {
            throw new RenException(ErrorCode.UPLOAD_FILE_EMPTY);
        }
        String normalisedMode = MODE_REPLACE.equalsIgnoreCase(mode) ? MODE_REPLACE : MODE_MERGE;

        Map<String, Object> root = parseYaml(yamlBytes);

        ConfigImportResultDTO result = new ConfigImportResultDTO();
        result.setDryRun(dryRun);
        result.setMode(normalisedMode);

        // Step A: selected_module -> {TYPE -> modelId}
        Map<String, String> selectedIds = applySelectedModules(root, result);

        // Step B: merge per-provider sections into ai_model_config.config_json
        applyProviderSections(root, normalisedMode, dryRun, result);

        // Step C: update the default ai_agent_template with the resolved model ids
        applyAgentTemplate(selectedIds, dryRun, result);

        // Step D: allow-listed server.* entries -> sys_params
        applyServerParams(root, dryRun, result);

        // Step E: invalidate cached server config so next fetch rebuilds from DB
        if (!dryRun) {
            redisUtils.delete(RedisKeys.getServerConfigKey());
        }

        return result;
    }

    private Map<String, Object> parseYaml(byte[] yamlBytes) {
        try {
            Map<String, Object> parsed = YAML_MAPPER.readValue(
                    yamlBytes,
                    new TypeReference<Map<String, Object>>() {});
            if (parsed == null) {
                throw new RenException(ErrorCode.PARAM_JSON_INVALID);
            }
            return parsed;
        } catch (RenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to parse uploaded config YAML: {}", e.getMessage());
            throw new RenException(ErrorCode.PARAM_JSON_INVALID);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> applySelectedModules(Map<String, Object> root, ConfigImportResultDTO result) {
        Map<String, String> selected = new LinkedHashMap<>();
        Object node = root.get("selected_module");
        if (node == null) {
            result.getValidationErrors().add("YAML has no 'selected_module' block; module selections unchanged");
            return selected;
        }
        if (!(node instanceof Map)) {
            result.getValidationErrors().add("'selected_module' must be a mapping");
            return selected;
        }
        for (Map.Entry<String, Object> e : ((Map<String, Object>) node).entrySet()) {
            String type = e.getKey();
            Object rawValue = e.getValue();
            if (rawValue == null) {
                continue;
            }
            if (!MODULE_TYPES.contains(type)) {
                result.getValidationErrors().add("selected_module." + type + " is not a recognised module type; ignored");
                continue;
            }
            String provideCode = String.valueOf(rawValue);
            ModelConfigEntity model = findModel(type, provideCode);
            if (model == null) {
                result.getSkippedModules()
                        .add(ConfigImportItemDTO.skipped(type, provideCode,
                                "No ai_model_config row for this (modelType, modelCode)"));
                continue;
            }
            selected.put(type, model.getId());
            result.getAppliedModules()
                    .add(ConfigImportItemDTO.applied(type, provideCode, model.getId()));
        }
        return selected;
    }

    @SuppressWarnings("unchecked")
    private void applyProviderSections(Map<String, Object> root, String mode, boolean dryRun,
                                       ConfigImportResultDTO result) {
        for (String type : MODULE_TYPES) {
            Object node = root.get(type);
            if (!(node instanceof Map)) {
                continue;
            }
            Map<String, Object> providers = (Map<String, Object>) node;
            for (Map.Entry<String, Object> p : providers.entrySet()) {
                String provideCode = p.getKey();
                Object rawFields = p.getValue();
                if (!(rawFields instanceof Map)) {
                    result.getValidationErrors()
                            .add(type + "." + provideCode + " is not a mapping; skipped");
                    continue;
                }
                Map<String, Object> fields = (Map<String, Object>) rawFields;
                ModelConfigEntity model = findModel(type, provideCode);
                if (model == null) {
                    result.getSkippedModules()
                            .add(ConfigImportItemDTO.skipped(type, provideCode,
                                    "No ai_model_config row for this (modelType, modelCode); fields not applied"));
                    continue;
                }
                JSONObject merged;
                if (MODE_REPLACE.equals(mode) || model.getConfigJson() == null) {
                    merged = new JSONObject();
                } else {
                    merged = new JSONObject(model.getConfigJson());
                }
                for (Map.Entry<String, Object> f : fields.entrySet()) {
                    merged.set(f.getKey(), f.getValue());
                }
                model.setConfigJson(merged);
                if (!dryRun) {
                    modelConfigService.updateById(model);
                }
                result.getAppliedFields()
                        .add(ConfigImportItemDTO.fields(type, provideCode,
                                new ArrayList<>(fields.keySet())));
            }
        }
    }

    private void applyAgentTemplate(Map<String, String> selected, boolean dryRun,
                                    ConfigImportResultDTO result) {
        if (selected.isEmpty()) {
            return;
        }
        AgentTemplateEntity template = agentTemplateService.getDefaultTemplate();
        if (template == null) {
            throw new RenException(ErrorCode.AGENT_TEMPLATE_NOT_FOUND);
        }
        if (selected.containsKey("VAD")) {
            template.setVadModelId(selected.get("VAD"));
        }
        if (selected.containsKey("ASR")) {
            template.setAsrModelId(selected.get("ASR"));
        }
        if (selected.containsKey("LLM")) {
            template.setLlmModelId(selected.get("LLM"));
        }
        if (selected.containsKey("VLLM")) {
            template.setVllmModelId(selected.get("VLLM"));
        }
        if (selected.containsKey("TTS")) {
            template.setTtsModelId(selected.get("TTS"));
        }
        if (selected.containsKey("Memory")) {
            template.setMemModelId(selected.get("Memory"));
        }
        if (selected.containsKey("Intent")) {
            template.setIntentModelId(selected.get("Intent"));
        }
        if (!dryRun) {
            agentTemplateService.updateById(template);
        }
        result.setAgentTemplateUpdated(true);
    }

    @SuppressWarnings("unchecked")
    private void applyServerParams(Map<String, Object> root, boolean dryRun, ConfigImportResultDTO result) {
        Object node = root.get("server");
        if (!(node instanceof Map)) {
            return;
        }
        flattenServer("server", (Map<String, Object>) node, result, dryRun);
    }

    @SuppressWarnings("unchecked")
    private void flattenServer(String prefix, Map<String, Object> node, ConfigImportResultDTO result, boolean dryRun) {
        for (Map.Entry<String, Object> e : node.entrySet()) {
            String paramCode = prefix + "." + e.getKey();
            Object value = e.getValue();
            if (value instanceof Map) {
                flattenServer(paramCode, (Map<String, Object>) value, result, dryRun);
                continue;
            }
            if (!ALLOWED_SERVER_PARAM_CODES.contains(paramCode)) {
                result.getValidationErrors().add(paramCode + " is not in the server.* allow-list; ignored");
                continue;
            }
            String stringValue = stringifyScalar(value);
            if (stringValue == null) {
                result.getValidationErrors().add(paramCode + " has an unsupported value type; ignored");
                continue;
            }
            if (!dryRun) {
                int updated = sysParamsService.updateValueByCode(paramCode, stringValue);
                if (updated == 0) {
                    result.getValidationErrors().add(paramCode + " is not defined in sys_params; not created");
                    continue;
                }
            }
            result.getAppliedServerParams().add(paramCode);
        }
    }

    private String stringifyScalar(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof List<?> list) {
            List<String> parts = new ArrayList<>(list.size());
            for (Object v : list) {
                if (v instanceof String || v instanceof Number || v instanceof Boolean) {
                    parts.add(v.toString());
                } else {
                    return null;
                }
            }
            return String.join(";", parts);
        }
        return null;
    }

    private ModelConfigEntity findModel(String modelType, String modelCode) {
        if (StringUtils.isBlank(modelType) || StringUtils.isBlank(modelCode)) {
            return null;
        }
        LambdaQueryWrapper<ModelConfigEntity> q = new LambdaQueryWrapper<>();
        q.eq(ModelConfigEntity::getModelType, modelType)
                .eq(ModelConfigEntity::getModelCode, modelCode)
                .last("limit 1");
        return modelConfigService.getOne(q);
    }

    // Reserved for future extensions (e.g. dict lookups). Kept as an explicit
    // utility list so the allow-list is easy to audit alongside
    // ALLOWED_SERVER_PARAM_CODES.
    @SuppressWarnings("unused")
    private static final List<String> MODULE_TYPES_LIST = new ArrayList<>(Arrays.asList(
            "VAD", "ASR", "LLM", "VLLM", "TTS", "Memory", "Intent"));
}
