package xiaozhi.modules.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "voiceinformation")
public class VoiceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Voice ID")
    private String id;

    @Schema(description = "voicename")
    private String name;

    @Schema(description = "audioplayAddress")
    private String voiceDemo;
    
    @Schema(description = "Languagetype")
    private String languages;
    
    @Schema(description = "YesNoasclonevoice")
    private Boolean isClone;

    // add双parameterconstructfunction，maintain backward compatibility
    public VoiceDTO(String id, String name) {
        this.id = id;
        this.name = name;
        this.voiceDemo = null;
        this.languages = null;
        this.isClone = false; // defaultnot Yesclonevoice
    }
    
    // add三parameterconstructfunction，used for普通voice
    public VoiceDTO(String id, String name, String voiceDemo) {
        this.id = id;
        this.name = name;
        this.voiceDemo = voiceDemo;
        this.languages = null;
        this.isClone = false;
    }

}