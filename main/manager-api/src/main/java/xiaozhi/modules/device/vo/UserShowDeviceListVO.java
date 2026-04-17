package xiaozhi.modules.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "userdisplayDevice listVO")
public class UserShowDeviceListVO {

    @Schema(description = "appversion")
    private String appVersion;

    @Schema(description = "bindUsernamename")
    private String bindUserName;

    @Schema(description = "devicetypenumber")
    private String deviceType;

    @Schema(description = "deviceunique identifiersymbol")
    private String id;

    @Schema(description = "macAddress")
    private String macAddress;

    @Schema(description = "enableOTA")
    private Integer otaUpgrade;

    @Schema(description = "mostrecentconversationtime")
    private String recentChatTime;

}