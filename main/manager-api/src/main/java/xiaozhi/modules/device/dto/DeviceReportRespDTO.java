package xiaozhi.modules.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Schema(description = "deviceOTAdetectversionreturn，containActivation codeneed to求")
public class DeviceReportRespDTO {
    @Schema(description = "servicetime")
    private ServerTime server_time;

    @Schema(description = "Activation code")
    private Activation activation;

    @Schema(description = "errorinformation")
    private String error;

    @Schema(description = "firmwareversioninformation")
    private Firmware firmware;

    @Schema(description = "WebSocketconfiguration")
    private Websocket websocket;

    @Schema(description = "MQTT Gatewayconfiguration")
    private MQTT mqtt;

    @Getter
    @Setter
    public static class Firmware {
        @Schema(description = "versionnumber")
        private String version;
        @Schema(description = "downloadAddress")
        private String url;
    }

    public static DeviceReportRespDTO createError(String message) {
        DeviceReportRespDTO resp = new DeviceReportRespDTO();
        resp.setError(message);
        return resp;
    }

    @Setter
    @Getter
    public static class Activation {
        @Schema(description = "Activation code")
        private String code;

        @Schema(description = "Activation codeinformation: activationAddress")
        private String message;

        @Schema(description = "挑战code")
        private String challenge;
    }

    @Getter
    @Setter
    public static class ServerTime {
        @Schema(description = "timestamp")
        private Long timestamp;

        @Schema(description = "时区")
        private String timeZone;

        @Schema(description = "时区偏移量，unitas分钟")
        private Integer timezone_offset;
    }

    @Getter
    @Setter
    public static class Websocket {
        @Schema(description = "WebSocketserviceAddress")
        private String url;
        @Schema(description = "WebSocket authentication token")
        private String token;
    }

    @Getter
    @Setter
    public static class MQTT {
        @Schema(description = "MQTT configuration网址")
        private String endpoint;
        @Schema(description = "MQTT clientunique identifier符")
        private String client_id;
        @Schema(description = "MQTT authenticationUsername")
        private String username;
        @Schema(description = "MQTT authenticationPassword")
        private String password;
        @Schema(description = "ESP32 发布message 主题")
        private String publish_topic;
        @Schema(description = "ESP32 订阅 主题")
        private String subscribe_topic;
    }
}