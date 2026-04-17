package xiaozhi.modules.knowledge.dto.document;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;

/**
 * slicemanagementaggregation DTO
 */
@Schema(description = "slicemanagementaggregation DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChunkDTO {

    /**
     * addslicerequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "addslicerequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "slicecontent", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "slicecontentcannot be empty")
        private String content;

        @Schema(description = "re-need tokeywordlist")
        @JsonProperty("important_keywords")
        private List<String> importantKeywords;

        @Schema(description = "预设questionlist")
        private List<String> questions;
    }

    /**
     * updateslicerequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "updateslicerequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "new slicecontent")
        private String content;

        @Schema(description = "updatekeywordlist (覆盖原haslist)")
        @JsonProperty("important_keywords")
        private List<String> importantKeywords;

        @Schema(description = "enable/disable (true: enable, false: disable)")
        private Boolean available;
    }

    /**
     * getslicelistrequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "getslicelistrequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "page number (default 1)")
        private Integer page;

        @Schema(description = "per pagecount (default 30)")
        @JsonProperty("page_size")
        private Integer pageSize;

        @Schema(description = "searchkeyword (全文retrieve)")
        private String keywords;

        @Schema(description = "精确slice ID")
        private String id;
    }

    /**
     * batchdeleteslicerequestparameter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "batchdeleteslicerequestparameter")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RemoveReq implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "slice ID list", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("chunk_ids")
        @NotEmpty(message = "sliceIDlistcannot be empty")
        private List<String> chunkIds;
    }

    /**
     * Document sliceinformation VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Document sliceinformation")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InfoVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "slice ID (usually  document_id + index)", requiredMode = Schema.RequiredMode.REQUIRED)
        private String id;

        @Schema(description = "slicetextcontent (全文retrieve 主need toobject)", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;

        @Schema(description = "belonging todocument ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("document_id")
        private String documentId;

        @Schema(description = "documentname / keyword")
        @JsonProperty("docnm_kwd")
        private String docnmKwd;

        @Schema(description = "re-need tokeywordlist (used forkeyword增strongretrieve)")
        @JsonProperty("important_keywords")
        private List<String> importantKeywords;

        @Schema(description = "预设questionlist (used for Q&A mode增strong)")
        private List<String> questions;

        @Schema(description = "associated 图片 ID")
        @JsonProperty("image_id")
        private String imageId;

        @Schema(description = "belonging toKnowledge base ID")
        @JsonProperty("dataset_id")
        private String datasetId;

        @Schema(description = "sliceYesNoavailable (true: 参andretrieve, false: isdisable)")
        private Boolean available;

        @Schema(description = "slicein原文 bit置indexlist (RAGFlowreturn嵌套array, e.g. [[start, end, filename]])")
        private List<List<Object>> positions;

        @Schema(description = "Token ID list")
        @JsonProperty("token")
        private List<Integer> token;
    }

    /**
     * 分片listaggregationresponse
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分片listaggregationresponse")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "sliceinformationlist")
        private List<InfoVO> chunks;

        @Schema(description = "associated document详细information")
        private DocumentDTO.InfoVO doc;

        @Schema(description = "totalrecordnumber")
        private Long total;
    }
}
