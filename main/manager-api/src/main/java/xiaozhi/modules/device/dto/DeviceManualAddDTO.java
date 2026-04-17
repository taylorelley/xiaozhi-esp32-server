package xiaozhi.modules.device.dto;

import lombok.Data;

@Data
public class DeviceManualAddDTO {
    private String agentId;
    private String board;        // devicetypenumber
    private String appVersion;   // firmwareversion
    private String macAddress;   // MacAddress
} 