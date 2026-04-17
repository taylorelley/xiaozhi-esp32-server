package xiaozhi.modules.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * queryalldevice DTO
 * 
 * @author zjy
 * @since 2025-3-21
 */
@Data
@Schema(description = "queryalldevice DTO")
public class DevicePageUserDTO {

    @Schema(description = "devicekeyword")
    private String keywords;

    @Schema(description = "page count")
    @Min(value = 0, message = "{page.number}")
    private String page;

    @Schema(description = "column count")
    @Min(value = 0, message = "{limit.number}")
    private String limit;
}
