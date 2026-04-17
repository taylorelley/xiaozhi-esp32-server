package xiaozhi.modules.agent.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * agentChat historyDTO
 */
@Data
@Schema(description = "agentChat history")
public class AgentChatHistoryDTO {
    @Schema(description = "Create time")
    private Date createdAt;

    @Schema(description = "messagetype: 1-user, 2-agent")
    private Byte chatType;

    @Schema(description = "chatcontent")
    private String content;

    @Schema(description = "audioID")
    private String audioId;

    @Schema(description = "MACAddress")
    private String macAddress;
}