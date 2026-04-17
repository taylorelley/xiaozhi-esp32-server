package xiaozhi.modules.security.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * SMSVerification coderequestDTO
 */
@Data
@Schema(description = "SMSVerification coderequest")
public class SmsVerificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Mobile phone number")
    @NotBlank(message = "{sysuser.username.require}")
    private String phone;

    @Schema(description = "Verification code")
    @NotBlank(message = "{sysuser.captcha.require}")
    private String captcha;

    @Schema(description = "unique identifier")
    @NotBlank(message = "{sysuser.uuid.require}")
    private String captchaId;
}