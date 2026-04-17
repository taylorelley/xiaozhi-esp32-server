package xiaozhi.modules.knowledge.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@Schema(description = "Knowledge basedocument")
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeFilesDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "unique identifier")
    private String id;

    @Schema(description = "documentID")
    private String documentId;

    @Schema(description = "Knowledge baseID")
    private String datasetId;

    @Schema(description = "documentname")
    private String name;

    @Schema(description = "documenttype")
    private String fileType;

    @Schema(description = "File size（byte）")
    private Long fileSize;

    @Schema(description = "filepath")
    private String filePath;

    @Schema(description = "parseprogress (0.0 ~ 1.0)")
    private Double progress;

    @Schema(description = "缩略图 (Base64 or URL)")
    private String thumbnail;

    @Schema(description = "parse耗when (unit: seconds)")
    private Double processDuration;

    @Schema(description = "sourcetype (local, s3, url etc.)")
    private String sourceType;

    @Schema(description = "datafield (Map format)")
    private Map<String, Object> metaFields;

    @Schema(description = "chunkmethod")
    private String chunkMethod;

    @Schema(description = "parserconfiguration")
    private Map<String, Object> parserConfig;

    @Schema(description = "availablestatus (1: enable/normal, 0: disable/invalid)")
    private String status;

    @Schema(description = "runstatus (UNSTART/RUNNING/CANCEL/DONE/FAIL)")
    private String run;

    @Schema(description = "Creator")
    private Long creator;

    @Schema(description = "Create time")
    private Date createdAt;

    @Schema(description = "update")
    private Long updater;

    @Schema(description = "updatetime")
    private Date updatedAt;

    @Schema(description = "chunkcount")
    private Integer chunkCount;

    @Schema(description = "Tokencount")
    private Long tokenCount;

    @Schema(description = "parseerrorinformation")
    private String error;

    // Document parsingstatus常量define
    private static final Integer STATUS_UNSTART = 0;
    private static final Integer STATUS_RUNNING = 1;
    private static final Integer STATUS_CANCEL = 2;
    private static final Integer STATUS_DONE = 3;
    private static final Integer STATUS_FAIL = 4;

    /**
     * getDocument parsingstatuscode（基于runfieldconvert）
     */
    public Integer getParseStatusCode() {
        if (run == null) {
            return STATUS_UNSTART;
        }

        // RAGFlowaccording torunfield valuedirectlymappingtocorresponding statuscode
        switch (run.toUpperCase()) {
            case "RUNNING":
                return STATUS_RUNNING;
            case "CANCEL":
                return STATUS_CANCEL;
            case "DONE":
                return STATUS_DONE;
            case "FAIL":
                return STATUS_FAIL;
            case "UNSTART":
            default:
                return STATUS_UNSTART;
        }
    }

}