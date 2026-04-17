package xiaozhi.modules.device.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * deviceregisterheaderinformation
 * 
 * @author zjy
 * @since 2025-3-28
 */
@Setter
@Getter
@Schema(description = "deviceregisterheaderinformation")
public class DeviceRegisterDTO implements Serializable {

    @Schema(description = "macAddress")
    private String macAddress;

}