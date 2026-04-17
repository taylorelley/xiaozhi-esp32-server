package xiaozhi.common.page;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 令牌information
 *
 * @author Jack
 */
@Data
@Schema(description = "令牌information")
public class TokenDTO implements Serializable {

    @Schema(description = "Password")
    private String token;

    @Schema(description = "Expiration time")
    private int expire;

    @Schema(description = "client指纹")
    private String clientHash;
}
