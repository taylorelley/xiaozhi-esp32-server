package xiaozhi.modules.config.controller;

import java.io.IOException;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.utils.Result;
import xiaozhi.common.validator.ValidatorUtils;
import xiaozhi.modules.config.dto.AgentModelsDTO;
import xiaozhi.modules.config.dto.ConfigImportResultDTO;
import xiaozhi.modules.config.service.ConfigImportService;
import xiaozhi.modules.config.service.ConfigService;

/**
 * xiaozhi-server configuration APIs.
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("config")
@Tag(name = "Parameter management")
@AllArgsConstructor
public class ConfigController {
    /**
     * Max size for an uploaded config YAML. The wizard's output is small
     * (typically a few KB) so 256 KB is comfortably above the largest
     * plausible real payload while protecting against accidental or
     * malicious huge uploads.
     */
    private static final long MAX_UPLOAD_BYTES = 256L * 1024L;

    private final ConfigService configService;
    private final ConfigImportService configImportService;

    @PostMapping("server-base")
    @Operation(summary = "serviceendgetconfigurationinterface")
    public Result<Object> getConfig() {
        Object config = configService.getConfig(true);
        return new Result<Object>().ok(config);
    }

    @PostMapping("agent-models")
    @Operation(summary = "getagentmodel")
    public Result<Object> getAgentModels(@Valid @RequestBody AgentModelsDTO dto) {
        // validatedata
        ValidatorUtils.validateEntity(dto);
        Object models = configService.getAgentModels(dto.getMacAddress(), dto.getSelectedModule());
        return new Result<Object>().ok(models);
    }

    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import a wizard-generated server config YAML into the running manager-api DB")
    @LogOperation("import server config")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<ConfigImportResultDTO> uploadConfig(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun,
            @RequestParam(value = "mode", defaultValue = "merge") String mode) {
        if (file == null || file.isEmpty()) {
            throw new RenException(ErrorCode.UPLOAD_FILE_EMPTY);
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new RenException(ErrorCode.PARAM_JSON_INVALID);
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            if (!lower.endsWith(".yaml") && !lower.endsWith(".yml")) {
                throw new RenException(ErrorCode.PARAM_JSON_INVALID);
            }
        }
        try {
            ConfigImportResultDTO result = configImportService.importYaml(file.getBytes(), dryRun, mode);
            return new Result<ConfigImportResultDTO>().ok(result);
        } catch (IOException e) {
            throw new RenException(ErrorCode.UPLOAD_FILE_EMPTY);
        }
    }
}
