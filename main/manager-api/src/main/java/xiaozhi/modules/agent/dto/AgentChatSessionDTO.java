package xiaozhi.modules.agent.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * agentsessionlistDTO
 */
@Data
public class AgentChatSessionDTO {
    /**
     * Session ID
     */
    private String sessionId;

    /**
     * sessiontime
     */
    private LocalDateTime createdAt;

    /**
     * chatitemsnumber
     */
    private Integer chatCount;

    /**
     * sessiontitle
     */
    private String title;
}