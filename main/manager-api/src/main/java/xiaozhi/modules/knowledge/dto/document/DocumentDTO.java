package xiaozhi.modules.knowledge.dto.document;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

/**
 * documentmanagementaggregation DTO
 */
@Schema(description = "documentmanagementaggregation DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentDTO {

    /**
     * uploaddocumentrequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "uploaddocumentrequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UploadReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Knowledge base ID (必须specified归属)", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("dataset_id")
        @NotBlank(message = "Knowledge baseIDcannot be empty")
        private String datasetId;

        @Schema(description = "File name (ifspecified，then覆盖原始File name)")
        private String name;

        @Schema(description = "chunk方法")
        @JsonProperty("chunk_method")
        private DocumentDTO.InfoVO.ChunkMethod chunkMethod;

        @Schema(description = "parseParameter configuration")
        @JsonProperty("parser_config")
        private DocumentDTO.InfoVO.ParserConfig parserConfig;

        @Schema(description = "虚拟filepath (defaultas /)")
        @JsonProperty("parent_path")
        private String parentPath;

        @Schema(description = "datafield")
        @JsonProperty("meta")
        private Map<String, Object> metaFields;

        @Schema(description = "file二进制流 (support PDF, DOCX, TXT, MD etc.多种format)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "uploadfilecannot be empty")
        private org.springframework.web.multipart.MultipartFile file;
    }

    /**
     * updatedocumentrequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "updatedocumentrequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "newdocumentname (必须containfile后缀，andnot 能更改原始type)")
        private String name;

        @Schema(description = "enable/disablestatus (true: enable, false: disable; disable后not 参andretrieve)")
        private Boolean enabled;

        @Schema(description = "newparse方法 (updatethisitem会重置parsestatus)")
        @JsonProperty("chunk_method")
        private InfoVO.ChunkMethod chunkMethod;

        @Schema(description = "newparser详细configuration (应and chunk_method 配套use)")
        @JsonProperty("parser_config")
        private InfoVO.ParserConfig parserConfig;
    }

    /**
     * getdocumentlistrequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "getdocumentlistrequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "page number (default: 1)")
        private Integer page;

        @Schema(description = "per pagecount (default: 30)")
        @JsonProperty("page_size")
        private Integer pageSize;

        @Schema(description = "Sort orderfield (可选: create_time, name, size; default: create_time)")
        private String orderby;

        @Schema(description = "YesNodescending排列 (true: 最new/最largein前; false: 最旧/minimumin前; default: true)")
        private Boolean desc;

        @Schema(description = "精确filter: document ID")
        private String id;

        @Schema(description = "精确filter: documentcompletename (含后缀)")
        private String name;

        @Schema(description = "模糊search: documentnamekeyword")
        private String keywords;

        @Schema(description = "filter: file后缀list (e.g. ['pdf', 'docx'])")
        private List<String> suffix;

        @Schema(description = "filter: runstatuslist")
        private List<InfoVO.RunStatus> run;

        @Schema(description = "filter: 起始Create time (timestamp, milliseconds)")
        @JsonProperty("create_time_from")
        private Long createTimeFrom;

        @Schema(description = "filter: endCreate time (timestamp, milliseconds)")
        @JsonProperty("create_time_to")
        private Long createTimeTo;
    }

    /**
     * batchdocumentoperationrequestparameter (used fordelete、parseetc.)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "batchdocumentoperationrequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchIdReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "document ID list", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("ids") // as了兼容，也可以考虑support document_ids，但this里统一叫 ids
        @JsonAlias("document_ids")
        @NotEmpty(message = "documentIDlistcannot be empty")
        private List<String> ids;
    }

    /**
     * Knowledge basedocumentinformation VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Knowledge basedocumentinformation")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "document ID (unique identifier)", requiredMode = Schema.RequiredMode.REQUIRED)
        private String id;

        @Schema(description = "document缩略图 URL (Base64 or link)")
        private String thumbnail;

        @Schema(description = "belonging toKnowledge base ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("dataset_id")
        private String datasetId;

        @Schema(description = "Document parsing方法 (决定了documente.g.何isslice)")
        @JsonProperty("chunk_method")
        private ChunkMethod chunkMethod;

        @Schema(description = "associated  ETL Pipeline ID (e.g.有)")
        @JsonProperty("pipeline_id")
        private String pipelineId;

        @Schema(description = "Document parsing 详细configuration")
        @JsonProperty("parser_config")
        private ParserConfig parserConfig;

        @Schema(description = "sourcetype (e.g. local, s3, url etc.)")
        @JsonProperty("source_type")
        private String sourceType;

        @Schema(description = "documentFile type (e.g. pdf, docx, txt)", requiredMode = Schema.RequiredMode.REQUIRED)
        private String type;

        @Schema(description = "Creatoruser ID")
        @JsonProperty("created_by")
        private String createdBy;

        @Schema(description = "documentname (containextension)", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "file存储pathorbit置identifier")
        private String location;

        @Schema(description = "File size (unit: Bytes)")
        private Long size;

        @Schema(description = "contain  Token total (parse后statistics)")
        @JsonProperty("token_count")
        private Long tokenCount;

        @Schema(description = "contain slice (Chunk) total")
        @JsonProperty("chunk_count")
        private Long chunkCount;

        @Schema(description = "parse进度 (0.0 ~ 1.0, 1.0 representscomplete)")
        private Double progress;

        @Schema(description = "current进度Descriptionorerrorinformation")
        @JsonProperty("progress_msg")
        private String progressMsg;

        @Schema(description = "startprocess timestamp (RAGFlowreturnRFC1123format)")
        @JsonProperty("process_begin_at")
        private String processBeginAt;

        @Schema(description = "processtotal耗时 (unit: seconds)")
        @JsonProperty("process_duration")
        private Double processDuration;

        @Schema(description = "custom metadatafield (Key-Value 键valuefor)")
        @JsonProperty("meta_fields")
        private Map<String, Object> metaFields;

        @Schema(description = "file后缀名 (not 含点)")
        private String suffix;

        @Schema(description = "Document parsingrunstatus")
        private RunStatus run;

        @Schema(description = "documentavailablestatus (1: enable/normal, 0: disable/invalid)", requiredMode = Schema.RequiredMode.REQUIRED)
        private String status;

        @Schema(description = "Create time (timestamp, milliseconds)", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("create_time")
        private Long createTime;

        @Schema(description = "createdate (RAGFlowreturnRFC1123format)")
        @JsonProperty("create_date")
        private String createDate;

        @Schema(description = "lastupdatetime (timestamp, milliseconds)")
        @JsonProperty("update_time")
        private Long updateTime;

        @Schema(description = "lastupdatedate (RAGFlowreturnRFC1123format)")
        @JsonProperty("update_date")
        private String updateDate;

        /**
         * parse方法enumeration (ChunkMethod)
         */
        public enum ChunkMethod {
            @Schema(description = "通用mode: 适used forlarge多number纯textor混合document")
            @JsonProperty("naive")
            NAIVE,
            @Schema(description = "手动mode: allowuser手动编辑slice")
            @JsonProperty("manual")
            MANUAL,
            @Schema(description = "问答mode: 专门优化 Q&A format document")
            @JsonProperty("qa")
            QA,
            @Schema(description = "table格mode: 专门优化 Excel or CSV etc.table格data")
            @JsonProperty("table")
            TABLE,
            @Schema(description = "论文mode: 针for学术论文排版优化")
            @JsonProperty("paper")
            PAPER,
            @Schema(description = "书籍mode: 针for书籍章节结构优化")
            @JsonProperty("book")
            BOOK,
            @Schema(description = "法律法规mode: 针for法律items文结构优化")
            @JsonProperty("laws")
            LAWS,
            @Schema(description = "演示文稿mode: 针for PPT etc.演示file优化")
            @JsonProperty("presentation")
            PRESENTATION,
            @Schema(description = "图片mode: 针for图片contentperform OCR andDescription")
            @JsonProperty("picture")
            PICTURE,
            @Schema(description = "整mode: will整个documentas一个slice")
            @JsonProperty("one")
            ONE,
            @Schema(description = "知识图谱mode: extractentity关系build图谱")
            @JsonProperty("knowledge_graph")
            KNOWLEDGE_GRAPH,
            @Schema(description = "邮件mode: 针for邮件format优化")
            @JsonProperty("email")
            EMAIL;
        }

        /**
         * runstatusenumeration (RunStatus)
         */
        public enum RunStatus {
            @Schema(description = "notstart: waitparse队列")
            @JsonProperty("UNSTART")
            UNSTART,
            @Schema(description = "perform: 正inparseorindex")
            @JsonProperty("RUNNING")
            RUNNING,
            @Schema(description = "already取消: user手动取消")
            @JsonProperty("CANCEL")
            CANCEL,
            @Schema(description = "alreadycomplete: parsesuccess")
            @JsonProperty("DONE")
            DONE,
            @Schema(description = "failed: parse程出错")
            @JsonProperty("FAIL")
            FAIL;
        }

        /**
         * 布局识别modelenumeration
         */
        public enum LayoutRecognize {
            @Schema(description = "深度document理解model: 适合复杂排版")
            @JsonProperty("DeepDOC")
            DeepDOC,
            @Schema(description = "简单规thenmodel: 适合纯text")
            @JsonProperty("Simple")
            Simple;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "Document parsingParameter configuration")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ParserConfig implements Serializable {
            private static final long serialVersionUID = 1L;

            @Schema(description = "slice最large Token number (建议value: 512, 1024, 2048)")
            @JsonProperty("chunk_token_num")
            private Integer chunkTokenNum;

            @Schema(description = "分段分隔符 (support转义字符, e.g. \\n)")
            private String delimiter;

            @Schema(description = "布局识别model (DeepDOC/Simple)")
            @JsonProperty("layout_recognize")
            private LayoutRecognize layoutRecognize;

            @Schema(description = "YesNowill Excel convert to HTML table格")
            @JsonProperty("html4excel")
            private Boolean html4excel;

            @Schema(description = "自动extractkeywordcount (0 representsnot extract)")
            @JsonProperty("auto_keywords")
            private Integer autoKeywords;

            @Schema(description = "自动generatequestioncount (0 representsnot generate)")
            @JsonProperty("auto_questions")
            private Integer autoQuestions;

            @Schema(description = "自动generateTagcount")
            @JsonProperty("topn_tags")
            private Integer topnTags;

            @Schema(description = "RAPTOR 高级indexconfiguration")
            private RaptorConfig raptor;

            @Schema(description = "GraphRAG 知识图谱configuration")
            @JsonProperty("graphrag")
            private GraphRagConfig graphRag;

            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            @Schema(description = "RAPTOR (递归摘need toindex) configuration")
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class RaptorConfig implements Serializable {
                private static final long serialVersionUID = 1L;
                @Schema(description = "YesNoenable RAPTOR index")
                @JsonProperty("use_raptor")
                private Boolean useRaptor;
            }

            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            @Schema(description = "GraphRAG (图增强retrieve) configuration")
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class GraphRagConfig implements Serializable {
                private static final long serialVersionUID = 1L;
                @Schema(description = "YesNoenable GraphRAG index")
                @JsonProperty("use_graphrag")
                private Boolean useGraphRag;
            }
        }
    }
}
