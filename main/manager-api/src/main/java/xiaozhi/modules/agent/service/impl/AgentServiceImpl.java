package xiaozhi.modules.agent.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.AllArgsConstructor;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.service.impl.BaseServiceImpl;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.ConvertUtils;
import xiaozhi.common.utils.JsonUtils;
import xiaozhi.common.utils.ToolUtil;
import xiaozhi.modules.agent.dao.AgentDao;
import xiaozhi.modules.agent.dao.AgentTagDao;
import xiaozhi.modules.agent.dto.AgentCreateDTO;
import xiaozhi.modules.agent.dto.AgentDTO;
import xiaozhi.modules.agent.dto.AgentTagDTO;
import xiaozhi.modules.agent.dto.AgentUpdateDTO;
import xiaozhi.modules.agent.entity.AgentContextProviderEntity;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.agent.entity.AgentPluginMapping;
import xiaozhi.modules.agent.entity.AgentTagEntity;
import xiaozhi.modules.agent.entity.AgentTemplateEntity;
import xiaozhi.modules.agent.service.AgentChatHistoryService;
import xiaozhi.modules.agent.service.AgentContextProviderService;
import xiaozhi.modules.agent.service.AgentPluginMappingService;
import xiaozhi.modules.agent.service.AgentService;
import xiaozhi.modules.agent.service.AgentTagService;
import xiaozhi.modules.agent.service.AgentTemplateService;
import xiaozhi.modules.agent.vo.AgentInfoVO;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.model.dto.ModelProviderDTO;
import xiaozhi.modules.model.dto.VoiceDTO;
import xiaozhi.modules.model.entity.ModelConfigEntity;
import xiaozhi.modules.model.service.ModelConfigService;
import xiaozhi.modules.model.service.ModelProviderService;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.enums.SuperAdminEnum;
import xiaozhi.modules.timbre.service.TimbreService;

@Service
@AllArgsConstructor
public class AgentServiceImpl extends BaseServiceImpl<AgentDao, AgentEntity> implements AgentService {
    private final AgentDao agentDao;
    private final AgentTagDao agentTagDao;
    private final TimbreService timbreModelService;
    private final ModelConfigService modelConfigService;
    private final RedisUtils redisUtils;
    private final DeviceService deviceService;
    private final AgentPluginMappingService agentPluginMappingService;
    private final AgentChatHistoryService agentChatHistoryService;
    private final AgentTemplateService agentTemplateService;
    private final ModelProviderService modelProviderService;
    private final AgentContextProviderService agentContextProviderService;
    private final AgentTagService agentTagService;

    @Override
    public PageData<AgentEntity> adminAgentList(Map<String, Object> params) {
        IPage<AgentEntity> page = agentDao.selectPage(
                getPage(params, "agent_name", true),
                new QueryWrapper<>());
        return new PageData<>(page.getRecords(), page.getTotal());
    }

    @Override
    public AgentInfoVO getAgentById(String id) {
        AgentInfoVO agent = agentDao.selectAgentInfoById(id);

        if (agent == null) {
            throw new RenException(ErrorCode.AGENT_NOT_FOUND);
        }

        if (agent.getMemModelId() != null && agent.getMemModelId().equals(Constant.MEMORY_NO_MEM)) {
            agent.setChatHistoryConf(Constant.ChatHistoryConfEnum.IGNORE.getCode());
        }
        if (agent.getChatHistoryConf() == null) {
            agent.setChatHistoryConf(Constant.ChatHistoryConfEnum.RECORD_TEXT_AUDIO.getCode());
        }

        // querycontextsourceconfiguration
        AgentContextProviderEntity contextProviderEntity = agentContextProviderService.getByAgentId(id);
        if (contextProviderEntity != null) {
            agent.setContextProviders(contextProviderEntity.getContextProviders());
        }

        // 无需额外querypluginlist，alreadyviaSQLquery出来
        return agent;
    }

    @Override
    public boolean insert(AgentEntity entity) {
        // ifIDasempty，自动generate一个UUIDasID
        if (entity.getId() == null || entity.getId().trim().isEmpty()) {
            entity.setId(UUID.randomUUID().toString().replace("-", ""));
        }

        // ifAgent codeasempty，自动generate一个with前缀 code
        if (entity.getAgentCode() == null || entity.getAgentCode().trim().isEmpty()) {
            entity.setAgentCode("AGT_" + System.currentTimeMillis());
        }

        // ifSort orderfieldasempty，setdefaultvalue0
        if (entity.getSort() == null) {
            entity.setSort(0);
        }

        return super.insert(entity);
    }

    @Override
    public void deleteAgentByUserId(Long userId) {
        UpdateWrapper<AgentEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        baseDao.delete(wrapper);
    }

    @Override
    public List<AgentDTO> getUserAgents(Long userId, String keyword, String searchType) {
        QueryWrapper<AgentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("created_at");

        // if有searchkeyword，according tosearchtypeadd相应 queryitems件
        if (StringUtils.isNotBlank(keyword)) {
            if ("mac".equals(searchType)) {
                // byMACAddresssearch：firstsearchdevice，再getcorresponding agent
                List<DeviceEntity> devices = Optional
                        .ofNullable(deviceService.searchDevicesByMacAddress(keyword, userId)).orElseGet(ArrayList::new);
                // getdevicecorresponding Agent IDlist
                List<String> agentIds = devices.stream()
                        .map(DeviceEntity::getAgentId)
                        .distinct()
                        .collect(Collectors.toList());
                if (ToolUtil.isNotEmpty(agentIds)) {
                    queryWrapper.in("id", agentIds);
                } else {
                    return new ArrayList<>();
                }
            } else {
                // bynamesearch（default）：同时searchAgent nameandTag名
                List<String> tagAgentIds = agentTagService.getAgentIdsByTagName(keyword);
                if (ToolUtil.isNotEmpty(tagAgentIds)) {
                    queryWrapper.and(wrapper -> wrapper
                            .like("agent_name", keyword)
                            .or()
                            .in("id", tagAgentIds));
                } else {
                    queryWrapper.like("agent_name", keyword);
                }
            }
        }

        // executequery
        List<AgentEntity> agentEntities = baseDao.selectList(queryWrapper);

        // convert toDTO并setall必need tofield
        return agentEntities.stream().map(this::buildAgentDTO).collect(Collectors.toList());
    }

    /**
     * willAgentEntityconvert toAgentDTO
     */
    private AgentDTO buildAgentDTO(AgentEntity agent) {
        AgentDTO dto = new AgentDTO();
        dto.setId(agent.getId());
        dto.setAgentName(agent.getAgentName());
        dto.setSystemPrompt(agent.getSystemPrompt());

        // get TTS Model name
        dto.setTtsModelName(modelConfigService.getModelNameById(agent.getTtsModelId()));

        // get LLM Model name
        dto.setLlmModelName(modelConfigService.getModelNameById(agent.getLlmModelId()));

        // get VLLM Model name
        dto.setVllmModelName(modelConfigService.getModelNameById(agent.getVllmModelId()));

        // getmemoryModel name
        dto.setMemModelId(agent.getMemModelId());

        // get TTS voicename
        dto.setTtsVoiceName(timbreModelService.getTimbreNameById(agent.getTtsVoiceId()));

        // getagent最近 lastconnection时长
        dto.setLastConnectedAt(deviceService.getLatestLastConnectionTime(agent.getId()));

        // getDevice count
        dto.setDeviceCount(getDeviceCountByAgentId(agent.getId()));

        // getTaglist
        List<AgentTagEntity> tags = agentTagDao.selectByAgentId(agent.getId());
        if (ToolUtil.isNotEmpty(tags)) {
            dto.setTags(tags.stream().map(this::convertTagToDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    private AgentTagDTO convertTagToDTO(AgentTagEntity entity) {
        AgentTagDTO dto = new AgentTagDTO();
        dto.setId(entity.getId());
        dto.setTagName(entity.getTagName());
        return dto;
    }

    @Override
    public Integer getDeviceCountByAgentId(String agentId) {
        if (StringUtils.isBlank(agentId)) {
            return 0;
        }

        // firstfromRedisget
        Integer cachedCount = (Integer) redisUtils.get(RedisKeys.getAgentDeviceCountById(agentId));
        if (cachedCount != null) {
            return cachedCount;
        }

        // ifRedisno，thenfromdatalibraryquery
        Integer deviceCount = agentDao.getDeviceCountByAgentId(agentId);

        // willresultstoreRedis
        if (deviceCount != null) {
            redisUtils.set(RedisKeys.getAgentDeviceCountById(agentId), deviceCount, 60);
        }

        return deviceCount != null ? deviceCount : 0;
    }

    @Override
    public AgentEntity getDefaultAgentByMacAddress(String macAddress) {
        if (StringUtils.isEmpty(macAddress)) {
            return null;
        }
        return agentDao.getDefaultAgentByMacAddress(macAddress);
    }

    @Override
    public boolean checkAgentPermission(String agentId, Long userId) {
        if (SecurityUser.getUser() == null || SecurityUser.getUser().getId() == null) {
            return false;
        }
        // getAgent information
        AgentEntity agent = getAgentById(agentId);
        if (agent == null) {
            return false;
        }

        // ifYesSuper administrator，directlyreturntrue
        if (SecurityUser.getUser().getSuperAdmin() == SuperAdminEnum.YES.value()) {
            return true;
        }

        // 检查YesNoYesagent all
        return userId.equals(agent.getUserId());
    }

    // according toidupdateAgent information
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAgentById(String agentId, AgentUpdateDTO dto) {
        // firstquery现有entity
        AgentEntity existingEntity = this.getAgentById(agentId);
        if (existingEntity == null) {
            throw new RenException(ErrorCode.AGENT_NOT_FOUND);
        }

        // onlyupdate提供 非emptyfield
        if (dto.getAgentName() != null) {
            existingEntity.setAgentName(dto.getAgentName());
        }
        if (dto.getAgentCode() != null) {
            existingEntity.setAgentCode(dto.getAgentCode());
        }
        if (dto.getAsrModelId() != null) {
            existingEntity.setAsrModelId(dto.getAsrModelId());
        }
        if (dto.getVadModelId() != null) {
            existingEntity.setVadModelId(dto.getVadModelId());
        }
        if (dto.getLlmModelId() != null) {
            existingEntity.setLlmModelId(dto.getLlmModelId());
        }
        if (dto.getSlmModelId() != null) {
            existingEntity.setSlmModelId(dto.getSlmModelId());
        }
        if (dto.getVllmModelId() != null) {
            existingEntity.setVllmModelId(dto.getVllmModelId());
        }
        if (dto.getTtsModelId() != null) {
            existingEntity.setTtsModelId(dto.getTtsModelId());
        }
        if (dto.getTtsVoiceId() != null) {
            existingEntity.setTtsVoiceId(dto.getTtsVoiceId());
        }
        if (dto.getTtsLanguage() != null) {
            existingEntity.setTtsLanguage(dto.getTtsLanguage());
        }
        if (dto.getTtsVolume() != null) {
            existingEntity.setTtsVolume(dto.getTtsVolume());
        }
        if (dto.getTtsRate() != null) {
            existingEntity.setTtsRate(dto.getTtsRate());
        }
        if (dto.getTtsPitch() != null) {
            existingEntity.setTtsPitch(dto.getTtsPitch());
        }
        if (dto.getMemModelId() != null) {
            existingEntity.setMemModelId(dto.getMemModelId());
        }
        if (dto.getIntentModelId() != null) {
            existingEntity.setIntentModelId(dto.getIntentModelId());
        }
        if (dto.getSystemPrompt() != null) {
            existingEntity.setSystemPrompt(dto.getSystemPrompt());
        }
        if (dto.getSummaryMemory() != null) {
            existingEntity.setSummaryMemory(dto.getSummaryMemory());
        }
        if (dto.getChatHistoryConf() != null) {
            existingEntity.setChatHistoryConf(dto.getChatHistoryConf());
        }
        if (dto.getLangCode() != null) {
            existingEntity.setLangCode(dto.getLangCode());
        }
        if (dto.getLanguage() != null) {
            existingEntity.setLanguage(dto.getLanguage());
        }
        if (dto.getSort() != null) {
            existingEntity.setSort(dto.getSort());
        }

        // updatefunctionplugininformation
        List<AgentUpdateDTO.FunctionInfo> functions = dto.getFunctions();
        if (functions != null) {
            // 1. 收集thistimes提交  pluginId
            List<String> newPluginIds = functions.stream()
                    .map(AgentUpdateDTO.FunctionInfo::getPluginId)
                    .toList();

            // 2. querycurrentagent现有 allmapping
            List<AgentPluginMapping> existing = agentPluginMappingService.list(
                    new QueryWrapper<AgentPluginMapping>()
                            .eq("agent_id", agentId));
            Map<String, AgentPluginMapping> existMap = existing.stream()
                    .collect(Collectors.toMap(AgentPluginMapping::getPluginId, Function.identity()));

            // 3. constructallneed to saveorupdate  entity
            List<AgentPluginMapping> allToPersist = functions.stream().map(info -> {
                AgentPluginMapping m = new AgentPluginMapping();
                m.setAgentId(agentId);
                m.setPluginId(info.getPluginId());
                m.setParamInfo(JsonUtils.toJsonString(info.getParamInfo()));
                AgentPluginMapping old = existMap.get(info.getPluginId());
                if (old != null) {
                    // already exists，setidrepresentsupdate
                    m.setId(old.getId());
                }
                return m;
            }).toList();

            // 4. 拆分：already有ID 走update，无ID 走insert
            List<AgentPluginMapping> toUpdate = allToPersist.stream()
                    .filter(m -> m.getId() != null)
                    .toList();
            List<AgentPluginMapping> toInsert = allToPersist.stream()
                    .filter(m -> m.getId() == null)
                    .toList();

            if (!toUpdate.isEmpty()) {
                agentPluginMappingService.updateBatchById(toUpdate);
            }
            if (!toInsert.isEmpty()) {
                agentPluginMappingService.saveBatch(toInsert);
            }

            // 5. deletethistimesnot in提交list里 pluginmapping
            List<Long> toDelete = existing.stream()
                    .filter(old -> !newPluginIds.contains(old.getPluginId()))
                    .map(AgentPluginMapping::getId)
                    .toList();
            if (!toDelete.isEmpty()) {
                agentPluginMappingService.removeBatchByIds(toDelete);
            }
        }

        // setupdateinformation
        UserDetail user = SecurityUser.getUser();
        existingEntity.setUpdater(user.getId());
        existingEntity.setUpdatedAt(new Date());

        // updatememory策略
        // deleteallrecord
        if (existingEntity.getMemModelId() != null && existingEntity.getMemModelId().equals(Constant.MEMORY_NO_MEM)) {
            agentChatHistoryService.deleteByAgentId(existingEntity.getId(), true, true);
            existingEntity.setSummaryMemory("");
            // deletememory
        } else if (existingEntity.getMemModelId() != null
                && existingEntity.getMemModelId().equals(Constant.MEMORY_MEM_REPORT_ONLY)) {
            existingEntity.setSummaryMemory("");
        }

        // updatecontextsourceconfiguration
        if (dto.getContextProviders() != null) {
            AgentContextProviderEntity contextEntity = new AgentContextProviderEntity();
            contextEntity.setAgentId(agentId);
            contextEntity.setContextProviders(dto.getContextProviders());
            agentContextProviderService.saveOrUpdateByAgentId(contextEntity);
        }

        boolean b = validateLLMIntentParams(dto.getLlmModelId(), dto.getIntentModelId());
        if (!b) {
            throw new RenException(ErrorCode.LLM_INTENT_PARAMS_MISMATCH);
        }
        this.updateById(existingEntity);
    }

    /**
     * verificationlargeLanguagemodelandIntent recognition parameterYesNomatching匹配
     * 
     * @param llmModelId    largeLanguagemodelid
     * @param intentModelId Intent recognitionid
     * @return T 匹配 : F does not match
     */
    private boolean validateLLMIntentParams(String llmModelId, String intentModelId) {
        if (StringUtils.isBlank(llmModelId)) {
            return true;
        }
        ModelConfigEntity llmModelData = modelConfigService.selectById(llmModelId);
        String type = llmModelData.getConfigJson().get("type").toString();
        // ifquerylargeLanguagemodelYesopenaiorollama，Intent recognition选parameter都可以
        if ("openai".equals(type) || "ollama".equals(type)) {
            return true;
        }
        // 除了openaiandollama type，not 可以选择idasIntent_function_call（functioncall） Intent recognition
        return !"Intent_function_call".equals(intentModelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAgent(AgentCreateDTO dto) {
        // convert toentity
        AgentEntity entity = ConvertUtils.sourceToTarget(dto, AgentEntity.class);

        // getdefaulttemplate
        AgentTemplateEntity template = agentTemplateService.getDefaultTemplate();
        if (template != null) {
            // settemplate defaultvalue
            entity.setAsrModelId(template.getAsrModelId());
            entity.setVadModelId(template.getVadModelId());
            entity.setLlmModelId(template.getLlmModelId());
            entity.setVllmModelId(template.getVllmModelId());
            entity.setTtsModelId(template.getTtsModelId());

            if (template.getTtsVoiceId() == null && template.getTtsModelId() != null) {
                ModelConfigEntity ttsModel = modelConfigService.selectById(template.getTtsModelId());
                if (ttsModel != null && ttsModel.getConfigJson() != null) {
                    Map<String, Object> config = ttsModel.getConfigJson();
                    String voice = (String) config.get("voice");
                    if (StringUtils.isBlank(voice)) {
                        voice = (String) config.get("speaker");
                    }
                    VoiceDTO timbre = timbreModelService.getByVoiceCode(template.getTtsModelId(), voice);
                    if (timbre != null) {
                        template.setTtsVoiceId(timbre.getId());
                    }
                }
            }

            entity.setTtsVoiceId(template.getTtsVoiceId());
            entity.setMemModelId(template.getMemModelId());
            entity.setIntentModelId(template.getIntentModelId());
            entity.setSystemPrompt(template.getSystemPrompt());
            entity.setSummaryMemory(template.getSummaryMemory());

            // according tomemoryModel typesetdefault chatHistoryConfvalue
            if (template.getMemModelId() != null) {
                if (template.getMemModelId().equals("Memory_nomem")) {
                    // 无memoryfunction model，defaultnot recordChat history
                    entity.setChatHistoryConf(0);
                } else {
                    // 有memoryfunction model，defaultrecordtext and voice
                    entity.setChatHistoryConf(2);
                }
            } else {
                entity.setChatHistoryConf(template.getChatHistoryConf());
            }

            entity.setLangCode(template.getLangCode());
            entity.setLanguage(template.getLanguage());
        }

        if (entity.getSlmModelId() == null) {
            String defaultSlmModelId = getDefaultLLMModelId();
            if (defaultSlmModelId != null) {
                entity.setSlmModelId(defaultSlmModelId);
            }
        }

        // setUser IDandCreatorinformation
        UserDetail user = SecurityUser.getUser();
        entity.setUserId(user.getId());
        entity.setCreator(user.getId());
        entity.setCreatedAt(new Date());

        // saveagent
        insert(entity);

        // setdefaultplugin
        List<AgentPluginMapping> toInsert = new ArrayList<>();
        // play音乐、查天气、查new闻
        String[] pluginIds = new String[] { "SYSTEM_PLUGIN_MUSIC", "SYSTEM_PLUGIN_WEATHER",
                "SYSTEM_PLUGIN_NEWS_NEWSNOW" };
        for (String pluginId : pluginIds) {
            ModelProviderDTO provider = modelProviderService.getById(pluginId);
            if (provider == null) {
                continue;
            }
            AgentPluginMapping mapping = new AgentPluginMapping();
            mapping.setPluginId(pluginId);

            Map<String, Object> paramInfo = new HashMap<>();
            List<Map<String, Object>> fields = JsonUtils.parseObject(provider.getFields(), List.class);
            if (fields != null) {
                for (Map<String, Object> field : fields) {
                    paramInfo.put((String) field.get("key"), field.get("default"));
                }
            }
            mapping.setParamInfo(JsonUtils.toJsonString(paramInfo));
            mapping.setAgentId(entity.getId());
            toInsert.add(mapping);
        }
        // savedefaultplugin
        agentPluginMappingService.saveBatch(toInsert);
        return entity.getId();
    }

    private String getDefaultLLMModelId() {
        try {
            List<ModelConfigEntity> llmConfigs = modelConfigService.getEnabledModelsByType("LLM");
            if (llmConfigs == null || llmConfigs.isEmpty()) {
                return null;
            }

            for (ModelConfigEntity config : llmConfigs) {
                if (config.getIsDefault() != null && config.getIsDefault() == 1) {
                    return config.getId();
                }
            }

            return llmConfigs.get(0).getId();
        } catch (Exception e) {
            return null;
        }
    }

}