package xiaozhi.modules.agent.dto;

import lombok.Data;

/**
 * updateagentvoiceprint dto
 *
 * @author zjy
 */
@Data
public class AgentVoicePrintUpdateDTO {
    /**
     * agentvoiceprintid
     */
    private String id;
    /**
     * audio fileid
     */
    private String audioId;
    /**
     * voiceprintsource personName
     */
    private String sourceName;
    /**
     * Descriptionvoiceprintsource person
     */
    private String introduce;
}
