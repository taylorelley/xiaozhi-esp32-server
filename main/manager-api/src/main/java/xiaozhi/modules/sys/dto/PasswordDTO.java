package xiaozhi.modules.sys.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * updatePassword
 */
@Data
@Schema(description = "updatePassword")
public class PasswordDTO implements Serializable {

    @Schema(description = "原Password")
    @NotBlank(message = "{sysuser.password.require}")
    private String password;

    @Schema(description = "newPassword")
    @NotBlank(message = "{sysuser.password.require}")
    private String newPassword;

}