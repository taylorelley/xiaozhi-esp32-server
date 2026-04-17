package xiaozhi.modules.agent.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * agentcreateDTO
 * 专used foraddagent，not containid、agentCodeandsortfield，thesefieldbysystemautomaticgenerate/setdefaultvalue
 */
@Data
@Schema(description = "agentcreateobject")
public class AgentCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Agent name", example = "customer serviceassistant")
    @NotBlank(message = "Agent namecannot be empty")
    private String agentName;
}