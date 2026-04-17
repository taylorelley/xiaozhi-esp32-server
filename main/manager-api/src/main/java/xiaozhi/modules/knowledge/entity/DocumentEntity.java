package xiaozhi.modules.knowledge.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * documenttable (Shadow DB for RAGFlow Documents)
 * correspondingtable名: ai_knowledge_document
 */
@Data
@TableName(value = "ai_rag_knowledge_document", autoResultMap = true)
@Schema(description = "Knowledge basedocumenttable")
public class DocumentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "this地唯一ID")
    private String id;

    @Schema(description = "Knowledge baseID (associated ai_rag_dataset.dataset_id)")
    private String datasetId;

    @Schema(description = "RAGFlowdocumentID (remoteID)")
    private String documentId;

    @Schema(description = "documentname")
    private String name;

    @Schema(description = "File size(Bytes)")
    private Long size;

    @Schema(description = "File type(pdf/doc/txtetc.)")
    private String type;

    @Schema(description = "chunk方法")
    private String chunkMethod;

    @Schema(description = "parseconfiguration(JSON String)")
    private String parserConfig;

    @Schema(description = "availablestatus (1: enable/normal, 0: disable/invalid)")
    private String status;

    @Schema(description = "runstatus (UNSTART/RUNNING/CANCEL/DONE/FAIL)")
    private String run;

    @Schema(description = "parse进度 (0.0 ~ 1.0)")
    private Double progress;

    @Schema(description = "缩略图 (Base64 or URL)")
    private String thumbnail;

    @Schema(description = "parse耗时 (unit: seconds)")
    private Double processDuration;

    @Schema(description = "custom metadata (JSON format)")
    private String metaFields;

    @Schema(description = "sourcetype (local, s3, url etc.)")
    private String sourceType;

    @Schema(description = "parseerrorinformation")
    private String error;

    @Schema(description = "chunkcount")
    private Integer chunkCount;

    @Schema(description = "Tokencount")
    private Long tokenCount;

    @Schema(description = "YesNoenable (0:disable 1:enable)")
    private Integer enabled;

    @Schema(description = "Creator")
    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @Schema(description = "updatetime")
    @TableField(fill = FieldFill.UPDATE)
    private Date updatedAt;

    @Schema(description = "最newsynchronoustime")
    private Date lastSyncAt;
}
