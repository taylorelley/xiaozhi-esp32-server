package xiaozhi.modules.voiceclone.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Voice cloneresponseDTO
 * used fordirectionbeforeenddisplayVoice cloneinformation，containModel nameandUsernamename
 */
@Data
@Schema(description = "Voice cloneresponseDTO")
public class VoiceCloneResponseDTO {

    @Schema(description = "unique identifier")
    private String id;

    @Schema(description = "voicename")
    private String name;

    @Schema(description = "modelid")
    private String modelId;

    @Schema(description = "Model name")
    private String modelName;

    @Schema(description = "voiceid")
    private String voiceId;

    @Schema(description = "Language")
    private String languages;

    @Schema(description = "User ID（associatedusertable）")
    private Long userId;

    @Schema(description = "Usernamename")
    private String userName;

    @Schema(description = "trainingstatus：0pendingtraining 1training 2trainingsuccess 3trainingfailed")
    private Integer trainStatus;

    @Schema(description = "trainingerrorreason")
    private String trainError;

    @Schema(description = "Create time")
    private Date createDate;

    @Schema(description = "YesNohasaudio data")
    private Boolean hasVoice;
}