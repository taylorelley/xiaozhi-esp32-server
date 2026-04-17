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

        @Schema(description = "Knowledge base ID (mustspecified)", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("dataset_id")
        @NotBlank(message = "Knowledge baseIDcannot be empty")
        private String datasetId;

        @Schema(description = "File name (ifspecified，thenoverrideoriginalFile name)")
        private String name;

        @Schema(description = "chunkmethod")
        @JsonProperty("chunk_method")
        private DocumentDTO.InfoVO.ChunkMethod chunkMethod;

        @Schema(description = "parseParameter configuration")
        @JsonProperty("parser_config")
        private DocumentDTO.InfoVO.ParserConfig parserConfig;

        @Schema(description = "virtualfilepath (defaultas /)")
        @JsonProperty("parent_path")
        private String parentPath;

        @Schema(description = "datafield")
        @JsonProperty("meta")
        private Map<String, Object> metaFields;

        @Schema(description = "filebinary stream (support PDF, DOCX, TXT, MD etc.multiplekindformat)", requiredMode = Schema.RequiredMode.REQUIRED)
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

        @Schema(description = "newdocumentname (mustcontainfileafterfix，andnot canchangeoriginaltype)")
        private String name;

        @Schema(description = "enable/disablestatus (true: enable, false: disable; disableafternot parameterandretrieve)")
        private Boolean enabled;

        @Schema(description = "newparsemethod (updatethisitemwillre-setparsestatus)")
        @JsonProperty("chunk_method")
        private InfoVO.ChunkMethod chunkMethod;

        @Schema(description = "newparserdetailedconfiguration (shouldand chunk_method setuse)")
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

        @Schema(description = "Sort orderfield (canselect: create_time, name, size; default: create_time)")
        private String orderby;

        @Schema(description = "YesNodescendingsortcolumn (true: mostnew/mostlargeinbefore; false: mostold/minimuminbefore; default: true)")
        private Boolean desc;

        @Schema(description = "precisionexactfilter: document ID")
        private String id;

        @Schema(description = "precisionexactfilter: documentcompletename (containafterfix)")
        private String name;

        @Schema(description = "fuzzysearch: documentnamekeyword")
        private String keywords;

        @Schema(description = "filter: fileafterfixlist (e.g. ['pdf', 'docx'])")
        private List<String> suffix;

        @Schema(description = "filter: runstatuslist")
        private List<InfoVO.RunStatus> run;

        @Schema(description = "filter: startCreate time (timestamp, milliseconds)")
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
        @JsonProperty("ids") // ascompatible，alsocantoconsidersupport document_ids，butthisinonecall ids
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

        @Schema(description = "documentthumbnail URL (Base64 or link)")
        private String thumbnail;

        @Schema(description = "belonging toKnowledge base ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("dataset_id")
        private String datasetId;

        @Schema(description = "Document parsingmethod (decidedocumente.g.whatisslice)")
        @JsonProperty("chunk_method")
        private ChunkMethod chunkMethod;

        @Schema(description = "associated  ETL Pipeline ID (e.g.has)")
        @JsonProperty("pipeline_id")
        private String pipelineId;

        @Schema(description = "Document parsing detailedconfiguration")
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

        @Schema(description = "filestorestorepathorbitsetidentifier")
        private String location;

        @Schema(description = "File size (unit: Bytes)")
        private Long size;

        @Schema(description = "contain  Token total (parseafterstatistics)")
        @JsonProperty("token_count")
        private Long tokenCount;

        @Schema(description = "contain slice (Chunk) total")
        @JsonProperty("chunk_count")
        private Long chunkCount;

        @Schema(description = "parseprogress (0.0 ~ 1.0, 1.0 representscomplete)")
        private Double progress;

        @Schema(description = "currentprogressDescriptionorerrorinformation")
        @JsonProperty("progress_msg")
        private String progressMsg;

        @Schema(description = "startprocess timestamp (RAGFlowreturnRFC1123format)")
        @JsonProperty("process_begin_at")
        private String processBeginAt;

        @Schema(description = "processtotalconsumptionwhen (unit: seconds)")
        @JsonProperty("process_duration")
        private Double processDuration;

        @Schema(description = "custom metadatafield (Key-Value keyvaluefor)")
        @JsonProperty("meta_fields")
        private Map<String, Object> metaFields;

        @Schema(description = "fileafterfixname (not containpoint)")
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
         * parsemethodenumeration (ChunkMethod)
         */
        public enum ChunkMethod {
            @Schema(description = "usemode: suitableused forlargemultiplenumberpuretextormixmergedocument")
            @JsonProperty("naive")
            NAIVE,
            @Schema(description = "manualmode: allowusermanualeditslice")
            @JsonProperty("manual")
            MANUAL,
            @Schema(description = "Q&Amode: specialdoorpriority Q&A format document")
            @JsonProperty("qa")
            QA,
            @Schema(description = "tablemode: specialdoorpriority Excel or CSV etc.tabledata")
            @JsonProperty("table")
            TABLE,
            @Schema(description = "papermode: foracademic papersortversionpriority")
            @JsonProperty("paper")
            PAPER,
            @Schema(description = "bookmode: forbook chapterconstructpriority")
            @JsonProperty("book")
            BOOK,
            @Schema(description = "rulerulemode: forruleitemstextresultconstructpriority")
            @JsonProperty("laws")
            LAWS,
            @Schema(description = "presentationmode: for PPT etc.demofilepriority")
            @JsonProperty("presentation")
            PRESENTATION,
            @Schema(description = "imagemode: forimagecontentperform OCR andDescription")
            @JsonProperty("picture")
            PICTURE,
            @Schema(description = "wholemode: willwholedocumentasoneslice")
            @JsonProperty("one")
            ONE,
            @Schema(description = "knowledge graphmode: extractentityrelatedsystembuildgraph")
            @JsonProperty("knowledge_graph")
            KNOWLEDGE_GRAPH,
            @Schema(description = "mailitemmode: formailitemformatpriority")
            @JsonProperty("email")
            EMAIL;
        }

        /**
         * runstatusenumeration (RunStatus)
         */
        public enum RunStatus {
            @Schema(description = "notstart: waitparsequeuecolumn")
            @JsonProperty("UNSTART")
            UNSTART,
            @Schema(description = "perform: inparseorindex")
            @JsonProperty("RUNNING")
            RUNNING,
            @Schema(description = "alreadycancel: usermanualcancel")
            @JsonProperty("CANCEL")
            CANCEL,
            @Schema(description = "alreadycomplete: parsesuccess")
            @JsonProperty("DONE")
            DONE,
            @Schema(description = "failed: parseprocessoutwrong")
            @JsonProperty("FAIL")
            FAIL;
        }

        /**
         * layoutidentifymodelenumeration
         */
        public enum LayoutRecognize {
            @Schema(description = "deepdocumentsolvemodel: suitablemergere-miscellaneoussortversion")
            @JsonProperty("DeepDOC")
            DeepDOC,
            @Schema(description = "simplerulethenmodel: suitablemergepuretext")
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

            @Schema(description = "slicemostlarge Token number (suggestionvalue: 512, 1024, 2048)")
            @JsonProperty("chunk_token_num")
            private Integer chunkTokenNum;

            @Schema(description = "segmentdelimitersymbol (supportconvertmeaningcharactersymbol, e.g. \\n)")
            private String delimiter;

            @Schema(description = "layoutidentifymodel (DeepDOC/Simple)")
            @JsonProperty("layout_recognize")
            private LayoutRecognize layoutRecognize;

            @Schema(description = "YesNowill Excel convert to HTML table")
            @JsonProperty("html4excel")
            private Boolean html4excel;

            @Schema(description = "automaticextractkeywordcount (0 representsnot extract)")
            @JsonProperty("auto_keywords")
            private Integer autoKeywords;

            @Schema(description = "automaticgeneratequestioncount (0 representsnot generate)")
            @JsonProperty("auto_questions")
            private Integer autoQuestions;

            @Schema(description = "automaticgenerateTagcount")
            @JsonProperty("topn_tags")
            private Integer topnTags;

            @Schema(description = "RAPTOR highlevelindexconfiguration")
            private RaptorConfig raptor;

            @Schema(description = "GraphRAG knowledge graphconfiguration")
            @JsonProperty("graphrag")
            private GraphRagConfig graphRag;

            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            @Schema(description = "RAPTOR (recursiveabstractneed toindex) configuration")
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
            @Schema(description = "GraphRAG (pictureincreasestrongretrieve) configuration")
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
