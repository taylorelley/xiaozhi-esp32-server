package xiaozhi.modules.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xiaozhi.modules.sys.enums.ServerActionEnum;

/**
 * sendpythonserviceendoperationDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmitSeverActionDTO
{
    @Schema(description = "targetwsAddress")
    @NotEmpty(message = "targetwsAddresscannot be empty")
    private String targetWs;

    @Schema(description = "specifiedoperation")
    @NotNull(message = "operationcannot be empty")
    private ServerActionEnum action;
}
