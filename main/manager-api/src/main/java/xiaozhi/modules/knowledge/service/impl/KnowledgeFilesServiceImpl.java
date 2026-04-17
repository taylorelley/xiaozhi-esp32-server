package xiaozhi.modules.knowledge.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.exception.ErrorCode;
import org.springframework.util.CollectionUtils;
import xiaozhi.common.exception.RenException;
import xiaozhi.modules.knowledge.dto.KnowledgeFilesDTO;
import xiaozhi.modules.knowledge.dto.document.ChunkDTO;
import xiaozhi.modules.knowledge.dto.document.RetrievalDTO;
import xiaozhi.modules.knowledge.dto.document.DocumentDTO;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.service.impl.BaseServiceImpl;
import xiaozhi.modules.knowledge.dao.DocumentDao;
import xiaozhi.modules.knowledge.entity.DocumentEntity;
import xiaozhi.modules.knowledge.rag.KnowledgeBaseAdapter;
import xiaozhi.modules.knowledge.rag.KnowledgeBaseAdapterFactory;
import xiaozhi.modules.knowledge.service.KnowledgeBaseService;
import xiaozhi.modules.knowledge.service.KnowledgeFilesService;

@Service
@Slf4j
public class KnowledgeFilesServiceImpl extends BaseServiceImpl<DocumentDao, DocumentEntity>
        implements KnowledgeFilesService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final DocumentDao documentDao;
    private final ObjectMapper objectMapper;
    private final RedisUtils redisUtils;

    public KnowledgeFilesServiceImpl(KnowledgeBaseService knowledgeBaseService,
            DocumentDao documentDao,
            ObjectMapper objectMapper,
            RedisUtils redisUtils) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.documentDao = documentDao;
        this.objectMapper = objectMapper;
        this.redisUtils = redisUtils;
    }

    @Lazy
    @Autowired
    private KnowledgeFilesService self;

    @Override
    public Map<String, Object> getRAGConfig(String ragModelId) {
        return knowledgeBaseService.getRAGConfig(ragModelId);
    }

    @Override
    public PageData<KnowledgeFilesDTO> getPageList(KnowledgeFilesDTO knowledgeFilesDTO, Integer page, Integer limit) {
        log.info("=== startgetKnowledge basedocumentlist (Local-First priority化版) ===");
        String datasetId = knowledgeFilesDTO.getDatasetId();
        if (StringUtils.isBlank(datasetId)) {
            throw new RenException(ErrorCode.RAG_DATASET_ID_AND_MODEL_ID_NOT_NULL);
        }

        // 1. getthisshadowtabledata (MyBatis-Plus pagination)
        Page<DocumentEntity> pageParams = new Page<>(page, limit);
        QueryWrapper<DocumentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dataset_id", datasetId);
        if (StringUtils.isNotBlank(knowledgeFilesDTO.getName())) {
            queryWrapper.like("name", knowledgeFilesDTO.getName());
        }
        if (StringUtils.isNotBlank(knowledgeFilesDTO.getRun())) {
            queryWrapper.eq("run", knowledgeFilesDTO.getRun());
        }
        if (StringUtils.isNotBlank(knowledgeFilesDTO.getStatus())) {
            queryWrapper.eq("status", knowledgeFilesDTO.getStatus());
        }
        queryWrapper.orderByDesc("created_at");

        // 2. executethisquery
        Page<DocumentEntity> iPage = documentDao.selectPage(pageParams, queryWrapper);

        // 3. manualconvert DTO
        List<KnowledgeFilesDTO> dtoList = new ArrayList<>();
        for (DocumentEntity entity : iPage.getRecords()) {
            dtoList.add(convertEntityToDTO(entity));
        }
        PageData<KnowledgeFilesDTO> pageData = new PageData<>(dtoList, iPage.getTotal());

        // 4. 动态statussynchronous (with限流and保护)
        // [Bug Fix] P1: 扩largesynchronous白名，CANCEL/FAIL alsoallow低频synchronoustosupport自愈
        if (pageData.getList() != null && !pageData.getList().isEmpty()) {
            KnowledgeBaseAdapter adapter = null;
            for (KnowledgeFilesDTO dto : pageData.getList()) {
                String runStatus = dto.getRun();
                // 高priorityfirst级synchronous: RUNNING/UNSTART (5second cooldown)
                boolean isActiveSync = "RUNNING".equals(runStatus) || "UNSTART".equals(runStatus);
                // 低频自愈synchronous: CANCEL/FAIL (60second cooldown), preventerrorstatus永久锁死
                boolean isRecoverySync = "CANCEL".equals(runStatus) || "FAIL".equals(runStatus);
                boolean needSync = isActiveSync || isRecoverySync;

                if (needSync) {
                    // 限流保护：活跃status 5 second cooldown，自愈status 60 second cooldown
                    long cooldownMs = isActiveSync ? 5000 : 60000;
                    DocumentEntity localEntity = documentDao.selectOne(new QueryWrapper<DocumentEntity>()
                            .eq("document_id", dto.getDocumentId()));
                    if (localEntity != null && localEntity.getLastSyncAt() != null) {
                        long diff = System.currentTimeMillis() - localEntity.getLastSyncAt().getTime();
                        if (diff < cooldownMs) {
                            continue;
                        }
                    }

                    // 延迟initializeadapter，onlyin确needsynchronouswhencreate
                    if (adapter == null) {
                        try {
                            Map<String, Object> ragConfig = knowledgeBaseService.getRAGConfigByDatasetId(datasetId);
                            adapter = KnowledgeBaseAdapterFactory.getAdapter(extractAdapterType(ragConfig), ragConfig);
                        } catch (Exception e) {
                            log.warn("synchronous断：unable toinitializeadapter, {}", e.getMessage());
                            break;
                        }
                    }
                    // [关key修复] recordsynchronousbefore  Token number，used for计算增量
                    Long oldTokenCount = dto.getTokenCount() != null ? dto.getTokenCount() : 0L;

                    syncDocumentStatusWithRAG(dto, adapter);

                    // 计算增量andupdateKnowledge basestatistics (and定whentask保持consistent)
                    Long newTokenCount = dto.getTokenCount() != null ? dto.getTokenCount() : 0L;
                    Long tokenDelta = newTokenCount - oldTokenCount;
                    if (tokenDelta != 0) {
                        knowledgeBaseService.updateStatistics(datasetId, 0, 0L, tokenDelta);
                        log.info("懒加载synchronous: 修Knowledge basestatistics, docId={}, tokenDelta={}", dto.getDocumentId(), tokenDelta);
                    }
                }
            }
        }

        log.info("getdocumentlistsuccess，total: {}", pageData.getTotal());
        return pageData;
    }

    /**
     * willthisrecordentityconvert toDTO，manualfor齐not consistentfield (size -> fileSize, type -> fileType)
     */
    private KnowledgeFilesDTO convertEntityToDTO(DocumentEntity entity) {
        if (entity == null) {
            return null;
        }
        KnowledgeFilesDTO dto = new KnowledgeFilesDTO();
        // 1. basefield拷贝
        BeanUtils.copyProperties(entity, dto);

        // Issue 2: 修 ID 语义。beforeend习惯use id asoperationPrimary key。
        // inthismodule，应始终willremote documentId mappingas DTO   id，ensurebeforeendindetails/deleteetc.operationwhen ID consistent。
        dto.setId(entity.getDocumentId());

        // 2. willthisrecordentityconvert toDTO，manualfor齐not consistentfield (size -> fileSize, type -> fileType)
        dto.setFileSize(entity.getSize());
        dto.setFileType(entity.getType());
        dto.setRun(entity.getRun());
        dto.setChunkCount(entity.getChunkCount());
        dto.setTokenCount(entity.getTokenCount());
        dto.setError(entity.getError());

        // 3. custom metadata JSON deserialize (Issue 3)
        if (StringUtils.isNotBlank(entity.getMetaFields())) {
            try {
                dto.setMetaFields(objectMapper.readValue(entity.getMetaFields(),
                        new TypeReference<Map<String, Object>>() {
                        }));
            } catch (Exception e) {
                log.warn("deserialize MetaFields failed, entityId: {}, error: {}", entity.getId(), e.getMessage());
            }
        }

        // 4. parseconfiguration JSON deserialize
        if (StringUtils.isNotBlank(entity.getParserConfig())) {
            try {
                dto.setParserConfig(objectMapper.readValue(entity.getParserConfig(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                        }));
            } catch (Exception e) {
                log.warn("deserialize ParserConfig failed, entityId: {}, error: {}", entity.getId(), e.getMessage());
            }
        }
        return dto;

    }

    /**
     * synchronousdocumentstatusandRAG际status
     * priority化statussynchronous逻辑，ensureparsestatuscan够normal显示
     * onlyhaswhendocumenthassliceandparsetime超30secondswhen，onlyupdateascompletestatus
     */
    /**
     * synchronousdocumentstatusandRAG际status (增strong型：support外部传入adapter)
     */
    private void syncDocumentStatusWithRAG(KnowledgeFilesDTO dto, KnowledgeBaseAdapter adapter) {
        if (dto == null || StringUtils.isBlank(dto.getDocumentId()) || adapter == null) {
            return;
        }

        String documentId = dto.getDocumentId();
        String datasetId = dto.getDatasetId();

        try {
            // usestrongtype ListReq 配合 ID filter来getstatus
            DocumentDTO.ListReq listReq = DocumentDTO.ListReq.builder()
                    .id(documentId)
                    .page(1)
                    .pageSize(1)
                    .build();

            PageData<KnowledgeFilesDTO> remoteList = adapter.getDocumentList(datasetId, listReq);

            if (remoteList != null && remoteList.getList() != null && !remoteList.getList().isEmpty()) {
                KnowledgeFilesDTO remoteDto = remoteList.getList().get(0);
                String remoteStatus = remoteDto.getStatus();

                // 核心statusfor齐判别逻辑
                boolean statusChanged = remoteStatus != null && !remoteStatus.equals(dto.getStatus());
                boolean runChanged = remoteDto.getRun() != null && !remoteDto.getRun().equals(dto.getRun());
                boolean isProcessing = "RUNNING".equals(remoteDto.getRun()) || "UNSTART".equals(remoteDto.getRun());

                // onlyneed tostatushas变，orrunstatushas变，orfile仍inparse（when刷progress），thenexecutesynchronous
                if (statusChanged || runChanged || isProcessing) {
                    log.info("shadowsynchronous：status变化={}，parse={}，document={}，mostnewstatus={}，progress={}",
                            statusChanged, isProcessing, documentId, remoteStatus, remoteDto.getProgress());

                    // 1. synchronous内store DTO
                    dto.setStatus(remoteStatus);
                    dto.setRun(remoteDto.getRun());
                    dto.setProgress(remoteDto.getProgress());
                    dto.setChunkCount(remoteDto.getChunkCount());
                    dto.setTokenCount(remoteDto.getTokenCount());
                    dto.setError(remoteDto.getError());
                    dto.setProcessDuration(remoteDto.getProcessDuration());
                    dto.setThumbnail(remoteDto.getThumbnail());

                    // 2. synchronousthisshadowtable
                    UpdateWrapper<DocumentEntity> updateWrapper = new UpdateWrapper<DocumentEntity>()
                            .set("status", remoteStatus)
                            .set("run", remoteDto.getRun())
                            .set("progress", remoteDto.getProgress())
                            .set("chunk_count", remoteDto.getChunkCount())
                            .set("token_count", remoteDto.getTokenCount())
                            .set("error", remoteDto.getError())
                            .set("process_duration", remoteDto.getProcessDuration())
                            .set("thumbnail", remoteDto.getThumbnail())
                            .eq("document_id", documentId)
                            .eq("dataset_id", datasetId);

                    // serializedatasynchronous
                    if (remoteDto.getMetaFields() != null) {
                        try {
                            updateWrapper.set("meta_fields",
                                    objectMapper.writeValueAsString(remoteDto.getMetaFields()));
                        } catch (Exception e) {
                            log.warn("synchronousdataserializefailed: {}", e.getMessage());
                        }
                    }

                    // priorityfirstsynchronous RAG 侧 updatetime，避免thissynchronousrowas覆盖businessupdatetime
                    Date lastUpdate = remoteDto.getUpdatedAt() != null ? remoteDto.getUpdatedAt() : new Date();
                    updateWrapper.set("updated_at", lastUpdate);
                    updateWrapper.set("last_sync_at", new Date()); // recordshadowlibrarysynchronoustime

                    documentDao.update(null, updateWrapper);
                }
            } else {
                // Issue 6: remotelistasempty，可canYesdocumentalreadydelete，also可canYesadaptercall出question
                // [Bug Fix] P2: onlywhenremote确return合法emptylistwhenonly标记 CANCEL
                // simultaneouslyupdate last_sync_at，配合 P1 冷却机制prevent高频误判
                log.warn("remotesynchronous感知：RAGFlow returnemptydocumentlist, docId={}, currentthisstatus={}",
                        documentId, dto.getRun());
                dto.setRun("CANCEL");
                dto.setError("documentinremoteservicealreadyisdelete");

                documentDao.update(null, new UpdateWrapper<DocumentEntity>()
                        .set("run", "CANCEL")
                        .set("error", "documentinremoteservicealreadyisdelete")
                        .set("updated_at", new Date())
                        .set("last_sync_at", new Date())
                        .eq("document_id", documentId));
            }
        } catch (Exception e) {
            // [Bug Fix] P2: adaptercallexceptionwhennot 标记 CANCEL，避免因network/deserializequestion导致误判
            // onlyrecordLog，etc.下timessynchronous周期re-试
            log.warn("synchronousdocumentstatuswhenadaptercallfailed(not 标记CANCEL), documentId: {}, error: {}",
                    documentId, e.getMessage());
        }
    }

    @Override
    public DocumentDTO.InfoVO getByDocumentId(String documentId, String datasetId) {
        if (StringUtils.isBlank(documentId) || StringUtils.isBlank(datasetId)) {
            throw new RenException(ErrorCode.RAG_DATASET_ID_AND_MODEL_ID_NOT_NULL);
        }

        log.info("=== startaccording todocumentIdgetdocument ===");
        log.info("documentId: {}, datasetId: {}", documentId, datasetId);

        try {
            // getRAGconfiguration
            Map<String, Object> ragConfig = knowledgeBaseService.getRAGConfigByDatasetId(datasetId);

            // extractadaptertype
            String adapterType = extractAdapterType(ragConfig);

            // useadapter工厂getadapterexample
            KnowledgeBaseAdapter adapter = KnowledgeBaseAdapterFactory.getAdapter(adapterType, ragConfig);

            // useadaptergetdocumentdetails
            DocumentDTO.InfoVO info = adapter.getDocumentById(datasetId, documentId);

            if (info != null) {
                log.info("getdocumentdetailssuccess，documentId: {}", documentId);
                return info;
            } else {
                throw new RenException(ErrorCode.Knowledge_Base_RECORD_NOT_EXISTS);
            }

        } catch (Exception e) {
            log.error("according todocumentIdgetdocumentfailed: {}", e.getMessage(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "null";
            if (e instanceof RenException) {
                throw (RenException) e;
            }
            throw new RenException(ErrorCode.RAG_API_ERROR, errorMessage);
        } finally {
            log.info("=== according todocumentIdgetdocumentoperationend ===");
        }
    }

    @Override
    public KnowledgeFilesDTO uploadDocument(String datasetId, MultipartFile file, String name,
            Map<String, Object> metaFields, String chunkMethod,
            Map<String, Object> parserConfig) {
        if (StringUtils.isBlank(datasetId) || file == null || file.isEmpty()) {
            throw new RenException(ErrorCode.PARAMS_GET_ERROR);
        }

        log.info("=== startdocumentuploadoperation (strongconsistentpriority化) ===");

        // 1. prepare工作 (non-transaction)
        String fileName = StringUtils.isNotBlank(name) ? name : file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new RenException(ErrorCode.RAG_FILE_NAME_NOT_NULL);
        }

        log.info("1. 发起remoteupload: datasetId={}, fileName={}", datasetId, fileName);

        // getadapter (non-transaction)
        Map<String, Object> ragConfig = knowledgeBaseService.getRAGConfigByDatasetId(datasetId);
        KnowledgeBaseAdapter adapter = KnowledgeBaseAdapterFactory.getAdapter(extractAdapterType(ragConfig), ragConfig);

        // constructstrongtyperequest DTO
        DocumentDTO.UploadReq uploadReq = DocumentDTO.UploadReq.builder()
                .datasetId(datasetId)
                .file(file)
                .name(fileName)
                .metaFields(metaFields)
                .build();

        // convertchunkmethod (String -> Enum)
        if (StringUtils.isNotBlank(chunkMethod)) {
            try {
                uploadReq.setChunkMethod(DocumentDTO.InfoVO.ChunkMethod.valueOf(chunkMethod.toUpperCase()));
            } catch (Exception e) {
                log.warn("no效 chunkmethod: {}, willuseafter台defaultconfiguration", chunkMethod);
            }
        }

        // convertparseconfiguration (Map -> DTO)
        if (parserConfig != null && !parserConfig.isEmpty()) {
            uploadReq.setParserConfig(objectMapper.convertValue(parserConfig, DocumentDTO.InfoVO.ParserConfig.class));
        }

        // executeremoteupload (耗when IO，intransaction之外)
        KnowledgeFilesDTO result = adapter.uploadDocument(uploadReq);

        if (result == null || StringUtils.isBlank(result.getDocumentId())) {
            throw new RenException(ErrorCode.RAG_API_ERROR, "remoteuploadsuccessbutnotreturnvalid DocumentID");
        }

        // 2. this持久化 (via self calltoactivation @Transactional 代理)
        log.info("2. synchronoussavethisshadowrecord: documentId={}", result.getDocumentId());
        self.saveDocumentShadow(datasetId, result, fileName, chunkMethod, parserConfig);

        log.info("=== documentuploadandshadowrecordsavesuccess ===");
        return result;
    }

    /**
     * 原child化saveshadowrecord，ensurethisdata绝forconsistent
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDocumentShadow(String datasetId, KnowledgeFilesDTO result, String originalName, String chunkMethod,
            Map<String, Object> parserConfig) {
        DocumentEntity entity = new DocumentEntity();
        entity.setDatasetId(datasetId);
        entity.setDocumentId(result.getDocumentId());
        entity.setName(StringUtils.isNotBlank(result.getName()) ? result.getName() : originalName);
        entity.setSize(result.getFileSize());
        entity.setType(getFileType(entity.getName()));
        entity.setChunkMethod(chunkMethod);

        if (parserConfig != null) {
            try {
                entity.setParserConfig(objectMapper.writeValueAsString(parserConfig));
            } catch (Exception e) {
                log.warn("serializeparseconfigurationfailed: {}", e.getMessage());
            }
        }

        entity.setStatus(result.getStatus() != null ? result.getStatus() : "1");
        entity.setRun(result.getRun());
        entity.setProgress(result.getProgress());
        entity.setThumbnail(result.getThumbnail());
        entity.setProcessDuration(result.getProcessDuration());
        entity.setSourceType(result.getSourceType());
        entity.setError(result.getError());
        entity.setChunkCount(result.getChunkCount());
        entity.setTokenCount(result.getTokenCount());
        entity.setEnabled(1);

        // 持久化data
        if (result.getMetaFields() != null) {
            try {
                entity.setMetaFields(objectMapper.writeValueAsString(result.getMetaFields()));
            } catch (Exception e) {
                log.warn("持久化shadowdatafailed: {}", e.getMessage());
            }
        }

        // priorityfirstsynchronous RAG 侧 timestamp，若nothenusethistime
        entity.setCreatedAt(result.getCreatedAt() != null ? result.getCreatedAt() : new Date());
        entity.setUpdatedAt(result.getUpdatedAt() != null ? result.getUpdatedAt() : new Date());

        // insertshadowtable (若failedwill抛出exception，触发call方报错，ensure Local-First listconsistent)
        documentDao.insert(entity);

        // Issue 4: synchronous递增datacollectiondocumenttotalstatistics，保持parentchildtableconsistent
        knowledgeBaseService.updateStatistics(datasetId, 1, 0L, 0L);
        log.info("alreadysynchronous递增datacollectionstatistics: datasetId={}", datasetId);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void deleteDocuments(String datasetId, DocumentDTO.BatchIdReq req) {
        if (StringUtils.isBlank(datasetId) || req == null || req.getIds() == null || req.getIds().isEmpty()) {
            throw new RenException(ErrorCode.RAG_DATASET_ID_AND_MODEL_ID_NOT_NULL);
        }

        List<String> documentIds = req.getIds();
        log.info("=== startbatchdeletedocument: datasetId={}, count={} ===", datasetId, documentIds.size());

        // 1. batchPermissionandstatus预审
        List<DocumentEntity> entities = documentDao.selectList(
                new QueryWrapper<DocumentEntity>()
                        .eq("dataset_id", datasetId)
                        .in("document_id", documentIds));

        if (entities.size() != documentIds.size()) {
            log.warn("部分documentdoes not existor归权exception: 预期={}, 际={}", documentIds.size(), entities.size());
            throw new RenException(ErrorCode.NO_PERMISSION);
        }

        long totalChunkDelta = 0;
        long totalTokenDelta = 0;

        for (DocumentEntity entity : entities) {
            // 拦截inparse document deleterequest
            // [Bug Fix] determineparse应this用 run field(RUNNING), whilenon- status field
            // status="1" Yes"enable/normal" 意思, not Yes"parse"
            if ("RUNNING".equals(entity.getRun())) {
                log.warn("拦截parsefile deleterequest: docId={}", entity.getDocumentId());
                throw new RenException(ErrorCode.RAG_DOCUMENT_PARSING_DELETE_ERROR);
            }
            totalChunkDelta += entity.getChunkCount() != null ? entity.getChunkCount() : 0L;
            totalTokenDelta += entity.getTokenCount() != null ? entity.getTokenCount() : 0L;
        }

        // 2. getadapter (non-transaction)
        Map<String, Object> ragConfig = knowledgeBaseService.getRAGConfigByDatasetId(datasetId);
        KnowledgeBaseAdapter adapter = KnowledgeBaseAdapterFactory.getAdapter(extractAdapterType(ragConfig), ragConfig);

        // 3. executeremotedelete
        try {
            adapter.deleteDocument(datasetId, req);
            log.info("remotebatchdeleterequestsuccess");
        } catch (Exception e) {
            log.warn("remotedeleterequest部分orAllfailed: {}", e.getMessage());
        }

        // 4. 原child化clean upthisshadowrecordandsynchronousstatisticsdata
        self.deleteDocumentShadows(documentIds, datasetId, totalChunkDelta, totalTokenDelta);

        // 5. clean upcache
        try {
            String cacheKey = RedisKeys.getKnowledgeBaseCacheKey(datasetId);
            redisUtils.delete(cacheKey);
            log.info("already驱逐datacollectioncache: {}", cacheKey);
        } catch (Exception e) {
            log.warn("驱逐 Redis cachefailed: {}", e.getMessage());
        }

        log.info("=== batchdocumentclean upcomplete ===");
    }

    /**
     * batch原child化deleteshadowrecordandsynchronousparenttablestatistics
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocumentShadows(List<String> documentIds, String datasetId, Long chunkDelta, Long tokenDelta) {
        // 1. 物理deleterecord
        int deleted = documentDao.delete(
                new QueryWrapper<DocumentEntity>()
                        .eq("dataset_id", datasetId)
                        .in("document_id", documentIds));

        if (deleted > 0) {
            // 2. synchronousupdatedatacollectionstatisticsinformation
            knowledgeBaseService.updateStatistics(datasetId, -documentIds.size(), -chunkDelta, -tokenDelta);
            log.info("alreadysynchronous扣减datacollectionstatistics: datasetId={}, chunks={}, tokens={}", datasetId, chunkDelta, tokenDelta);
        }
    }

    /**
     * getFile type - supportRAG四种documentformattype
     */
    private String getFileType(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            log.warn("File nameasempty，returnunknowntype");
            return "unknown";
        }

        try {
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
                String extension = fileName.substring(lastDotIndex + 1).toLowerCase();

                // documentformattype
                String[] documentTypes = { "pdf", "doc", "docx", "txt", "md", "mdx" };
                String[] spreadsheetTypes = { "csv", "xls", "xlsx" };
                String[] presentationTypes = { "ppt", "pptx" };

                // checkdocumenttype
                for (String type : documentTypes) {
                    if (type.equals(extension)) {
                        return "document";
                    }
                }

                // checktable格type
                for (String type : spreadsheetTypes) {
                    if (type.equals(extension)) {
                        return "spreadsheet";
                    }
                }
                // check幻灯片type
                for (String type : presentationTypes) {
                    if (type.equals(extension)) {
                        return "presentation";
                    }
                }
                // returnoriginalextensionasFile type
                return extension;
            }
            return "unknown";
        } catch (Exception e) {
            log.error("getFile typefailed: ", e);
            return "unknown";
        }
    }

    /**
     * fromRAGconfigurationextractadaptertype
     */
    private String extractAdapterType(Map<String, Object> config) {
        if (config == null) {
            throw new RenException(ErrorCode.RAG_CONFIG_NOT_FOUND);
        }

        // fromconfigurationextracttypefield
        String adapterType = (String) config.get("type");
        if (StringUtils.isBlank(adapterType)) {
            throw new RenException(ErrorCode.RAG_ADAPTER_TYPE_NOT_FOUND);
        }

        // verificationadaptertypeYesNoalreadyregister
        if (!KnowledgeBaseAdapterFactory.isAdapterTypeRegistered(adapterType)) {
            throw new RenException(ErrorCode.RAG_ADAPTER_TYPE_NOT_SUPPORTED, "adaptertypenotregister: " + adapterType);
        }

        return adapterType;
    }

    @Override
    public boolean parseDocuments(String datasetId, List<String> documentIds) {
        if (StringUtils.isBlank(datasetId) || documentIds == null || documentIds.isEmpty()) {
            throw new RenException(ErrorCode.RAG_DATASET_ID_AND_MODEL_ID_NOT_NULL);
        }

        log.info("=== startparsedocument（chunk） ===");
        log.info("datasetId: {}, documentIds: {}", datasetId, documentIds);

        try {
            // getRAGconfiguration
            Map<String, Object> ragConfig = knowledgeBaseService.getRAGConfigByDatasetId(datasetId);

            // extractadaptertype
            String adapterType = extractAdapterType(ragConfig);

            // getKnowledge baseadapter
            KnowledgeBaseAdapter adapter = KnowledgeBaseAdapterFactory.getAdapter(adapterType, ragConfig);

            log.debug("parsedocumentparameter: documentIds: {}", documentIds);

            // calladapterparsedocument
            boolean result = adapter.parseDocuments(datasetId, documentIds);

            if (result) {
                log.info("Document parsingcommandsendsuccess，preparesynchronousthisshadowlibrarystatus，datasetId: {}, documentIds: {}", datasetId, documentIds);
                // instructionsuccessafterimmediatelyupdatethisshadowstatusas RUNNING and parse(1)，ensure Local-First listcanimmediately反馈
                documentDao.update(null, new UpdateWrapper<DocumentEntity>()
                        .set("run", "RUNNING")
                        .set("status", "1")
                        .set("updated_at", new Date())
                        .eq("dataset_id", datasetId)
                        .in("document_id", documentIds));

                log.info("documentthisstatusalreadyupdateas RUNNING");
            } else {
                log.error("Document parsingfailed，datasetId: {}, documentIds: {}", datasetId, documentIds);
                throw new RenException(ErrorCode.RAG_API_ERROR, "Document parsingfailed");
            }

            return result;

        } catch (Exception e) {
            log.error("parsedocumentfailed: {}", e.getMessage(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "null";
            if (e instanceof RenException) {
                throw (RenException) e;
            }
            throw new RenException(ErrorCode.RAG_API_ERROR, errorMessage);
        } finally {
            log.info("=== parsedocumentoperationend ===");
        }
    }

    @Override
    public ChunkDTO.ListVO listChunks(String datasetId, String documentId, ChunkDTO.ListReq req) {
        if (StringUtils.isBlank(datasetId) || StringUtils.isBlank(documentId)) {
            throw new RenException(ErrorCode.RAG_DATASET_ID_AND_MODEL_ID_NOT_NULL);
        }

        log.info("=== startlistslice: datasetId={}, documentId={}, req={} ===", datasetId, documentId, req);

        try {
            Map<String, Object> ragConfig = knowledgeBaseService.getRAGConfigByDatasetId(datasetId);
            KnowledgeBaseAdapter adapter = KnowledgeBaseAdapterFactory.getAdapter(extractAdapterType(ragConfig),
                    ragConfig);

            ChunkDTO.ListVO result = adapter.listChunks(datasetId, documentId, req);
            log.info("slicelistgetsuccess: datasetId={}, total={}", datasetId, result.getTotal());
            return result;
        } catch (Exception e) {
            log.error("listslicefailed: {}", e.getMessage(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "null";
            if (e instanceof RenException) {
                throw (RenException) e;
            }
            throw new RenException(ErrorCode.RAG_API_ERROR, errorMessage);
        } finally {
            log.info("=== listsliceoperationend ===");
        }
    }

    @Override
    public RetrievalDTO.ResultVO retrievalTest(RetrievalDTO.TestReq req) {
        if (CollectionUtils.isEmpty(req.getDatasetIds())) {
            throw new RenException("notspecifiedrecalltest Knowledge base");
        }

        log.info("=== startrecalltest: req={} ===", req);

        try {
            Map<String, Object> ragConfig = knowledgeBaseService.getRAGConfigByDatasetId(req.getDatasetIds().get(0));
            KnowledgeBaseAdapter adapter = KnowledgeBaseAdapterFactory.getAdapter(extractAdapterType(ragConfig),
                    ragConfig);

            RetrievalDTO.ResultVO result = adapter.retrievalTest(req);
            log.info("recalltestsuccess: total={}", result != null ? result.getTotal() : 0);
            return result;
        } catch (Exception e) {
            log.error("recalltestfailed: {}", e.getMessage(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "null";
            if (e instanceof RenException) {
                throw (RenException) e;
            }
            throw new RenException(ErrorCode.RAG_API_ERROR, errorMessage);
        } finally {
            log.info("=== recalltestoperationend ===");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocumentsByDatasetId(String datasetId) {
        log.info("cascadeclean updatacollectiondocument: datasetId={}", datasetId);
        List<DocumentEntity> list = documentDao
                .selectList(new QueryWrapper<DocumentEntity>().eq("dataset_id", datasetId));
        if (list == null || list.isEmpty())
            return;

        List<String> docIds = list.stream().map(DocumentEntity::getDocumentId).toList();

        // 封包call现hasdelete逻辑 (含 RAG 物理delete)
        DocumentDTO.BatchIdReq req = DocumentDTO.BatchIdReq.builder().ids(docIds).build();
        this.deleteDocuments(datasetId, req);
    }

    @Override
    public void syncRunningDocuments() {
        // 1. queryall RUNNING status document
        List<DocumentEntity> runningDocs = documentDao.selectList(
                new QueryWrapper<DocumentEntity>()
                        .eq("run", "RUNNING")
                        .eq("status", "1") // onlysynchronousenable document
        );

        if (runningDocs == null || runningDocs.isEmpty()) {
            return;
        }

        log.info("定whentask: 发现 {} documentinparse，startsynchronous...", runningDocs.size());

        // 2. by DatasetID 分group，复用 Adapter
        Map<String, List<DocumentEntity>> groupedDocs = runningDocs.stream()
                .collect(java.util.stream.Collectors.groupingBy(DocumentEntity::getDatasetId));

        groupedDocs.forEach((datasetId, docs) -> {
            KnowledgeBaseAdapter adapter = null;
            try {
                // initialize Adapter (每datacollectiononlyinitializeonetimes)
                Map<String, Object> ragConfig = knowledgeBaseService.getRAGConfigByDatasetId(datasetId);
                adapter = KnowledgeBaseAdapterFactory.getAdapter(extractAdapterType(ragConfig), ragConfig);
            } catch (Exception e) {
                log.warn("unable toasdatacollection {} initializeadapter，skipsynchronous: {}", datasetId, e.getMessage());
                return;
            }

            for (DocumentEntity doc : docs) {
                try {
                    // construct临when DTO 传tosynchronousmethod
                    KnowledgeFilesDTO dto = convertEntityToDTO(doc);
                    // recordsynchronousbefore  Token number
                    Long oldTokenCount = dto.getTokenCount() != null ? dto.getTokenCount() : 0L;

                    syncDocumentStatusWithRAG(dto, adapter);

                    // 3. [关key修复] 计算增量andupdateKnowledge basestatistics
                    Long newTokenCount = dto.getTokenCount() != null ? dto.getTokenCount() : 0L;
                    Long tokenDelta = newTokenCount - oldTokenCount;

                    // onlywhenstatus变as SUCCESS and Token numberhas变化whenupdatestatistics
                    if (tokenDelta != 0) {
                        knowledgeBaseService.updateStatistics(datasetId, 0, 0L, tokenDelta);
                        log.info("定whentask: synchronous修Knowledge basestatistics, docId={}, tokenDelta={}", dto.getDocumentId(), tokenDelta);
                    }
                } catch (Exception e) {
                    log.error("synchronousdocument {} failed: {}", doc.getDocumentId(), e.getMessage());
                }
            }
        });
    }
}