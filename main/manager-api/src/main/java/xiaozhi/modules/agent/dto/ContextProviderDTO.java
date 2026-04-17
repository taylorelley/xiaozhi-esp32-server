package xiaozhi.modules.agent.dto;

import java.io.Serializable;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "contextsourceconfigurationDTO")
public class ContextProviderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "URLAddress")
    private String url;

    @Schema(description = "requestheader")
    private Map<String, Object> headers;
}
