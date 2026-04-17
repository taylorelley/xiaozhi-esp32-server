package xiaozhi.modules.knowledge.dto.bot;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

@Schema(description = "externalrobot (Bot) aggregation DTO")
public class BotDTO {

    // ========== 1. SearchBot (retrieverobot) ==========

    // corresponding /api/v1/searchbots/ask
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "SearchBot askrequest")
    public static class SearchAskReq implements Serializable {
        @Schema(description = "userquestion", requiredMode = Schema.RequiredMode.REQUIRED, example = "What is RAG?")
        @NotBlank(message = "questioncannot be empty")
        @JsonProperty("question")
        private String question;

        @Schema(description = "YesNoreturnreference", defaultValue = "false")
        @JsonProperty("quote")
        @Builder.Default
        private Boolean quote = false;

        @Schema(description = "YesNostreamingreturn", defaultValue = "true")
        @JsonProperty("stream")
        @Builder.Default
        private Boolean stream = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "SearchBot askresponse")
    public static class SearchAskVO implements Serializable {
        @Schema(description = "returnanswercontent")
        @JsonProperty("answer")
        private String answer;

        @Schema(description = "referencesource (Value resultconstructcommoncorresponding RetrievalDTO.HitVO)")
        @JsonProperty("reference")
        private Map<String, Object> reference;
    }

    // corresponding /api/v1/searchbots/related_questions
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "relatedquestionrequest")
    public static class RelatedQuestionReq implements Serializable {
        @Schema(description = "userquestion", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "questioncannot be empty")
        @JsonProperty("question")
        private String question;
    }

    // corresponding /api/v1/searchbots/mindmap
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "mind maprequest")
    public static class MindMapReq implements Serializable {
        @Schema(description = "userquestion", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "questioncannot be empty")
        @JsonProperty("question")
        private String question;
    }

    // ========== 2. AgentBot (embeddingstyle Agent) ==========

    // corresponding /api/v1/agentbots/{id}/inputs
    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "AgentBot inputparameterrequest")
    public static class AgentInputsReq implements Serializable {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "AgentBot inputparameterdefineresponse")
    public static class AgentInputsVO implements Serializable {
        @Schema(description = "tablechangeamountdefinelist")
        @JsonProperty("variables")
        private List<Map<String, Object>> variables;
    }

    // corresponding /api/v1/agentbots/{id}/completions
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "AgentBot conversationrequest")
    public static class AgentCompletionReq implements Serializable {
        @Schema(description = "inputParameter value")
        @JsonProperty("inputs")
        private Map<String, Object> inputs;

        @Schema(description = "userquery", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "querycontentcannot be empty")
        @JsonProperty("question")
        private String question;

        @Schema(description = "YesNostreamingreturn", defaultValue = "true")
        @JsonProperty("stream")
        @Builder.Default
        private Boolean stream = true;

        @Schema(description = "session ID")
        @JsonProperty("session_id")
        private String sessionId;
    }
}
