package xiaozhi.modules.knowledge.dto.chat;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * chatconversationrequest DTO (OpenAI compatibleformat)
 */
@Data
@Schema(description = "chatconversationrequest")
public class ChatCompletionRequest implements Serializable {

    @Schema(description = "modelidentifier (corresponding agent_id or bot_id)", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("model")
    private String model;

    @Schema(description = "conversationmessagelist", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("messages")
    private List<Message> messages;

    @Schema(description = "YesNostreamingreturn", defaultValue = "false")
    @JsonProperty("stream")
    private Boolean stream = false;

    @Schema(description = "indoor climatenumber (0-1)", defaultValue = "0.7")
    @JsonProperty("temperature")
    private Double temperature;

    @Schema(description = "Session ID (canselect，used fordelaycontinuesession)")
    @JsonProperty("session_id")
    private String sessionId;

    @Schema(description = "otherRAGFlowspecialparameter (canselect)")
    private Map<String, Object> extra;

    @Data
    public static class Message implements Serializable {
        @Schema(description = "Role (system, user, assistant)", requiredMode = Schema.RequiredMode.REQUIRED)
        private String role;

        @Schema(description = "content", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;
    }
}
