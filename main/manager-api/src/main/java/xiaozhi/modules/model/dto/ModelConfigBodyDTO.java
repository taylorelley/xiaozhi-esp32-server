package xiaozhi.modules.model.dto;

import java.io.Serial;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Model provider/ provider")
public class ModelConfigBodyDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Model ID,notfillwritewillautomaticgenerate")
    private String id;

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

    @Schema(description = "officialdocumentlink")
    private String docLink;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Sort order")
    private Integer sort;
}
