package xiaozhi.modules.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "agentTagDTO")
public class AgentTagDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "TagID")
    private String id;

    @Schema(description = "Tagname")
    private String tagName;
}
