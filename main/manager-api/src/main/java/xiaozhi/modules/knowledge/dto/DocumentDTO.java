package xiaozhi.modules.knowledge.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * document DTO
 */
@Data
@Schema(description = "Knowledge basedocument")
public class DocumentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "thisID")
    private String id;

    @Schema(description = "Knowledge baseID")
    private String datasetId;

    @Schema(description = "RAGFlowdocumentID")
    private String documentId;

    @Schema(description = "documentname")
    private String name;

    @Schema(description = "File size")
    private Long size;

    @Schema(description = "File type")
    private String type;

    @Schema(description = "chunkmethod")
    private String chunkMethod;

    @Schema(description = "parseconfiguration")
    private Map<String, Object> parserConfig;

    @Schema(description = "processstatus (1:parse 3:success 4:failed)")
    private Integer status;

    @Schema(description = "errorinformation")
    private String error;

    @Schema(description = "chunkcount")
    private Integer chunkCount;

    @Schema(description = "Tokencount")
    private Long tokenCount;

    @Schema(description = "YesNoenable")
    private Integer enabled;

    @Schema(description = "Create time")
    private Date createdAt;

    @Schema(description = "updatetime")
    private Date updatedAt;

    @Schema(description = "uploadprogress (虚拟field)")
    private Double progress;

    @Schema(description = "缩略图/预览图 (虚拟field)")
    private String thumbnail;
}
