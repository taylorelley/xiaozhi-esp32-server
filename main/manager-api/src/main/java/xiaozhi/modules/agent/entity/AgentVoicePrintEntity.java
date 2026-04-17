package xiaozhi.modules.agent.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * agentvoiceprinttable
 *
 * @author zjy
 */
@TableName(value = "ai_agent_voice_print")
@Data
public class AgentVoicePrintEntity {
    /**
     * Primary keyid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * associated agentid
     */
    private String agentId;
    /**
     * associated audioid
     */
    private String audioId;
    /**
     * voiceprintsource personName
     */
    private String sourceName;
    /**
     * Descriptionvoiceprintsource person
     */
    private String introduce;

    /**
     * Creator
     */
    @TableField(fill = FieldFill.INSERT)
    private Long creator;
    /**
     * Create time
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    /**
     * update
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    /**
     * updatetime
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
}
