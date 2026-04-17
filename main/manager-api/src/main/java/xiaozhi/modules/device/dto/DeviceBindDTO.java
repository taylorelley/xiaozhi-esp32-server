package xiaozhi.modules.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * devicebind DTO
 * 
 * @author zjy
 * @since 2025-3-28
 */
@Data
@AllArgsConstructor
@Schema(description = "deviceconnectionheaderinformation")
public class DeviceBindDTO {

    @Schema(description = "macAddress")
    private String macAddress;

    @Schema(description = "belonging touserid")
    private Long userId;

    @Schema(description = "agentid")
    private String agentId;

}