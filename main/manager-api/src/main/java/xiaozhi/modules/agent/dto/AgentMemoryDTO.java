package xiaozhi.modules.agent.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Agent memoryupdateDTO
 */
@Data
@Schema(description = "Agent memoryupdateobject")
public class AgentMemoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "summary memory", example = "Build a dynamic memory network that can grow，Retain key information in limited spaceinformation simultaneously，Intelligently maintaininformationevolution trajectory\n" +
            "according toconversationrecord，summaryuser re-need toinformation，so that in futureconversation to provide more personalized service", required = false)
    private String summaryMemory;
}