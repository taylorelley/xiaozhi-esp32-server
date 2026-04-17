package xiaozhi.modules.knowledge.rag.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.AbstractResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.page.PageData;
import xiaozhi.modules.knowledge.dto.KnowledgeFilesDTO;
import xiaozhi.modules.knowledge.dto.dataset.DatasetDTO;
import xiaozhi.modules.knowledge.dto.document.ChunkDTO;
import xiaozhi.modules.knowledge.dto.document.RetrievalDTO;
import xiaozhi.modules.knowledge.dto.document.DocumentDTO;
import xiaozhi.modules.knowledge.rag.KnowledgeBaseAdapter;
import xiaozhi.modules.knowledge.rag.RAGFlowClient;

/**
 * RAGFlowKnowledge baseadapterimplement
 * <p>
 * re-构Description (Refactoring Note):
 * thisclassalreadyupgradeasuse {@link RAGFlowClient} 统oneprocess HTTP 通信。
 * 解决旧代code Timeout 缺失、Error Handling 分散 question。
 * </p>
 */
@Slf4j
public class RAGFlowAdapter extends KnowledgeBaseAdapter {

    private static final String ADAPTER_TYPE = "ragflow";

    private Map<String, Object> config;
    private ObjectMapper objectMapper;
    // Client example，initializewhencreate
    private RAGFlowClient client;

    public RAGFlowAdapter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getAdapterType() {
        return ADAPTER_TYPE;
    }

    @Override
    public void initialize(Map<String, Object> config) {
        this.config = config;
        validateConfig(config);

        String baseUrl = getConfigValue(config, "base_url", "baseUrl");
        String apiKey = getConfigValue(config, "api_key", "apiKey");

        // initialize Client，defaulttimeout 30s，可via config 扩展
        int timeout = 30;
        Object timeoutObj = getConfigValue(config, "timeout", "timeout");
        if (timeoutObj != null) {
            try {
                timeout = Integer.parseInt(timeoutObj.toString());
            } catch (Exception e) {
                log.warn("parsetimeoutconfigurationfailed，usedefaultvalue 30s");
            }
        }
        this.client = new RAGFlowClient(baseUrl, apiKey, timeout);
        log.info("RAGFlowadapterinitializecomplete，Clientalreadythen绪");
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) {
            throw new RenException(ErrorCode.RAG_CONFIG_NOT_FOUND);
        }

        String baseUrl = getConfigValue(config, "base_url", "baseUrl");
        String apiKey = getConfigValue(config, "api_key", "apiKey");

        if (StringUtils.isBlank(baseUrl)) {
            throw new RenException(ErrorCode.RAG_API_ERROR_URL_NULL);
        }

        if (StringUtils.isBlank(apiKey)) {
            throw new RenException(ErrorCode.RAG_API_ERROR_API_KEY_NULL);
        }

        if (apiKey.contains("you")) {
            throw new RenException(ErrorCode.RAG_API_ERROR_API_KEY_INVALID);
        }

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new RenException(ErrorCode.RAG_API_ERROR_URL_INVALID);
        }

        return true;
    }

    /**
     * helper method：support多种key名getconfiguration（compatible camelCase and snake_case）
     */
    private String getConfigValue(Map<String, Object> config, String snakeKey, String camelKey) {
        if (config.containsKey(snakeKey)) {
            return (String) config.get(snakeKey);
        }
        if (config.containsKey(camelKey)) {
            return (String) config.get(camelKey);
        }
        return null;
    }

    /**
     * helper method：ensure Client alreadyinitialize
     */
    private RAGFlowClient getClient() {
        if (this.client == null) {
            // 尝试re-newinitialize
            if (this.config != null) {
                initialize(this.config);
            } else {
                throw new RenException(ErrorCode.RAG_CONFIG_NOT_FOUND, "adapternotinitialize"); // 应this抛出 RuntimeException
            }
        }
        return this.client;
    }

    private RenException convertToRenException(Exception e) {
        if (e instanceof RenException) {
            return (RenException) e;
        }
        return new RenException(ErrorCode.RAG_API_ERROR, e.getMessage());
    }

    @Override
    public PageData<KnowledgeFilesDTO> getDocumentList(String datasetId, DocumentDTO.ListReq req) {
        try {
            log.info("=== [RAGFlow] getdocumentlist: datasetId={} ===", datasetId);

            // use Jackson will DTO 转as Map asqueryparameter
            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.convertValue(req, Map.class);

            Map<String, Object> response = getClient().get("/api/v1/datasets/" + datasetId + "/documents", params);

            Object dataObj = response.get("data");
            return parseDocumentListResponse(dataObj, req.getPage() != null ? req.getPage() : 1,
                    req.getPageSize() != null ? req.getPageSize() : 10);

        } catch (Exception e) {
            log.error("getdocumentlistfailed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public DocumentDTO.InfoVO getDocumentById(String datasetId, String documentId) {
        try {
            log.info("=== [RAGFlow] getdocumentdetails: datasetId={}, documentId={} ===", datasetId, documentId);
            DocumentDTO.ListReq req = DocumentDTO.ListReq.builder()
                    .id(documentId)
                    .page(1)
                    .pageSize(1)
                    .build();

            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.convertValue(req, Map.class);
            Map<String, Object> response = getClient().get("/api/v1/datasets/" + datasetId + "/documents", params);

            Object dataObj = response.get("data");
            if (dataObj instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                List<?> documents = (List<?>) dataMap.get("docs");
                if (documents != null && !documents.isEmpty()) {
                    return objectMapper.convertValue(documents.get(0), DocumentDTO.InfoVO.class);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("getdocumentdetailsfailed: documentId={}", documentId, e);
            throw convertToRenException(e);
        }
    }

    @Override
    public KnowledgeFilesDTO uploadDocument(DocumentDTO.UploadReq req) {
        String datasetId = req.getDatasetId();
        MultipartFile file = req.getFile();
        try {
            log.info("=== [RAGFlow] uploaddocument: datasetId={} ===", datasetId);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartFileResource(file));

            if (StringUtils.isNotBlank(req.getName())) {
                body.add("name", req.getName());
            }
            if (req.getMetaFields() != null && !req.getMetaFields().isEmpty()) {
                body.add("meta", objectMapper.writeValueAsString(req.getMetaFields()));
            }
            if (req.getChunkMethod() != null) {
                // willenumerationvalue转as RAGFlow 期pending string（e.g. NAIVE -> naive）
                body.add("chunk_method", req.getChunkMethod().name().toLowerCase());
            }
            if (req.getParserConfig() != null) {
                body.add("parser_config", objectMapper.writeValueAsString(req.getParserConfig()));
            }
            if (StringUtils.isNotBlank(req.getParentPath())) {
                body.add("parent_path", req.getParentPath());
            }

            Map<String, Object> response = getClient().postMultipart("/api/v1/datasets/" + datasetId + "/documents",
                    body);

            Object dataObj = response.get("data");
            return parseUploadResponse(dataObj, datasetId, file);

        } catch (Exception e) {
            log.error("documentuploadfailed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public PageData<KnowledgeFilesDTO> getDocumentListByStatus(String datasetId, Integer status, Integer page,
            Integer limit) {
        List<DocumentDTO.InfoVO.RunStatus> runStatusList = null;
        if (status != null) {
            runStatusList = new ArrayList<>();
            switch (status) {
                case 0:
                    runStatusList.add(DocumentDTO.InfoVO.RunStatus.UNSTART);
                    break;
                case 1:
                    runStatusList.add(DocumentDTO.InfoVO.RunStatus.RUNNING);
                    break;
                case 2:
                    runStatusList.add(DocumentDTO.InfoVO.RunStatus.CANCEL);
                    break;
                case 3:
                    runStatusList.add(DocumentDTO.InfoVO.RunStatus.DONE);
                    break;
                case 4:
                    runStatusList.add(DocumentDTO.InfoVO.RunStatus.FAIL);
                    break;
                default:
                    break;
            }
        }
        DocumentDTO.ListReq req = DocumentDTO.ListReq.builder()
                .run(runStatusList)
                .page(page)
                .pageSize(limit)
                .build();
        return getDocumentList(datasetId, req);
    }

    @Override
    public void deleteDocument(String datasetId, DocumentDTO.BatchIdReq req) {
        try {
            log.info("=== [RAGFlow] batchdeletedocument: datasetId={}, count={} ===", datasetId,
                    req.getIds() != null ? req.getIds().size() : 0);
            getClient().delete("/api/v1/datasets/" + datasetId + "/documents", req);
        } catch (Exception e) {
            log.error("batchdeletedocumentfailed: datasetId={}", datasetId, e);
            throw convertToRenException(e);
        }
    }

    @Override
    public boolean parseDocuments(String datasetId, List<String> documentIds) {
        try {
            log.info("=== [RAGFlow] parsedocument ===");
            Map<String, Object> body = new HashMap<>();
            body.put("document_ids", documentIds);

            getClient().post("/api/v1/datasets/" + datasetId + "/chunks", body);
            return true;
        } catch (Exception e) {
            log.error("parsedocumentfailed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public ChunkDTO.ListVO listChunks(String datasetId, String documentId, ChunkDTO.ListReq req) {
        try {
            // [提灯re-构] use objectMapper 动态convertqueryparameter，消除硬code
            Map<String, Object> params = objectMapper.convertValue(req, new TypeReference<Map<String, Object>>() {
            });

            Map<String, Object> response = getClient()
                    .get("/api/v1/datasets/" + datasetId + "/documents/" + documentId + "/chunks", params);

            Object dataObj = response.get("data");
            if (dataObj == null) {
                log.warn("[RAGFlow] listChunks response data asempty, docId={}", documentId);
                return ChunkDTO.ListVO.builder()
                        .chunks(new ArrayList<>())
                        .total(0L)
                        .build();
            }

            ChunkDTO.ListVO result = objectMapper.convertValue(dataObj, ChunkDTO.ListVO.class);
            if (result.getTotal() == null) {
                result.setTotal(0L);
            }
            return result;
        } catch (Exception e) {
            log.error("getslicefailed: docId={}", documentId, e);
            throw convertToRenException(e);
        }
    }

    @Override
    public RetrievalDTO.ResultVO retrievalTest(RetrievalDTO.TestReq req) {
        try {
            // [Production Reinforce] parameter防御for齐：RAGFlow Python endfor 0 or负numberpaginationsensitive
            // 解决 ValueError('Search does not support negative slicing.')
            if (req.getPage() != null && req.getPage() < 1) {
                req.setPage(1);
            }
            if (req.getPageSize() != null && req.getPageSize() < 1) {
                req.setPageSize(10); // default 10 items
            }
            if (req.getTopK() != null && req.getTopK() < 1) {
                req.setTopK(1024); // RAGFlow 内部default TopK
            }
            // similar度阈value归one化 (0.0 ~ 1.0)
            if (req.getSimilarityThreshold() != null) {
                if (req.getSimilarityThreshold() < 0f)
                    req.setSimilarityThreshold(0.2f);
                if (req.getSimilarityThreshold() > 1f)
                    req.setSimilarityThreshold(1.0f);
            }

            // [提灯re-构] directly透传strongtype DTO，by getClient processserialize
            Map<String, Object> response = getClient().post("/api/v1/retrieval", req);

            Object dataObj = response.get("data");
            if (dataObj == null) {
                log.warn("[RAGFlow] retrievalTest response data asempty");
                return RetrievalDTO.ResultVO.builder()
                        .chunks(new ArrayList<>())
                        .docAggs(new ArrayList<>())
                        .total(0L)
                        .build();
            }

            RetrievalDTO.ResultVO result = objectMapper.convertValue(dataObj, RetrievalDTO.ResultVO.class);
            if (result.getTotal() == null) {
                result.setTotal(0L);
            }
            return result;
        } catch (Exception e) {
            log.error("recalltestfailed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public boolean testConnection() {
        try {
            getClient().get("/api/v1/health", null);
            return true;
        } catch (Exception e) {
            log.error("connectiontestfailed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("adapterType", getAdapterType());
        status.put("configKeys", config != null ? config.keySet() : "notconfiguration");
        status.put("connectionTest", testConnection());
        status.put("lastChecked", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return status;
    }

    @Override
    public Map<String, Object> getSupportedConfig() {
        Map<String, Object> supportedConfig = new HashMap<>();
        supportedConfig.put("base_url", "RAGFlow APIbaseURL");
        supportedConfig.put("api_key", "RAGFlow APIkey");
        supportedConfig.put("timeout", "requesttimeouttime（milliseconds）");
        return supportedConfig;
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("timeout", 30000);
        return defaultConfig;
    }

    @Override
    public DatasetDTO.InfoVO createDataset(DatasetDTO.CreateReq req) {
        try {
            // [Production Fix] strong化defaultvalueprocess，prevent RAGFlow API 因emptystringor缺失field报错 (Code 101)
            // 解决 "Field: <avatar> - Message: <Missing MIME prefix>" etc.validatefailed
            if (StringUtils.isBlank(req.getPermission())) {
                req.setPermission("me");
            }
            if (StringUtils.isBlank(req.getChunkMethod())) {
                req.setChunkMethod("naive");
            }

            // 🤖 automatic补全embeddingmodel：priorityfirstuserequest传参，其timesuseconfiguration defaultmodel
            if (StringUtils.isBlank(req.getEmbeddingModel())) {
                String defaultModel = (String) getConfigValue(config, "embedding_model", "embeddingModel");
                if (StringUtils.isNotBlank(defaultModel)) {
                    log.info("RAGFlow: useconfiguration defaultembeddingmodel: {}", defaultModel);
                    req.setEmbeddingModel(defaultModel);
                }
                // 若configurationalsonodefaultvalue，then留emptyby RAGFlow serviceend自row兜底（or抛出businessexception）
            }

            // 🖼️ automatic补全avatar：若asemptythen提供one 1x1 透明像素，prevent RAGFlow validate MIME Prefix failed
            if (StringUtils.isBlank(req.getAvatar())) {
                req.setAvatar(
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
            }

            // directlywillstrongtyperequestobject传to Client，Jackson willprocess JsonProperty mapping
            Map<String, Object> response = getClient().post("/api/v1/datasets", req);

            // 安全get data andvia DatasetDTO.InfoVO perform全量mapping
            Object dataObj = response.get("data");
            if (dataObj != null) {
                return objectMapper.convertValue(dataObj, DatasetDTO.InfoVO.class);
            }
            throw new RenException(ErrorCode.RAG_API_ERROR, "Invalid response from createDataset: missing data object");
        } catch (Exception e) {
            log.error("createdatacollectionfailed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public DatasetDTO.InfoVO updateDataset(String datasetId, DatasetDTO.UpdateReq req) {
        try {
            // RAGFlow API updatesuggestionpathwith ID
            Map<String, Object> response = getClient().put("/api/v1/datasets/" + datasetId, req);

            Object dataObj = response.get("data");
            if (dataObj != null) {
                return objectMapper.convertValue(dataObj, DatasetDTO.InfoVO.class);
            }
            return null;
        } catch (Exception e) {
            log.error("updatedatacollectionfailed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public DatasetDTO.BatchOperationVO deleteDataset(DatasetDTO.BatchIdReq req) {
        try {
            // RAGFlow batchdeleteinterfaceuse DELETE /api/v1/datasets
            Map<String, Object> response = getClient().delete("/api/v1/datasets", req);

            Object dataObj = response.get("data");
            if (dataObj != null) {
                return objectMapper.convertValue(dataObj, DatasetDTO.BatchOperationVO.class);
            }
            return null;
        } catch (Exception e) {
            log.error("batchDelete datacollectionfailed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public Integer getDocumentCount(String datasetId) {
        try {
            // [Fix] uselistfilterinterfacegetdetails (GET /datasets?id={id})
            Map<String, Object> params = new HashMap<>();
            params.put("id", datasetId);
            params.put("page", 1);
            params.put("page_size", 1);

            Map<String, Object> response = getClient().get("/api/v1/datasets", params);
            Object dataObj = response.get("data");

            if (dataObj instanceof List) {
                List<?> list = (List<?>) dataObj;
                if (!list.isEmpty()) {
                    Object firstItem = list.get(0);
                    if (firstItem instanceof Map) {
                        Object countObj = ((Map<?, ?>) firstItem).get("document_count");
                        if (countObj instanceof Number) {
                            return ((Number) countObj).intValue();
                        }
                    }
                }
            }
            // 降级：not foundor结构does not match
            return 0;
        } catch (Exception e) {
            log.warn("getdocumentcountfailed: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public void postStream(String endpoint, Object body, Consumer<String> onData) {
        try {
            getClient().postStream(endpoint, body, onData);
        } catch (Exception e) {
            log.error("streamingrequestfailed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public Object postSearchBotAsk(Map<String, Object> config, Object body,
            Consumer<String> onData) {
        // SearchBot 际上Yes Dataset retrieve one种封装，orYesnot公开  API？
        // 假设 RAGFlow no显式  /searchbots interface供 SDK call，whileYes Dataset Retrieval or Chat。
        // butaccording to BotDTO，它Yes /api/v1/searchbots/ask (假设)
        // thisin  config 可canYes覆盖用 ，orIsonlyYes用 adapter examplealreadyhas  client。
        // but Bot 可canusenot 同  API Key？通常 Adapter examplebindone Key。
        // if Bot usesystem Key，thendirectly用 getClient()。

        // 暂when假设 endpoint /api/v1/searchbots/ask storein（orclass似 ）
        // ifYesstreaming:
        try {
            getClient().postStream("/api/v1/searchbots/ask", body, onData);
            return null;
        } catch (Exception e) {
            log.error("SearchBot Ask failed", e);
            throw convertToRenException(e);
        }
    }

    @Override
    public void postAgentBotCompletion(Map<String, Object> config, String agentId, Object body,
            Consumer<String> onData) {
        // AgentBot corresponding /api/v1/agentbots/{id}/completions
        try {
            getClient().postStream("/api/v1/agentbots/" + agentId + "/completions", body, onData);
        } catch (Exception e) {
            log.error("AgentBot Completion failed", e);
            throw convertToRenException(e);
        }
    }

    // 复用原has 辅助parsemethod，保持compatible
    // [Bug Fix] not again吞掉deserializeexception，避免上layer误判"documentalreadydelete"
    private PageData<KnowledgeFilesDTO> parseDocumentListResponse(Object dataObj, long curPage, long pageSize) {
        if (dataObj == null) {
            return new PageData<>(new ArrayList<>(), 0);
        }

        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
        List<Map<String, Object>> documents = (List<Map<String, Object>>) dataMap.get("docs");
        if (documents == null || documents.isEmpty()) {
            // RAGFlow 明确returnemptydocumentlist，thisYes合法 "真empty"
            return new PageData<>(new ArrayList<>(), 0);
        }

        List<KnowledgeFilesDTO> list = new ArrayList<>();
        for (Object docObj : documents) {
            try {
                // documentconvert容错：onedocumentdeserializefailednot 影响otherdocument
                DocumentDTO.InfoVO info = objectMapper.convertValue(docObj, DocumentDTO.InfoVO.class);
                list.add(mapToKnowledgeFilesDTO(info, null));
            } catch (Exception e) {
                log.warn("[RAGFlow] document DTO convertfailed，skipthisdocument: {}", e.getMessage());
            }
        }

        long total = 0;
        if (dataMap.containsKey("total")) {
            total = ((Number) dataMap.get("total")).longValue();
        }

        return new PageData<>(list, total);
    }

    private KnowledgeFilesDTO parseUploadResponse(Object dataObj, String datasetId, MultipartFile file) {
        KnowledgeFilesDTO result = null;

        // 尝试fromresponsedataextractdocumentID (documentId)
        if (dataObj != null) {
            try {
                DocumentDTO.InfoVO info = null;
                if (dataObj instanceof Map) {
                    info = objectMapper.convertValue(dataObj, DocumentDTO.InfoVO.class);
                } else if (dataObj instanceof List) {
                    List<?> list = (List<?>) dataObj;
                    if (!list.isEmpty()) {
                        info = objectMapper.convertValue(list.get(0), DocumentDTO.InfoVO.class);
                    }
                }

                if (info != null) {
                    result = mapToKnowledgeFilesDTO(info, datasetId);
                }
            } catch (Exception e) {
                log.warn("parseuploadresponsedatafailed: {}", e.getMessage());
            }
        }

        if (result == null) {
            log.error("notcanfromRAGFlowresponseextracttodocumentId，responsecontent: {}", dataObj);
            // thisin应thisreturnoneminimum化 containbaseinformation  DTO whilenot Yes null，prevent上游 NPE
            result = new KnowledgeFilesDTO();
            result.setDatasetId(datasetId);
            result.setName(file.getOriginalFilename());
            result.setFileSize(file.getSize());
            result.setStatus("1");
        }

        return result;
    }

    /**
     * will RAGFlow  strongtype InfoVO mappingto内部use  KnowledgeFilesDTO
     * ensureallavailablefield（name、largesmall、status、configurationetc.）all得to全量synchronous
     */
    private KnowledgeFilesDTO mapToKnowledgeFilesDTO(DocumentDTO.InfoVO info, String datasetId) {
        KnowledgeFilesDTO dto = new KnowledgeFilesDTO();
        if (info == null)
            return dto;

        dto.setId(info.getId());
        dto.setDocumentId(info.getId());
        dto.setDatasetId(StringUtils.isNotBlank(info.getDatasetId()) ? info.getDatasetId() : datasetId);
        dto.setName(info.getName());
        dto.setFileSize(info.getSize());

        // statusmapping
        if (info.getRun() != null) {
            dto.setRun(info.getRun().name());
        }

        if (StringUtils.isNotBlank(info.getStatus())) {
            dto.setStatus(info.getStatus());
        } else {
            dto.setStatus("1"); // defaultenable
        }

        // timesynchronous
        if (info.getCreateTime() != null) {
            dto.setCreatedAt(new Date(info.getCreateTime()));
        }
        if (info.getUpdateTime() != null) {
            dto.setUpdatedAt(new Date(info.getUpdateTime()));
        }

        // 核心data补齐 (Issue 1)
        dto.setProgress(info.getProgress());
        dto.setThumbnail(info.getThumbnail());
        dto.setProcessDuration(info.getProcessDuration());
        dto.setSourceType(info.getSourceType());
        dto.setChunkCount(info.getChunkCount() != null ? info.getChunkCount().intValue() : 0);
        dto.setTokenCount(info.getTokenCount());
        dto.setError(info.getProgressMsg()); // willprogressDescriptionmappingaserrorinformationhint

        // 扩展fieldsynchronous
        dto.setMetaFields(info.getMetaFields());
        if (info.getChunkMethod() != null) {
            dto.setChunkMethod(info.getChunkMethod().name().toLowerCase());
        }
        if (info.getParserConfig() != null) {
            dto.setParserConfig(objectMapper.convertValue(info.getParserConfig(), Map.class));
        }

        return dto;
    }

    private static class MultipartFileResource extends AbstractResource {
        private final MultipartFile multipartFile;

        public MultipartFileResource(MultipartFile multipartFile) {
            this.multipartFile = multipartFile;
        }

        @Override
        public String getDescription() {
            return "MultipartFile resource [" + multipartFile.getOriginalFilename() + "]";
        }

        @Override
        public String getFilename() {
            return multipartFile.getOriginalFilename();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return multipartFile.getInputStream();
        }

        @Override
        public long contentLength() throws IOException {
            return multipartFile.getSize();
        }

        @Override
        public boolean exists() {
            return !multipartFile.isEmpty();
        }
    }
}