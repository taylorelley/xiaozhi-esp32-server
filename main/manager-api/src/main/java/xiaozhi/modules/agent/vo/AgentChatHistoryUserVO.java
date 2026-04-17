package xiaozhi.modules.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * agentuser人chatdata VO
 */
@Data
public class AgentChatHistoryUserVO {
    @Schema(description = "chatcontent")
    private String content;

    @Schema(description = "audioID")
    private String audioId;
}
