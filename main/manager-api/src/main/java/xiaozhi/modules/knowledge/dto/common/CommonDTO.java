package xiaozhi.modules.knowledge.dto.common;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

@Schema(description = "useextensionfunction DTO")
public class CommonDTO {

    // ========== 1. referencedetails (detail_share_embedded) ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "referencedetailsrequest")
    public static class ReferenceDetailReq implements Serializable {
        @Schema(description = "slice ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "slice ID cannot be empty")
        @JsonProperty("chunk_id")
        private String chunkId;

        @Schema(description = "Knowledge base ID")
        @JsonProperty("knowledge_id")
        private String knowledgeId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "referencedetailsresponse")
    public static class ReferenceDetailVO implements Serializable {
        @Schema(description = "slice ID")
        @JsonProperty("chunk_id")
        private String chunkId;

        @Schema(description = "completecontent")
        @JsonProperty("content_with_weight")
        private String contentWithWeight;

        @Schema(description = "documentname")
        @JsonProperty("doc_name")
        private String docName;

        @Schema(description = "image ID list")
        @JsonProperty("img_id")
        private String imageId; // Note：RAGFlow haswhenreturn String haswhenreturn List，needaccording tosituationconfirm，temporarily String used for ID

        @Schema(description = "document ID")
        @JsonProperty("doc_id")
        private String docId;
    }

    // ========== 2. useQ&A (ask_about) - adjusttryuse ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "useQ&Arequest (adjusttryuse)")
    public static class AskAboutReq implements Serializable {
        @Schema(description = "userquestion", requiredMode = Schema.RequiredMode.REQUIRED, example = "What is this dataset about?")
        @NotBlank(message = "questioncannot be empty")
        @JsonProperty("question")
        private String question;

        @Schema(description = "datacollection ID list", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "datacollectionlistcannot be empty")
        @JsonProperty("dataset_ids")
        private List<String> datasetIds;
    }

    // responsecommonre-use String orsimple  Map resultconstruct，viewhaveimplementwhile，temporarilynot definespecialuse VO
}
