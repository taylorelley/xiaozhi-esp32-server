package xiaozhi.modules.agent.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * agentupdateDTO
 * specialused forupdateagent，idfieldYesmustneed ，used foridentifierneed toupdate agent
 * otherfieldaverageasnon-required，onlyupdateprovide field
 */
@Data
@Schema(description = "agentupdateobject")
public class AgentUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Agent code", example = "AGT_1234567890", nullable = true)
    private String agentCode;

    @Schema(description = "Agent name", example = "customer serviceassistant", nullable = true)
    private String agentName;

    @Schema(description = "voiceidentifymodelidentifier", example = "asr_model_02", nullable = true)
    private String asrModelId;

    @Schema(description = "voiceactivitydetectidentifier", example = "vad_model_02", nullable = true)
    private String vadModelId;

    @Schema(description = "largeLanguagemodelidentifier", example = "llm_model_02", nullable = true)
    private String llmModelId;

    @Schema(description = "smallmodelidentifier", example = "slm_model_02", nullable = true)
    private String slmModelId;

    @Schema(description = "VLLMmodelidentifier", example = "vllm_model_02", required = false)
    private String vllmModelId;

    @Schema(description = "text-to-speechmodelidentifier", example = "tts_model_02", required = false)
    private String ttsModelId;

    @Schema(description = "voiceidentifier", example = "voice_02", nullable = true)
    private String ttsVoiceId;

    @Schema(description = "voiceLanguage", example = "ordinarytalk", nullable = true)
    private String ttsLanguage;

    @Schema(description = "TTSvolume", example = "50", nullable = true)
    private Integer ttsVolume;

    @Schema(description = "TTSspeech rate", example = "50", nullable = true)
    private Integer ttsRate;

    @Schema(description = "TTStone", example = "50", nullable = true)
    private Integer ttsPitch;

    @Schema(description = "memorymodelidentifier", example = "mem_model_02", nullable = true)
    private String memModelId;

    @Schema(description = "Intentmodelidentifier", example = "intent_model_02", nullable = true)
    private String intentModelId;

    @Schema(description = "pluginfunctioninformation", nullable = true)
    private List<FunctionInfo> functions;

    @Schema(description = "Rolesetparameter", example = "youYesonespecialbusiness customer serviceassistant，responsiblereturnansweruserquestionandprovidehelp", nullable = true)
    private String systemPrompt;

    @Schema(description = "summary memory", example = "Build a dynamic memory network that can grow，Retain key information in limited spaceinformation simultaneously，Intelligently maintaininformationevolution trajectory\n"
            + "according toconversationrecord，summaryuser re-need toinformation，so that in futureconversation to provide more personalized service", nullable = true)
    private String summaryMemory;

    @Schema(description = "Chat historyconfiguration（0not record 1onlyrecordtext 2recordtext and voice）", example = "3", nullable = true)
    private Integer chatHistoryConf;

    @Schema(description = "Languagecode", example = "zh_CN", nullable = true)
    private String langCode;

    @Schema(description = "interactionkind", example = "Chinese", nullable = true)
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