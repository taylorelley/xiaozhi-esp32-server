package xiaozhi.modules.agent.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("ai_agent")
@Schema(description = "Agent information")
public class AgentEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "agentunique identifier")
    private String id;

    @Schema(description = "belonging toUser ID")
    private Long userId;

    @Schema(description = "Agent code")
    private String agentCode;

    @Schema(description = "Agent name")
    private String agentName;

    @Schema(description = "voiceidentifymodelidentifier")
    private String asrModelId;

    @Schema(description = "voiceactivitydetectidentifier")
    private String vadModelId;

    @Schema(description = "largeLanguagemodelidentifier")
    private String llmModelId;

    @Schema(description = "smallmodelidentifier")
    private String slmModelId;

    @Schema(description = "VLLMmodelidentifier")
    private String vllmModelId;

    @Schema(description = "text-to-speechmodelidentifier")
    private String ttsModelId;

    @Schema(description = "voiceidentifier")
    private String ttsVoiceId;

    @Schema(description = "voiceLanguage")
    private String ttsLanguage;

    @Schema(description = "TTSvolume")
    private Integer ttsVolume;

    @Schema(description = "TTSspeech rate")
    private Integer ttsRate;

    @Schema(description = "TTStone")
    private Integer ttsPitch;

    @Schema(description = "memorymodelidentifier")
    private String memModelId;

    @Schema(description = "Intentmodelidentifier")
    private String intentModelId;

    @Schema(description = "Chat historyconfiguration（0not record 1onlyrecordtext 2recordtext and voice）")
    private Integer chatHistoryConf;

    @Schema(description = "Rolesetparameter")
    private String systemPrompt;

    @Schema(description = "summary memory", example = "Build a dynamic memory network that can grow，Retain key information in limited spaceinformation simultaneously，Intelligently maintaininformationevolution trajectory\n" +
            "according toconversationrecord，summaryuser re-need toinformation，so that in futureconversation to provide more personalized service", required = false)
    private String summaryMemory;

    @Schema(description = "Languagecode")
    private String langCode;

    @Schema(description = "interactionkind")
    private String language;

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
}