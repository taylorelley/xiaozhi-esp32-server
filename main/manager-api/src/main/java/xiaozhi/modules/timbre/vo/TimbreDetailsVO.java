package xiaozhi.modules.timbre.vo;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * voicedetails展示VO
 * 
 * @author zjy
 * @since 2025-3-21
 */
@Data
public class TimbreDetailsVO implements Serializable {
    @Schema(description = "voiceid")
    private String id;

    @Schema(description = "Language")
    private String languages;

    @Schema(description = "voicename")
    private String name;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "referenceaudiopath")
    private String referenceAudio;

    @Schema(description = "參考text")
    private String referenceText;

    @Schema(description = "Sort order")
    private long sort;

    @Schema(description = "corresponding TTS modelPrimary key")
    private String ttsModelId;

    @Schema(description = "voicecode")
    private String ttsVoice;

    @Schema(description = "audioplayAddress")
    private String voiceDemo;

}
