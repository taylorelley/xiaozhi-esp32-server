package xiaozhi.modules.agent.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * agentupdateDTO
 * 专used forupdateagent，idfieldYes必需 ，used foridentifierneed toupdate agent
 * 其他field均as非必填，onlyupdate提供 field
 */
@Data
@Schema(description = "agentupdateobject")
public class AgentUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Agent code", example = "AGT_1234567890", nullable = true)
    private String agentCode;

    @Schema(description = "Agent name", example = "customer serviceassistant", nullable = true)
    private String agentName;

    @Schema(description = "voice识别modelidentifier", example = "asr_model_02", nullable = true)
    private String asrModelId;

    @Schema(description = "voice活动detectidentifier", example = "vad_model_02", nullable = true)
    private String vadModelId;

    @Schema(description = "largeLanguagemodelidentifier", example = "llm_model_02", nullable = true)
    private String llmModelId;

    @Schema(description = "小modelidentifier", example = "slm_model_02", nullable = true)
    private String slmModelId;

    @Schema(description = "VLLMmodelidentifier", example = "vllm_model_02", required = false)
    private String vllmModelId;

    @Schema(description = "text-to-speechmodelidentifier", example = "tts_model_02", required = false)
    private String ttsModelId;

    @Schema(description = "voiceidentifier", example = "voice_02", nullable = true)
    private String ttsVoiceId;

    @Schema(description = "voiceLanguage", example = "普通话", nullable = true)
    private String ttsLanguage;

    @Schema(description = "TTSvolume", example = "50", nullable = true)
    private Integer ttsVolume;

    @Schema(description = "TTS语速", example = "50", nullable = true)
    private Integer ttsRate;

    @Schema(description = "TTS音调", example = "50", nullable = true)
    private Integer ttsPitch;

    @Schema(description = "memorymodelidentifier", example = "mem_model_02", nullable = true)
    private String memModelId;

    @Schema(description = "Intentmodelidentifier", example = "intent_model_02", nullable = true)
    private String intentModelId;

    @Schema(description = "pluginfunctioninformation", nullable = true)
    private List<FunctionInfo> functions;

    @Schema(description = "Rolesetparameter", example = "youYes一个专业 customer serviceassistant，负责回答userquestion并提供帮助", nullable = true)
    private String systemPrompt;

    @Schema(description = "summary memory", example = "Build a dynamic memory network that can grow，Retain key information in limited spaceinformation 同时，Intelligently maintaininformationevolution trajectory\n"
            + "according toconversationrecord，summaryuser 重need toinformation，so that in futureconversation to provide more personalized service", nullable = true)
    private String summaryMemory;

    @Schema(description = "Chat historyconfiguration（0not record 1onlyrecordtext 2recordtext and voice）", example = "3", nullable = true)
    private Integer chatHistoryConf;

    @Schema(description = "Languagecode", example = "zh_CN", nullable = true)
    private String langCode;

    @Schema(description = "交互语种", example = "Chinese", nullable = true)
    private String language;

    @Schema(description = "Sort order", example = "1", nullable = true)
    private Integer sort;

    @Schema(description = "contextsourceconfiguration", nullable = true)
    private List<ContextProviderDTO> contextProviders;

    @Data
    @Schema(description = "pluginfunctioninformation")
    public static class FunctionInfo implements Serializable {
        @Schema(description = "pluginID", example = "plugin_01")
        private String pluginId;

        @Schema(description = "functionparameterinformation", nullable = true)
        private HashMap<String, Object> paramInfo;

        private static final long serialVersionUID = 1L;
    }
}