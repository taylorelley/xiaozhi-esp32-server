package xiaozhi.modules.knowledge.dto.document;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

/**
 * retrieveanddatamanagementaggregation DTO
 */
@Schema(description = "retrieveanddatamanagementaggregation DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrievalDTO {

    /**
     * Document aggregationinformation (VO)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Document aggregationinformation")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocAggVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "documentname")
        @JsonProperty("doc_name")
        private String docName;

        @Schema(description = "document ID")
        @JsonProperty("doc_id")
        private String docId;

        @Schema(description = "count")
        private Integer count;
    }

    /**
     * retrievetestrequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "retrievetestrequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Knowledge base ID list", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("dataset_ids")
        @NotEmpty(message = "Knowledge baseIDlistcannot be empty")
        private List<String> datasetIds;

        @Schema(description = "document ID list (可选，used for限定retrieve范围)")
        @JsonProperty("document_ids")
        private List<String> documentIds;

        @Schema(description = "retrievequestion", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "retrievequestioncannot be empty")
        private String question;

        @Schema(description = "page number (default 1)")
        private Integer page;

        @Schema(description = "per pagecount (default 10)")
        @JsonProperty("page_size")
        private Integer pageSize;

        @Schema(description = "相似度阈value (default 0.2)")
        @JsonProperty("similarity_threshold")
        private Float similarityThreshold;

        @Schema(description = "vector相似度权重 (default 0.3)")
        @JsonProperty("vector_similarity_weight")
        private Float vectorSimilarityWeight;

        @Schema(description = "return Top K slice (default 1024)")
        @JsonProperty("top_k")
        private Integer topK;

        @Schema(description = "重Sort ordermodel ID")
        @JsonProperty("rerank_id")
        private String rerankId;

        @Schema(description = "YesNo高亮keyword")
        private Boolean highlight;

        @Schema(description = "YesNoenablekeywordretrieve")
        private Boolean keyword;

        @Schema(description = "跨Language翻译list (可选)")
        @JsonProperty("cross_languages")
        private List<String> crossLanguages;

        @Schema(description = "datafilteritems件 (JSON object)")
        @JsonProperty("metadata_condition")
        private Map<String, Object> metadataCondition;
    }

    /**
     * retrieve命result (VO)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "retrieve命slicedetails")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HitVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "slice ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private String id;

        @Schema(description = "slicecontent", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;

        @Schema(description = "belonging todocument ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("document_id")
        private String documentId;

        @Schema(description = "belonging toKnowledge base ID")
        @JsonProperty("dataset_id")
        private String datasetId;

        @Schema(description = "documentname")
        @JsonProperty("document_name")
        private String documentName;

        @Schema(description = "documentkeyword")
        @JsonProperty("document_keyword")
        private String documentKeyword;

        @Schema(description = "综合相似度", requiredMode = Schema.RequiredMode.REQUIRED)
        private Float similarity;

        @Schema(description = "vector相似度")
        @JsonProperty("vector_similarity")
        private Float vectorSimilarity;

        @Schema(description = "keyword相似度")
        @JsonProperty("term_similarity")
        private Float termSimilarity;

        @Schema(description = "indexbit置")
        private Integer index;

        @Schema(description = "高亮content")
        private String highlight;

        @Schema(description = "重need tokeywordlist")
        @JsonProperty("important_keywords")
        private List<String> importantKeywords;

        @Schema(description = "预设questionlist")
        private List<String> questions;

        @Schema(description = "图片 ID")
        @JsonProperty("image_id")
        private String imageId;

        @Schema(description = "bit置index (RAGFlowreturn嵌套array, e.g. [[start, end, filename]])")
        private Object positions;
    }

    /**
     * Knowledge basedata摘need to (VO)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Knowledge basedata摘need toinformation")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaSummaryVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "documenttotal", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("total_doc_count")
        private Long totalDocCount;

        @Schema(description = "Token total", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("total_token_count")
        private Long totalTokenCount;

        @Schema(description = "File type分布 (key: file后缀, value: count)")
        @JsonProperty("file_type_distribution")
        private Map<String, Long> fileTypeDistribution;

        @Schema(description = "文status分布 (key: statuscode, value: count)")
        @JsonProperty("status_distribution")
        private Map<String, Long> statusDistribution;

        @Schema(description = "custom metadatastatistics (key: field name, value: count/value)")
        @JsonProperty("custom_metadata")
        private Map<String, Object> customMetadata;
    }

    /**
     * batchupdatedatarequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "batchupdatedatarequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaBatchReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "filter: used forspecifiedneed toupdate document范围 (defaultAll)")
        private Selector selector;

        @Schema(description = "addorupdate datalist")
        private List<UpdateItem> updates;

        @Schema(description = "needdelete data键list")
        private List<DeleteItem> deletes;

        /**
         * documentfilter
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "dataupdatefilter")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Selector implements Serializable {
            private static final long serialVersionUID = 1L;

            @Schema(description = "specifieddocument ID list")
            @JsonProperty("document_ids")
            private List<String> documentIds;

            @Schema(description = "dataitems件匹配 (key: field name, value: 匹配value)")
            @JsonProperty("metadata_condition")
            private Map<String, Object> metadataCondition;
        }

        /**
         * updateitem
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "dataupdateitem")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class UpdateItem implements Serializable {
            private static final long serialVersionUID = 1L;

            @Schema(description = "data键名", requiredMode = Schema.RequiredMode.REQUIRED)
            private String key;

            @Schema(description = "datavalue", requiredMode = Schema.RequiredMode.REQUIRED)
            private Object value;
        }

        /**
         * deleteitem
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "datadeleteitem")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DeleteItem implements Serializable {
            private static final long serialVersionUID = 1L;

            @Schema(description = "需delete data键名", requiredMode = Schema.RequiredMode.REQUIRED)
            private String key;
        }
    }

    /**
     * recalltestresultaggregationresponse
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "recalltestresultaggregationresponse")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "retrieve命 slicelist")
        private List<HitVO> chunks;

        @Schema(description = "document分布statistics")
        @JsonProperty("doc_aggs")
        private List<DocAggVO> docAggs;

        @Schema(description = "total命recordnumber")
        private Long total;
    }
}
