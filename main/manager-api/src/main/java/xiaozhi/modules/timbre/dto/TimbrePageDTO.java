package xiaozhi.modules.timbre.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * voicepaginationparameterDTO
 * 
 * @author zjy
 * @since 2025-3-21
 */
@Data
@Schema(description = "voicepaginationparameter")
public class TimbrePageDTO {

    @Schema(description = "corresponding TTS modelPrimary key")
    @NotBlank(message = "{timbre.ttsModelId.require}")
    private String ttsModelId;

    @Schema(description = "voicename")
    private String name;

    @Schema(description = "page count")
    private String page;

    @Schema(description = "column count")
    private String limit;
}
