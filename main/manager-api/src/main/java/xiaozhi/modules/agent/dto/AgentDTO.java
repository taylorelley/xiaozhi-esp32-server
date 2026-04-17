package xiaozhi.modules.agent.dto;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xiaozhi.modules.agent.dto.AgentTagDTO;

/**
 * agentdatatransfer object
 * used forinservicelayerandcontrollayer之间传递agentrelated data
 */
@Data
@Schema(description = "agentobject")
public class AgentDTO {
    @Schema(description = "Agent code", example = "AGT_1234567890")
    private String id;

    @Schema(description = "Agent name", example = "customer serviceassistant")
    private String agentName;

    @Schema(description = "text-to-speechModel name", example = "tts_model_01")
    private String ttsModelName;

    @Schema(description = "voicename", example = "voice_01")
    private String ttsVoiceName;

    @Schema(description = "largeLanguageModel name", example = "llm_model_01")
    private String llmModelName;

    @Schema(description = "视觉Model name", example = "vllm_model_01")
    private String vllmModelName;

    @Schema(description = "memoryModel ID", example = "mem_model_01")
    private String memModelId;

    @Schema(description = "Rolesetparameter", example = "youYesone专业 customer serviceassistant，负责回答userquestionand提供帮助")
    private String systemPrompt;

    @Schema(description = "summary memory", example = "Build a dynamic memory network that can grow，Retain key information in limited spaceinformation simultaneously，Intelligently maintaininformationevolution trajectory\n" +
            "according toconversationrecord，summaryuser re-need toinformation，so that in futureconversation to provide more personalized service", required = false)
    private String summaryMemory;

    @Schema(description = "lastconnectiontime", example = "2024-03-20 10:00:00")
    private Date lastConnectedAt;

    @Schema(description = "Device count", example = "10")
    private Integer deviceCount;

    @Schema(description = "Taglist")
    private List<AgentTagDTO> tags;
}