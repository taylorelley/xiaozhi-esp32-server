package xiaozhi.modules.config.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single row in a config-import summary. Used for both applied and skipped
 * selected_module entries, applied per-provider field merges, and generic
 * warnings. Unused fields are left null for the target row type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Config import summary item")
public class ConfigImportItemDTO {

    @Schema(description = "Module type (VAD/ASR/LLM/VLLM/TTS/Memory/Intent)")
    private String type;

    @Schema(description = "Provider code (e.g. OpenaiASR, EdgeTTS)")
    private String provideCode;

    @Schema(description = "Model id resolved from provider code (selected_module rows only)")
    private String modelId;

    @Schema(description = "Field names merged into configJson (per-provider rows only)")
    private List<String> fields;

    @Schema(description = "Reason the row was skipped (skipped rows only)")
    private String reason;

    public static ConfigImportItemDTO applied(String type, String provideCode, String modelId) {
        ConfigImportItemDTO item = new ConfigImportItemDTO();
        item.type = type;
        item.provideCode = provideCode;
        item.modelId = modelId;
        return item;
    }

    public static ConfigImportItemDTO skipped(String type, String provideCode, String reason) {
        ConfigImportItemDTO item = new ConfigImportItemDTO();
        item.type = type;
        item.provideCode = provideCode;
        item.reason = reason;
        return item;
    }

    public static ConfigImportItemDTO fields(String type, String provideCode, List<String> fields) {
        ConfigImportItemDTO item = new ConfigImportItemDTO();
        item.type = type;
        item.provideCode = provideCode;
        item.fields = fields;
        return item;
    }
}
