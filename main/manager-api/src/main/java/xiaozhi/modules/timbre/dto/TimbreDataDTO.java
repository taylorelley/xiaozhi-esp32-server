package xiaozhi.modules.timbre.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * voicetabledataDTO
 * 
 * @author zjy
 * @since 2025-3-21
 */
@Data
@Schema(description = "voicetableinformation")
public class TimbreDataDTO {

    @Schema(description = "Language")
    @NotBlank(message = "{timbre.languages.require}")
    private String languages;

    @Schema(description = "voicename")
    @NotBlank(message = "{timbre.name.require}")
    private String name;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "referenceaudiopath")
    private String referenceAudio;

    @Schema(description = "參考text")
    private String referenceText;

    @Schema(description = "Sort order")
    @Min(value = 0, message = "{sort.number}")
    private long sort;

    @Schema(description = "corresponding TTS modelPrimary key")
    @NotBlank(message = "{timbre.ttsModelId.require}")
    private String ttsModelId;

    @Schema(description = "voicecode")
    @NotBlank(message = "{timbre.ttsVoice.require}")
    private String ttsVoice;

    @Schema(description = "audioplayAddress")
    private String voiceDemo;
}