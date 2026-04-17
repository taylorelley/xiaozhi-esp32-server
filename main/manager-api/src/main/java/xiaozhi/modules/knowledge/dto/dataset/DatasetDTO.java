package xiaozhi.modules.knowledge.dto.dataset;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

/**
 * Knowledge basemanagementaggregation DTO
 * <p>
 * 容class，内含Knowledge basemoduleallrequest/responseobject 静态内部classdefine。
 * </p>
 */
@Schema(description = "Knowledge basemanagementaggregation DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetDTO {

    // ========== 通用内部class ==========

    /**
     * parserconfiguration
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "parserconfiguration")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParserConfig implements Serializable {

        @Schema(description = "chunk token count", example = "128")
        @JsonProperty("chunk_token_num")
        private Integer chunkTokenNum;

        @Schema(description = "分隔符", example = "\\n!?;。；！？")
        private String delimiter;

        @Schema(description = "布局识别model: DeepDOC / Simple", example = "DeepDOC")
        @JsonProperty("layout_recognize")
        private String layoutRecognize;

        @Schema(description = "YesNowill Excel 转as HTML", example = "false")
        private Boolean html4excel;

        @Schema(description = "automaticgeneratekeywordcount (0 representsclose)", example = "0")
        @JsonProperty("auto_keywords")
        private Integer autoKeywords;

        @Schema(description = "automaticgeneratequestioncount (0 representsclose)", example = "0")
        @JsonProperty("auto_questions")
        private Integer autoQuestions;
    }

    // ========== requestclass ==========

    /**
     * createKnowledge baserequest (mappinginterface 1: create)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "createKnowledge baserequest")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateReq implements Serializable {

        @NotBlank(message = "Knowledge base namecannot be empty")
        @Schema(description = "Knowledge base name", requiredMode = Schema.RequiredMode.REQUIRED, example = "my_dataset")
        private String name;

        @Schema(description = "Knowledge baseavatar (Base64 code)", example = "")
        private String avatar;

        @Schema(description = "Knowledge baseDescription", example = "used forstore储产品document")
        private String description;

        @Schema(description = "embeddingModel name", example = "BAAI/bge-large-zh-v1.5")
        @JsonProperty("embedding_model")
        private String embeddingModel;

        @Schema(description = "Permissionset: me / team", example = "me")
        private String permission;

        @Schema(description = "chunkmethod: naive / manual / qa / table / paper / book / laws / presentation / picture / one / knowledge_graph / email", example = "naive")
        @JsonProperty("chunk_method")
        private String chunkMethod;

        @Schema(description = "parserconfiguration")
        @JsonProperty("parser_config")
        private ParserConfig parserConfig;
    }

    /**
     * updateKnowledge baserequest (mappinginterface 4: update)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "updateKnowledge baserequest")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateReq implements Serializable {

        @Schema(description = "Knowledge base name", example = "updated_dataset")
        private String name;

        @Schema(description = "Knowledge baseavatar (Base64 code)", example = "")
        private String avatar;

        @Schema(description = "Knowledge baseDescription", example = "updateafter Description")
        private String description;

        @Schema(description = "Permissionset: me / team", example = "team")
        private String permission;

        @Schema(description = "embeddingModel name", example = "BAAI/bge-large-zh-v1.5")
        @JsonProperty("embedding_model")
        private String embeddingModel;

        @Schema(description = "chunkmethod: naive / manual / qa / table / paper / book / laws / presentation / picture / one / knowledge_graph / email", example = "naive")
        @JsonProperty("chunk_method")
        private String chunkMethod;

        @Schema(description = "parserconfiguration")
        @JsonProperty("parser_config")
        private ParserConfig parserConfig;

        @Schema(description = "PageRank 权re- (0-100)", example = "50")
        private Integer pagerank;
    }

    /**
     * queryKnowledge baselistrequest (mappinginterface 3: list_datasets)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "queryKnowledge baselistrequest")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListReq implements Serializable {

        @Schema(description = "page number (from 1 start)", example = "1")
        private Integer page;

        @Schema(description = "per pagecount", example = "30")
        @JsonProperty("page_size")
        private Integer pageSize;

        @Schema(description = "Sort orderfield: create_time / update_time", example = "create_time")
        private String orderby;

        @Schema(description = "YesNodescending", example = "true")
        private Boolean desc;

        @Schema(description = "bynamefilter (模糊匹配)", example = "my_dataset")
        private String name;

        @Schema(description = "byKnowledge base ID filter", example = "abc123")
        private String id;
    }

    /**
     * batchdeleteKnowledge baserequest (mappinginterface 2: delete)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "batchdeleteKnowledge baserequest")
    public static class BatchIdReq implements Serializable {

        @NotNull(message = "Knowledge base ID listcannot be empty")
        @Size(min = 1, message = "至少needoneKnowledge base ID")
        @Schema(description = "Knowledge base ID list", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"id1\", \"id2\"]")
        private List<String> ids;
    }

    /**
     * run GraphRAG request
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "run GraphRAG request")
    public static class RunGraphRagReq implements Serializable {

        @Schema(description = "entitytypelist", example = "[\"person\", \"organization\"]")
        @JsonProperty("entity_types")
        private List<String> entityTypes;

        @Schema(description = "buildmethod: light / fast / full", example = "light")
        private String method;
    }

    /**
     * run RAPTOR request
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "run RAPTOR request")
    public static class RunRaptorReq implements Serializable {

        @Schema(description = "mostlarge聚classnumber", example = "64")
        @JsonProperty("max_cluster")
        private Integer maxCluster;

        @Schema(description = "自defineprompt", example = "请summaryto下content...")
        private String prompt;
    }

    /**
     * asynchronoustask ID response VO (mappinginterface 7/8: run_graphrag/run_raptor)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "asynchronoustask ID response")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskIdVO implements Serializable {

        @Schema(description = "GraphRAG task ID", example = "task_uuid_12345678")
        @JsonProperty("graphrag_task_id")
        private String graphragTaskId;

        @Schema(description = "RAPTOR task ID", example = "task_uuid_87654321")
        @JsonProperty("raptor_task_id")
        private String raptorTaskId;
    }

    // ========== responseclass ==========

    /**
     * Knowledge basedetails VO (mappinginterface 1/3  returndataitem)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Knowledge basedetails VO")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoVO implements Serializable {

        @Schema(description = "Knowledge base ID", example = "abc123")
        private String id;

        @Schema(description = "Knowledge base name", example = "my_dataset")
        private String name;

        @Schema(description = "Knowledge baseavatar (Base64 code)", example = "")
        private String avatar;

        @Schema(description = "tenant ID", example = "tenant_001")
        @JsonProperty("tenant_id")
        private String tenantId;

        @Schema(description = "Knowledge baseDescription", example = "used forstore储产品document")
        private String description;

        @Schema(description = "embeddingModel name", example = "BAAI/bge-large-zh-v1.5")
        @JsonProperty("embedding_model")
        private String embeddingModel;

        @Schema(description = "Permissionset: me / team", example = "me")
        private String permission;

        @Schema(description = "chunkmethod", example = "naive")
        @JsonProperty("chunk_method")
        private String chunkMethod;

        @Schema(description = "parserconfiguration")
        @JsonProperty("parser_config")
        private ParserConfig parserConfig;

        @Schema(description = "chunktotal", example = "1024")
        @JsonProperty("chunk_count")
        private Long chunkCount;

        @Schema(description = "documenttotal", example = "50")
        @JsonProperty("document_count")
        private Long documentCount;

        @Schema(description = "Create time (timestamp)", example = "1700000000000")
        @JsonProperty("create_time")
        private Long createTime;

        @Schema(description = "updatetime (timestamp)", example = "1700000001000")
        @JsonProperty("update_time")
        private Long updateTime;

        @Schema(description = "total Token number", example = "102400")
        @JsonProperty("token_num")
        private Long tokenNum;

        @Schema(description = "createdate (format: yyyy-MM-dd HH:mm:ss)")
        @JsonProperty("create_date")
        private String createDate;

        @Schema(description = "lastupdatedate (format: yyyy-MM-dd HH:mm:ss)")
        @JsonProperty("update_date")
        private String updateDate;
    }

    /**
     * batchoperationresponse VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "batchoperationresponse VO")
    public static class BatchOperationVO implements Serializable {

        @Schema(description = "successoperationcount", example = "5")
        @JsonProperty("success_count")
        private Integer successCount;

        @Schema(description = "errorlist")
        private List<Object> errors;
    }

    // ========== 知识图谱related ==========

    /**
     * 知识图谱data VO (mappinginterface 5: knowledge_graph)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "知识图谱data VO")
    public static class GraphVO implements Serializable {

        @Schema(description = "图谱节点list")
        private List<Node> nodes;

        @Schema(description = "图谱边list")
        private List<Edge> edges;

        @Schema(description = "思维导图data")
        @JsonProperty("mind_map")
        private Map<String, Object> mindMap;

        /**
         * 图谱节点
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "图谱节点")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Node implements Serializable {

            @Schema(description = "节点 ID", example = "node_001")
            private String id;

            @Schema(description = "节点Tag", example = "产品")
            private String label;

            @Schema(description = "PageRank value", example = "0.85")
            private Double pagerank;

            @Schema(description = "节点颜色", example = "#FF5733")
            private String color;

            @Schema(description = "节点图片 URL", example = "https://example.com/icon.png")
            private String img;
        }

        /**
         * 图谱边
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "图谱边")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Edge implements Serializable {

            @Schema(description = "source节点 ID", example = "node_001")
            private String source;

            @Schema(description = "target节点 ID", example = "node_002")
            private String target;

            @Schema(description = "边权re-", example = "0.75")
            private Double weight;

            @Schema(description = "边Tag (关系Description)", example = "belongs to")
            private String label;
        }
    }

    // ========== asynchronoustasktrace (GraphRAG/RAPTOR) ==========

    /**
     * asynchronoustasktrace VO (mappinginterface 9/10: taskprogressreturn)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "asynchronoustasktrace VO")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskTraceVO implements Serializable {

        @Schema(description = "task ID", example = "task_001")
        private String id;

        @Schema(description = "document ID", example = "doc_001")
        @JsonProperty("doc_id")
        private String docId;

        @Schema(description = "起始page number", example = "1")
        @JsonProperty("from_page")
        private Integer fromPage;

        @Schema(description = "endpage number", example = "10")
        @JsonProperty("to_page")
        private Integer toPage;

        @Schema(description = "progress百分比 (0.0 - 1.0)", example = "0.75")
        private Double progress;

        @Schema(description = "progressmessage", example = "inprocess第 5 页...")
        @JsonProperty("progress_msg")
        private String progressMsg;

        @Schema(description = "Create time (timestamp)", example = "1700000000000")
        @JsonProperty("create_time")
        private Long createTime;

        @Schema(description = "updatetime (timestamp)", example = "1700000001000")
        @JsonProperty("update_time")
        private Long updateTime;
    }
}
