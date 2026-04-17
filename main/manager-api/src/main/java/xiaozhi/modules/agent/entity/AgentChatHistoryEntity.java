package xiaozhi.modules.agent.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * agentChat historytable
 *
 * @author Goody
 * @version 1.0, 2025/4/30
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "ai_agent_chat_history")
public class AgentChatHistoryEntity {
    /**
     * Primary keyID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * MACAddress
     */
    @TableField(value = "mac_address")
    private String macAddress;

    /**
     * agentid
     */
    @TableField(value = "agent_id")
    private String agentId;

    /**
     * Session ID
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * messagetype: 1-user, 2-agent
     */
    @TableField(value = "chat_type")
    private Byte chatType;

    /**
     * chatcontent
     */
    @TableField(value = "content")
    private String content;

    /**
     * audiobase64data
     */
    @TableField(value = "audio_id")
    private String audioId;

    /**
     * Create time
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * updatetime
     */
    @TableField(value = "updated_at")
    private Date updatedAt;
}
