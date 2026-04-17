package xiaozhi.modules.agent.vo;

import lombok.Data;

import java.util.Date;

/**
 * 展示agentvoiceprintlistVO
 */
@Data
public class AgentVoicePrintVO {

    /**
     * Primary keyid
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
    /**
     * Create time
     */
    private Date createDate;
}
