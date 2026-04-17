package xiaozhi.modules.device.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_ota")
@Schema(description = "firmwareinformation")
public class OtaEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "firmwarename")
    private String firmwareName;

    @Schema(description = "firmwaretype")
    private String type;

    @Schema(description = "versionnumber")
    private String version;

    @Schema(description = "File size(byte)")
    private Long size;

    @Schema(description = "Remark/Description")
    private String remark;

    @Schema(description = "firmwarepath")
    private String firmwarePath;

    @Schema(description = "Sort order")
    private Integer sort;

    @Schema(description = "update")
    @TableField(fill = FieldFill.UPDATE)
    private Long updater;

    @Schema(description = "updatetime")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateDate;

    @Schema(description = "Creator")
    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
}