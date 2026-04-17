package xiaozhi.modules.config.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "getagentModel configurationDTO")
public class AgentModelsDTO {

    @NotBlank(message = "deviceMACAddresscannot be empty")
    @Schema(description = "deviceMACAddress")
    private String macAddress;

    @NotBlank(message = "clientIDcannot be empty")
    @Schema(description = "clientID")
    private String clientId;

    @NotNull(message = "clientalreadyexample modelcannot be empty")
    @Schema(description = "clientalreadyexample model")
    private Map<String, String> selectedModule;
}