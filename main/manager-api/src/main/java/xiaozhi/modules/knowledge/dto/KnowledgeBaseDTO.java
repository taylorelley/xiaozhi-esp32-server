package xiaozhi.modules.knowledge.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Knowledge baseKnowledge base")
public class KnowledgeBaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "unique identifier")
    private String id;

    @Schema(description = "Knowledge baseID")
    private String datasetId;

    @Schema(description = "RAGModel configurationID")
    private String ragModelId;

    @Schema(description = "Knowledge base name")
    private String name;

    @Schema(description = "Knowledge baseavatar(Base64)")
    private String avatar;

    @Schema(description = "Knowledge baseDescription")
    private String description;

    @Schema(description = "embeddingModel name")
    private String embeddingModel;

    @Schema(description = "Permissionset: me/team")
    private String permission;

    @Schema(description = "chunkmethod")
    private String chunkMethod;

    @Schema(description = "parserconfiguration(JSON String)")
    private String parserConfig;

    @Schema(description = "chunktotal")
    private Long chunkCount;

    @Schema(description = "totalTokennumber")
    private Long tokenNum;

    @Schema(description = "status(0:disable 1:enable)")
    private Integer status;

    @Schema(description = "Creator")
    private Long creator;

    @Schema(description = "Create time")
    private Date createdAt;

    @Schema(description = "update")
    private Long updater;

    @Schema(description = "updatetime")
    private Date updatedAt;

    @Schema(description = "documentcount")
    private Integer documentCount;
}