package xiaozhi.modules.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Voiceprint identificationinterfacereturn object
 */
@Data
public class IdentifyVoicePrintResponse {
    /**
     * 最匹配 voiceprintid
     */
    @JsonProperty("speaker_id")
    private String speakerId;
    /**
     * voiceprint 分number
     */
    private Double score;
}
