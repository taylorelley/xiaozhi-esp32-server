package xiaozhi.modules.agent.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("ai_agent_tag")
@Schema(description = "agentTag")
public class AgentTagEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "Primary key")
    private String id;

    @Schema(description = "Tagname")
    private String tagName;

    @Schema(description = "Sort order")
    private Integer sort;

    @Schema(description = "Creator")
    private Long creator;

    @Schema(description = "Create time")
    private Date createdAt;

    @Schema(description = "update")
    private Long updater;

    @Schema(description = "updatetime")
    private Date updatedAt;

    @Schema(description = "delete标记")
    private Integer deleted;
}
