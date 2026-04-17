package xiaozhi.modules.agent.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * Agent configurationtemplatetable
 * 
 * @TableName ai_agent_template
 */
@TableName(value = "ai_agent_template")
@Data
public class AgentTemplateEntity implements Serializable {
    /**
     * agentunique identifier
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * Agent code
     */
    private String agentCode;

    /**
     * Agent name
     */
    private String agentName;

    /**
     * voiceidentifymodelidentifier
     */
    private String asrModelId;

    /**
     * voiceactivitydetectidentifier
     */
    private String vadModelId;

    /**
     * largeLanguagemodelidentifier
     */
    private String llmModelId;

    /**
     * VLLMmodelidentifier
     */
    private String vllmModelId;

    /**
     * text-to-speechmodelidentifier
     */
    private String ttsModelId;

    /**
     * voiceidentifier
     */
    private String ttsVoiceId;

    /**
     * voiceLanguage
     */
    private String ttsLanguage;

    /**
     * TTSvolume
     */
    private Integer ttsVolume;

    /**
     * TTSspeech rate
     */
    private Integer ttsRate;

    /**
     * TTStone
     */
    private Integer ttsPitch;

    /**
     * memorymodelidentifier
     */
    private String memModelId;

    /**
     * Intentmodelidentifier
     */
    private String intentModelId;

    /**
     * Chat historyconfiguration（0not record 1onlyrecordtext 2recordtext and voice）
     */
    private Integer chatHistoryConf;

    /**
     * Rolesetparameter
     */
    private String systemPrompt;

    /**
     * summary memory
     */
    private String summaryMemory;
    /**
     * Languagecode
     */
    private String langCode;

    /**
     * interactionkind
     */
    private String language;

    /**
     * Sort orderpermissionre-
     */
    private Integer sort;

    /**
     * Creator ID
     */
    private Long creator;

    /**
     * Create time
     */
    private Date createdAt;

    /**
     * update ID
     */
    private Long updater;

    /**
     * updatetime
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}