package xiaozhi.modules.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * LittleWisedevicechat reportingrequest
 *
 * @author Haotian
 * @version 1.0, 2025/5/8
 */
@Data
@Schema(description = "LittleWisedevicechat reportingrequest")
public class AgentChatHistoryReportDTO {
    @Schema(description = "MACAddress", example = "00:11:22:33:44:55")
    @NotBlank
    private String macAddress;
    @Schema(description = "Session ID", example = "79578c31-f1fb-426a-900e-1e934215f05a")
    @NotBlank
    private String sessionId;
    @Schema(description = "messagetype: 1-user, 2-agent", example = "1")
    @NotNull
    private Byte chatType;
    @Schema(description = "chatcontent", example = "Hello呀")
    @NotBlank
    private String content;
    @Schema(description = "base64code opusaudio data", example = "")
    private String audioBase64;
    @Schema(description = "上报time，十bittimestamp，emptywhendefaultusecurrenttime", example = "1745657732")
    private Long reportTime;
}
