package xiaozhi.common.page;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * tokeninformation
 *
 * @author Jack
 */
@Data
@Schema(description = "tokeninformation")
public class TokenDTO implements Serializable {

    @Schema(description = "Password")
    private String token;

    @Schema(description = "Expiration time")
    private int expire;

    @Schema(description = "clientfingerprint")
    private String clientHash;
}
