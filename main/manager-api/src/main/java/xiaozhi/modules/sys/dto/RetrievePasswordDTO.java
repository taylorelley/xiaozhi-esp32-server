package xiaozhi.modules.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * find回PasswordDTO
 */
@Data
@Schema(description = "find回Password")
public class RetrievePasswordDTO implements Serializable {

    @Schema(description = "Mobile phone number")
    @NotBlank(message = "{sysuser.password.require}")
    private String phone;

    @Schema(description = "Verification code")
    @NotBlank(message = "{sysuser.password.require}")
    private String code;

    @Schema(description = "newPassword")
    @NotBlank(message = "{sysuser.password.require}")
    private String password;

    @Schema(description = "图形Verification codeID")
    @NotBlank(message = "{sysuser.uuid.require}")
    private String captchaId;



}