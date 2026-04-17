package xiaozhi.modules.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "user显示Device listVO")
public class UserShowDeviceListVO {

    @Schema(description = "appversion")
    private String appVersion;

    @Schema(description = "bindUsernamename")
    private String bindUserName;

    @Schema(description = "device型number")
    private String deviceType;

    @Schema(description = "deviceunique identifier符")
    private String id;

    @Schema(description = "macAddress")
    private String macAddress;

    @Schema(description = "enableOTA")
    private Integer otaUpgrade;

    @Schema(description = "most近conversationtime")
    private String recentChatTime;

}