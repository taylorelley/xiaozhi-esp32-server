package xiaozhi.modules.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Voiceprint identificationinterfacereturn object
 */
@Data
public class IdentifyVoicePrintResponse {
    /**
     * mostmatch voiceprintid
     */
    @JsonProperty("speaker_id")
    private String speakerId;
    /**
     * voiceprint number
     */
    private Double score;
}
