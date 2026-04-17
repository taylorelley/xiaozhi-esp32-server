package xiaozhi.modules.knowledge.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName(value = "ai_rag_dataset", autoResultMap = true)
@Schema(description = "Knowledge baseKnowledge basetable")
public class KnowledgeBaseEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "unique identifier")
    private String id;

    @Schema(description = "Knowledge baseID")
    private String datasetId;

//    @Deprecated
    @Schema(description = "RAGModel configurationID (connectionRAGFlow 凭证指针)")
    private String ragModelId;

    @Schema(description = "tenantID")
    private String tenantId;

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

    @Schema(description = "chunk方法")
    private String chunkMethod;

    @Schema(description = "parserconfiguration(JSON String)")
    private String parserConfig;

    @Schema(description = "chunktotal")
    private Long chunkCount;

    @Schema(description = "documenttotal")
    private Long documentCount;

    @Schema(description = "totalTokennumber")
    private Long tokenNum;

    @Schema(description = "status(0:disable 1:enable)")
    private Integer status;

    @Schema(description = "Creator")
    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @Schema(description = "update")
    @TableField(fill = FieldFill.UPDATE)
    private Long updater;

    @Schema(description = "updatetime")
    @TableField(fill = FieldFill.UPDATE)
    private Date updatedAt;
}