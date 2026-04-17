package xiaozhi.modules.sys.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

/**
 * Parameter management
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_params")
public class SysParamsEntity extends BaseEntity {
    /**
     * Parameter code
     */
    private String paramCode;
    /**
     * Parameter value
     */
    private String paramValue;
    /**
     * valuetype：string-string，number-number，boolean-，array-array
     */
    private String valueType;
    /**
     * type 0：systemparameter 1：non-systemparameter
     */
    private Integer paramType;
    /**
     * Remark
     */
    private String remark;
    /**
     * update
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    /**
     * updatetime
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

}