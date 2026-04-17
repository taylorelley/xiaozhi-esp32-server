package xiaozhi.modules.knowledge.dto.bot;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

@Schema(description = "外部机人 (Bot) aggregation DTO")
public class BotDTO {

    // ========== 1. SearchBot (retrieve机人) ==========

    // corresponding /api/v1/searchbots/ask
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "SearchBot 提问request")
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
    @Schema(description = "SearchBot 提问response")
    public static class SearchAskVO implements Serializable {
        @Schema(description = "回答content")
        @JsonProperty("answer")
        private String answer;

        @Schema(description = "referencesource (Value 结构通常corresponding RetrievalDTO.HitVO)")
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
    @Schema(description = "思维导图request")
    public static class MindMapReq implements Serializable {
        @Schema(description = "userquestion", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "questioncannot be empty")
        @JsonProperty("question")
        private String question;
    }

    // ========== 2. AgentBot (embedding式 Agent) ==========

    // corresponding /api/v1/agentbots/{id}/inputs
    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "AgentBot 输入parameterrequest")
    public static class AgentInputsReq implements Serializable {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "AgentBot 输入parameterdefineresponse")
    public static class AgentInputsVO implements Serializable {
        @Schema(description = "table变量definelist")
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
        @Schema(description = "输入Parameter value")
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
