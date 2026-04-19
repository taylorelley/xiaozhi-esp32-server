package xiaozhi.modules.config.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Result of a POST /config/upload request. Contains lists of applied and
 * skipped items plus non-fatal warnings so the UI can render a before/after
 * diff to the operator.
 */
@Data
@Schema(description = "Config import result")
public class ConfigImportResultDTO {

    @Schema(description = "True when the request was a dry-run (no DB writes performed)")
    private boolean dryRun;

    @Schema(description = "Merge mode: 'merge' (default) keeps untouched configJson keys, 'replace' overwrites the whole configJson object")
    private String mode;

    @Schema(description = "selected_module entries that resolved to an existing ai_model_config row")
    private List<ConfigImportItemDTO> appliedModules = new ArrayList<>();

    @Schema(description = "selected_module entries with no matching ai_model_config row")
    private List<ConfigImportItemDTO> skippedModules = new ArrayList<>();

    @Schema(description = "Per-provider sections whose configJson was merged or replaced")
    private List<ConfigImportItemDTO> appliedFields = new ArrayList<>();

    @Schema(description = "Allow-listed server.* sys_params that were updated")
    private List<String> appliedServerParams = new ArrayList<>();

    @Schema(description = "Non-fatal warnings (unknown keys, empty sections, etc.)")
    private List<String> validationErrors = new ArrayList<>();

    @Schema(description = "True when the default ai_agent_template row's *_model_id columns were updated")
    private boolean agentTemplateUpdated;
}
