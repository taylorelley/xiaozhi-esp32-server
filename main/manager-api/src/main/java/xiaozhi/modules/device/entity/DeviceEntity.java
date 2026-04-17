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
@TableName("ai_device")
@Schema(description = "Device information")
public class DeviceEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "associatedUser ID")
    private Long userId;

    @Schema(description = "MACAddress")
    private String macAddress;

    @Schema(description = "lastconnectiontime")
    private Date lastConnectedAt;

    @Schema(description = "automaticupdate开关(0close/1enable)")
    private Integer autoUpdate;

    @Schema(description = "device硬item型number")
    private String board;

    @Schema(description = "device别名")
    private String alias;

    @Schema(description = "Agent ID")
    private String agentId;

    @Schema(description = "firmwareversionnumber")
    private String appVersion;

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