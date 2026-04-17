package xiaozhi.modules.model.dto;

import java.io.Serial;
import java.io.Serializable;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Model provider/ provider")
public class ModelConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary key")
    private String id;

    @Schema(description = "Model type(Memory/ASR/VAD/LLM/TTS)")
    private String modelType;

    @Schema(description = "Model code(e.g.AliLLM、DoubaoTTS)")
    private String modelCode;

    @Schema(description = "Model name")
    private String modelName;

    @Schema(description = "YesNodefaultconfiguration(0No 1Yes)")
    private Integer isDefault;

    @Schema(description = "YesNoenable")
    private Integer isEnabled;

    @Schema(description = "Model configuration(JSONformat)")
    private JSONObject configJson;

    @Schema(description = "官方documentlink")
    private String docLink;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Sort order")
    private Integer sort;
}
