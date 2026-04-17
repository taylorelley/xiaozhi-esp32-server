package xiaozhi.modules.agent.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Agentandplugin unique mapping table
 * 
 * @TableName ai_agent_plugin_mapping
 */
@Data
@TableName(value = "ai_agent_plugin_mapping")
@Schema(description = "Agentandplugin unique mapping table")
public class AgentPluginMapping implements Serializable {
    /**
     * Primary key
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "mappinginformationPrimary keyID")
    private Long id;

    /**
     * Agent ID
     */
    @Schema(description = "Agent ID")
    private String agentId;

    /**
     * pluginID
     */
    @Schema(description = "pluginID")
    private String pluginId;

    /**
     * pluginparameter(Json)format
     */
    @Schema(description = "pluginparameter(Json)format")
    private String paramInfo;

    // 冗余field，used for方便inaccording toidquerypluginwhen，foraccording to查出plugin Provider_code,详见daolayerxmlfile
    @TableField(exist = false)
    @Schema(description = "pluginprovider_code, correspondingtableai_model_provider")
    private String providerCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}