package xiaozhi.modules.security.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * logintable单
 */
@Data
@Schema(description = "logintable单")
public class LoginDTO implements Serializable {

    @Schema(description = "Mobile phone number")
    @NotBlank(message = "{sysuser.username.require}")
    private String username;

    @Schema(description = "Password")
    @NotBlank(message = "{sysuser.password.require}")
    private String password;

    @Schema(description = "mobileVerification code")
    private String mobileCaptcha;

    @Schema(description = "unique identifier")
    @NotBlank(message = "{sysuser.uuid.require}")
    private String captchaId;

}