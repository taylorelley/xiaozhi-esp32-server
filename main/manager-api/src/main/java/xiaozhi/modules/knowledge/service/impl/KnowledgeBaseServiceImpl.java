package xiaozhi.modules.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.service.impl.BaseServiceImpl;
import xiaozhi.common.utils.ConvertUtils;
import xiaozhi.common.utils.JsonUtils;
import xiaozhi.modules.knowledge.dao.KnowledgeBaseDao;
import xiaozhi.modules.knowledge.dto.KnowledgeBaseDTO;
import xiaozhi.modules.knowledge.dto.dataset.DatasetDTO;
import xiaozhi.modules.knowledge.entity.KnowledgeBaseEntity;
import xiaozhi.modules.knowledge.rag.KnowledgeBaseAdapter;
import xiaozhi.modules.knowledge.rag.KnowledgeBaseAdapterFactory;
import xiaozhi.modules.knowledge.service.KnowledgeBaseService;
import xiaozhi.modules.model.dao.ModelConfigDao;
import xiaozhi.modules.model.entity.ModelConfigEntity;
import xiaozhi.modules.model.service.ModelConfigService;
import xiaozhi.modules.security.user.SecurityUser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Knowledge baseserviceimplementclass (Refactored)
 * collection成 RAGFlow Adapter and Shadow DB mode
 */
@Service
@AllArgsConstructor
@Slf4j
public class KnowledgeBaseServiceImpl extends BaseServiceImpl<KnowledgeBaseDao, KnowledgeBaseEntity>
        implements KnowledgeBaseService {

    private final KnowledgeBaseDao knowledgeBaseDao;
    private final ModelConfigService modelConfigService;
    private final ModelConfigDao modelConfigDao;
    private final RedisUtils redisUtils;

    @Override
    public PageData<KnowledgeBaseDTO> getPageList(KnowledgeBaseDTO knowledgeBaseDTO, Integer page, Integer limit) {
        Page<KnowledgeBaseEntity> pageInfo = new Page<>(page, limit);
        QueryWrapper<KnowledgeBaseEntity> queryWrapper = new QueryWrapper<>();

        if (knowledgeBaseDTO != null) {
            queryWrapper.like(StringUtils.isNotBlank(knowledgeBaseDTO.getName()), "name", knowledgeBaseDTO.getName());
            queryWrapper.eq(knowledgeBaseDTO.getStatus() != null, "status", knowledgeBaseDTO.getStatus());
            queryWrapper.eq("creator", knowledgeBaseDTO.getCreator());
        }
        queryWrapper.orderByDesc("created_at");

        IPage<KnowledgeBaseEntity> iPage = knowledgeBaseDao.selectPage(pageInfo, queryWrapper);
        PageData<KnowledgeBaseDTO> pageData = getPageData(iPage, KnowledgeBaseDTO.class);

        // Enrich with Document Count from RAG (Optional / Lazy)
        if (pageData != null && pageData.getList() != null) {
            for (KnowledgeBaseDTO dto : pageData.getList()) {
                enrichDocumentCount(dto);
            }
        }
        return pageData;
    }

    private void enrichDocumentCount(KnowledgeBaseDTO dto) {
        try {
            if (StringUtils.isNotBlank(dto.getDatasetId()) && StringUtils.isNotBlank(dto.getRagModelId())) {
                KnowledgeBaseAdapter adapter = getAdapterByModelId(dto.getRagModelId());
                if (adapter != null) {
                    dto.setDocumentCount(adapter.getDocumentCount(dto.getDatasetId()));
                }
            }
        } catch (Exception e) {
            log.warn("unable togetKnowledge base {}  document计number: {}", dto.getName(), e.getMessage());
            dto.setDocumentCount(0);
        }
    }

    @Override
    public KnowledgeBaseDTO getById(String id) {
        KnowledgeBaseEntity entity = knowledgeBaseDao.selectById(id);
        if (entity == null) {
            throw new RenException(ErrorCode.Knowledge_Base_RECORD_NOT_EXISTS);
        }
        return ConvertUtils.sourceToTarget(entity, KnowledgeBaseDTO.class);
    }

    @Override
    public KnowledgeBaseDTO getByDatasetId(String datasetId) {
        if (StringUtils.isBlank(datasetId)) {
            throw new RenException(ErrorCode.PARAMS_GET_ERROR);
        }
        // [Production Fix] compatiblefind：priorityfirstvia dataset_id find，findnot toviaPrimary key id find，ensurebeforeend传哪种 UUID allcan命
        KnowledgeBaseEntity entity = knowledgeBaseDao
                .selectOne(new QueryWrapper<KnowledgeBaseEntity>()
                        .eq("dataset_id", datasetId)
                        .or()
                        .eq("id", datasetId));
        if (entity == null) {
            throw new RenException(ErrorCode.Knowledge_Base_RECORD_NOT_EXISTS);
        }
        return ConvertUtils.sourceToTarget(entity, KnowledgeBaseDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseDTO save(KnowledgeBaseDTO dto) {
        // 1. Validation
        checkDuplicateName(dto.getName(), null);
        KnowledgeBaseAdapter adapter = null;

        // 2. RAG Creation
        String datasetId = null;
        try {
            // 若notspecified RAG model，automaticusesystemdefault
            if (StringUtils.isBlank(dto.getRagModelId())) {
                List<ModelConfigEntity> models = getRAGModels();
                if (models != null && !models.isEmpty()) {
                    dto.setRagModelId(models.get(0).getId());
                } else {
                    throw new RenException(ErrorCode.RAG_CONFIG_NOT_FOUND, "notspecifiedandnoavailabledefault RAG model");
                }
            }

            Map<String, Object> ragConfig = getValidatedRAGConfig(dto.getRagModelId());
            adapter = KnowledgeBaseAdapterFactory.getAdapter((String) ragConfig.get("type"),
                    ragConfig);

            DatasetDTO.CreateReq createReq = ConvertUtils.sourceToTarget(dto, DatasetDTO.CreateReq.class);
            createReq.setName(SecurityUser.getUser().getUsername() + "_" + dto.getName());

            DatasetDTO.InfoVO ragResponse = adapter.createDataset(createReq);
            if (ragResponse == null || StringUtils.isBlank(ragResponse.getId())) {
                throw new RenException(ErrorCode.RAG_API_ERROR, "RAGcreatereturnno效: 缺失ID");
            }
            datasetId = ragResponse.getId();

            // 3. Local Save (Shadow)
            KnowledgeBaseEntity entity = ConvertUtils.sourceToTarget(dto, KnowledgeBaseEntity.class);

            // [Production Fix] 统onethis ID and RAGFlow ID，preventbeforeendcall /delete or /update when因 ID 混淆（this
            // UUID vs RAG UUID）导致 10163 error
            entity.setId(datasetId);
            entity.setDatasetId(datasetId);
            entity.setStatus(1); // Default Enabled

            // ✅ FULL PERSISTENCE: 严格全量回write (User Requirement)
            // usestrongtype DTO get，not againfrom Map manualparse Key
            entity.setTenantId(ragResponse.getTenantId());
            entity.setChunkMethod(ragResponse.getChunkMethod());
            entity.setEmbeddingModel(ragResponse.getEmbeddingModel());
            entity.setPermission(ragResponse.getPermission());

            if (StringUtils.isBlank(entity.getAvatar())) {
                entity.setAvatar(ragResponse.getAvatar());
            }

            // Parse Config (JSON)
            if (ragResponse.getParserConfig() != null) {
                entity.setParserConfig(JsonUtils.toJsonString(ragResponse.getParserConfig()));
            }

            // Numeric fields
            entity.setChunkCount(ragResponse.getChunkCount() != null ? ragResponse.getChunkCount() : 0L);
            entity.setDocumentCount(ragResponse.getDocumentCount() != null ? ragResponse.getDocumentCount() : 0L);
            entity.setTokenNum(ragResponse.getTokenNum() != null ? ragResponse.getTokenNum() : 0L);

            // 清empty creator/updater，let FieldMetaObjectHandler from SecurityUser automatic填充
            // ConvertUtils will DTO   creator=0 拷贝来，导致 strictInsertFill skip填充
            entity.setCreator(null);
            entity.setUpdater(null);

            knowledgeBaseDao.insert(entity);
            return ConvertUtils.sourceToTarget(entity, KnowledgeBaseDTO.class);
        } catch (Exception e) {
            log.error("RAGcreateorthissavefailed", e);
            // ifdatasetIdalreadygeneratebutinsavethiswhenfailed，尝试回滚RAG (Best Effort)
            if (StringUtils.isNotBlank(datasetId)) {
                try {
                    if (adapter != null)
                        adapter.deleteDataset(
                                DatasetDTO.BatchIdReq.builder().ids(Collections.singletonList(datasetId)).build());
                } catch (Exception rollbackEx) {
                    log.error("RAG回滚failed: {}", datasetId, rollbackEx);
                }
            }
            if (e instanceof RenException) {
                throw (RenException) e;
            }
            throw new RenException(ErrorCode.RAG_API_ERROR, "createKnowledge basefailed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("deprecation")
    public KnowledgeBaseDTO update(KnowledgeBaseDTO dto) {
        log.info("Update Service Called: ID={}, DatasetID={}", dto.getId(), dto.getDatasetId());
        KnowledgeBaseEntity entity = knowledgeBaseDao.selectById(dto.getId());
        if (entity == null) {
            log.error("Update failed: Entity not found for ID={}", dto.getId());
            throw new RenException(ErrorCode.Knowledge_Base_RECORD_NOT_EXISTS);
        }

        checkDuplicateName(dto.getName(), dto.getId());

        // verificationdatacollectionIDYesNoandotherrecord冲突
        if (StringUtils.isNotBlank(dto.getDatasetId())) {
            KnowledgeBaseEntity conflictEntity = knowledgeBaseDao.selectOne(
                    new QueryWrapper<KnowledgeBaseEntity>()
                            .eq("dataset_id", dto.getDatasetId())
                            .ne("id", dto.getId()));
            if (conflictEntity != null) {
                throw new RenException(ErrorCode.DB_RECORD_EXISTS);
            }
        }

        // RAG Update if needed
        if (StringUtils.isNotBlank(entity.getDatasetId()) && StringUtils.isNotBlank(dto.getRagModelId())) {
            try {
                // 🤖 AUTO-FILL: 若 DTO not传 ragModelId (极少情况)，尝试复用 Entity  
                if (StringUtils.isBlank(dto.getRagModelId())) {
                    dto.setRagModelId(entity.getRagModelId());
                }

                // [FIX] 智can补全：if DTO in 关keyfieldasempty，thenuse Entity in 旧value
                // ensure发to RAGFlow  requestcontainall必填item (Partial Update Support)
                if (StringUtils.isBlank(dto.getPermission())) {
                    dto.setPermission(entity.getPermission());
                }
                if (StringUtils.isBlank(dto.getChunkMethod())) {
                    dto.setChunkMethod(entity.getChunkMethod());
                }

                KnowledgeBaseAdapter adapter = getAdapterByModelId(dto.getRagModelId());
                if (adapter != null) {
                    DatasetDTO.UpdateReq updateReq = ConvertUtils.sourceToTarget(dto, DatasetDTO.UpdateReq.class);

                    // 1. 必填/核心fieldbefore缀process
                    if (StringUtils.isNotBlank(dto.getName())) {
                        updateReq.setName(SecurityUser.getUser().getUsername() + "_" + dto.getName());
                    }

                    // 2. parserconfigurationsupport (if DTO inhasstring形式 configuration，尝试convert，butpriorityfirstsuggestion DTO 化)
                    if (StringUtils.isNotBlank(dto.getParserConfig())) {
                        try {
                            DatasetDTO.ParserConfig parserConfig = JsonUtils.parseObject(dto.getParserConfig(),
                                    DatasetDTO.ParserConfig.class);
                            updateReq.setParserConfig(parserConfig);
                        } catch (Exception e) {
                            log.warn("parse parser_config failed，skipsynchronous", e);
                        }
                    }

                    adapter.updateDataset(entity.getDatasetId(), updateReq);
                    log.info("RAGupdatesuccess: {}", entity.getDatasetId());
                }
            } catch (Exception e) {
                log.error("RAGupdatefailed", e);
                // 恢复transactionconsistent：RAGfailedthen整回滚
                if (e instanceof RenException) {
                    throw (RenException) e;
                }
                throw new RenException(ErrorCode.RAG_API_ERROR, "RAGupdatefailed: " + e.getMessage());
            }
        }

        BeanUtils.copyProperties(dto, entity);
        knowledgeBaseDao.updateById(entity);

        // Clean cache
        redisUtils.delete(RedisKeys.getKnowledgeBaseCacheKey(entity.getId()));

        return ConvertUtils.sourceToTarget(entity, KnowledgeBaseDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDatasetId(String datasetId) {
        if (StringUtils.isBlank(datasetId)) {
            throw new RenException(ErrorCode.PARAMS_GET_ERROR);
        }

        KnowledgeBaseEntity entity = knowledgeBaseDao
                .selectOne(new QueryWrapper<KnowledgeBaseEntity>().eq("dataset_id", datasetId));

        // 1. 恢复 404 validate：findnot torecord抛exception
        if (entity == null) {
            log.warn("recorddoes not exist，datasetId: {}", datasetId);
            throw new RenException(ErrorCode.Knowledge_Base_RECORD_NOT_EXISTS);
        }
        log.info("findtorecord: ID={}, datasetId={}, ragModelId={}",
                entity.getId(), entity.getDatasetId(), entity.getRagModelId());

        // 2. RAG Delete (Strict Mode)
        // 恢复严格consistent：RAG deletefailedthen抛出exception，触发transaction回滚，not allowalreadydeletethisbut保留remote 脏data
        boolean apiDeleteSuccess = false;
        if (StringUtils.isNotBlank(entity.getRagModelId()) && StringUtils.isNotBlank(entity.getDatasetId())) {
            try {
                KnowledgeBaseAdapter adapter = getAdapterByModelId(entity.getRagModelId());
                if (adapter != null) {
                    adapter.deleteDataset(
                            DatasetDTO.BatchIdReq.builder().ids(Collections.singletonList(datasetId)).build());
                }
                apiDeleteSuccess = true;
            } catch (Exception e) {
                log.error("RAGdeletefailed，触发回滚", e);
                if (e instanceof RenException) {
                    throw (RenException) e;
                }
                throw new RenException(ErrorCode.RAG_API_ERROR, "RAGdeletefailed: " + e.getMessage());
            }
        } else {
            log.warn("datasetIdorragModelIdasempty，skipRAGdelete");
            apiDeleteSuccess = true; // noRAGdatacollection，视assuccess
        }

        // 3. Local Delete (Safe Order)
        // 恢复确顺order：first删childtable (Plugin Mapping)，again删主table (Entity)
        if (apiDeleteSuccess) {
            log.info("startdeleteai_agent_plugin_mappingtableandKnowledge baseID '{}' related mappingrecord", entity.getId());
            log.info("startdeleteassociateddata, entityId: {}", entity.getId());
            knowledgeBaseDao.deletePluginMappingByKnowledgeBaseId(entity.getId());
            log.info("pluginmappingrecorddeletecomplete");
            int deleteCount = knowledgeBaseDao.deleteById(entity.getId());
            log.info("thisdatalibrarydeleteresult: {}", deleteCount > 0 ? "success" : "failed");
            redisUtils.delete(RedisKeys.getKnowledgeBaseCacheKey(entity.getId()));
        }
    }

    @Override
    public List<KnowledgeBaseDTO> getByDatasetIdList(List<String> datasetIdList) {
        if (datasetIdList == null || datasetIdList.isEmpty()) {
            return Collections.emptyList();
        }
        // [Production Fix] batchcompatiblefind
        QueryWrapper<KnowledgeBaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("dataset_id", datasetIdList).or().in("id", datasetIdList);
        List<KnowledgeBaseEntity> list = knowledgeBaseDao.selectList(queryWrapper);
        return ConvertUtils.sourceToTarget(list, KnowledgeBaseDTO.class);
    }

    @Override
    public Map<String, Object> getRAGConfig(String ragModelId) {
        return getValidatedRAGConfig(ragModelId);
    }

    @Override
    public Map<String, Object> getRAGConfigByDatasetId(String datasetId) {
        KnowledgeBaseEntity entity = knowledgeBaseDao
                .selectOne(new QueryWrapper<KnowledgeBaseEntity>().eq("dataset_id", datasetId));
        if (entity == null || StringUtils.isBlank(entity.getRagModelId())) {
            throw new RenException(ErrorCode.RAG_CONFIG_NOT_FOUND);
        }
        return getRAGConfig(entity.getRagModelId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatistics(String datasetId, Integer docDelta, Long chunkDelta, Long tokenDelta) {
        log.info("递增updateKnowledge basestatistics: datasetId={}, docs={}, chunks={}, tokens={}", datasetId, docDelta, chunkDelta, tokenDelta);
        knowledgeBaseDao.updateStatsAfterChange(datasetId, docDelta, chunkDelta, tokenDelta);
    }

    @Override
    public List<ModelConfigEntity> getRAGModels() {
        return modelConfigDao.selectList(new QueryWrapper<ModelConfigEntity>()
                .select("id", "model_name", "config_json") // Explicitly select needed fields
                .eq("model_type", Constant.RAG_CONFIG_TYPE)
                .eq("is_enabled", 1)
                .orderByDesc("is_default")
                .orderByDesc("create_date"));
    }

    // --- Helpers ---

    private void checkDuplicateName(String name, String excludeId) {
        if (StringUtils.isBlank(name))
            return;
        QueryWrapper<KnowledgeBaseEntity> qw = new QueryWrapper<>();
        qw.eq("name", name).eq("creator", SecurityUser.getUserId());
        if (excludeId != null)
            qw.ne("id", excludeId);
        if (knowledgeBaseDao.selectCount(qw) > 0) {
            throw new RenException(ErrorCode.KNOWLEDGE_BASE_NAME_EXISTS);
        }
    }

    private KnowledgeBaseAdapter getAdapterByModelId(String modelId) {
        Map<String, Object> config = getValidatedRAGConfig(modelId);
        return KnowledgeBaseAdapterFactory.getAdapter((String) config.get("type"), config);
    }

    private Map<String, Object> getValidatedRAGConfig(String modelId) {
        ModelConfigEntity configEntity = modelConfigService.getModelByIdFromCache(modelId);
        if (configEntity == null || configEntity.getConfigJson() == null) {
            throw new RenException(ErrorCode.RAG_CONFIG_NOT_FOUND);
        }
        Map<String, Object> config = new HashMap<>(configEntity.getConfigJson());
        if (!config.containsKey("type")) {
            config.put("type", "ragflow");
        }
        return config;
    }
}