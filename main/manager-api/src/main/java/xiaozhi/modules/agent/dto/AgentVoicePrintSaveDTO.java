package xiaozhi.modules.agent.dto;

import lombok.Data;

/**
 * saveagentvoiceprint dto
 *
 * @author zjy
 */
@Data
public class AgentVoicePrintSaveDTO {
    /**
     * associated agentid
     */
    private String agentId;
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
