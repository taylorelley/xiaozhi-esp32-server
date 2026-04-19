package xiaozhi.modules.config.service;

import xiaozhi.modules.config.dto.ConfigImportResultDTO;

/**
 * Applies a YAML config blob (as produced by the xiaozhi-esp32 setup wizard)
 * to the manager-api database. Updates the default AgentTemplate's selected
 * model ids, merges per-provider settings into ai_model_config.config_json,
 * and pushes an allow-listed subset of server.* entries into sys_params.
 */
public interface ConfigImportService {

    String MODE_MERGE = "merge";
    String MODE_REPLACE = "replace";

    /**
     * Parse and apply the given YAML.
     *
     * @param yamlBytes raw YAML bytes uploaded by the operator
     * @param dryRun    when true, the method walks all steps but does not
     *                  persist any changes; the returned DTO still reflects
     *                  what would have been applied
     * @param mode      {@link #MODE_MERGE} (default) overlays new keys into an
     *                  existing configJson; {@link #MODE_REPLACE} overwrites
     *                  the configJson object entirely
     * @return a structured summary of applied / skipped items
     */
    ConfigImportResultDTO importYaml(byte[] yamlBytes, boolean dryRun, String mode);
}
