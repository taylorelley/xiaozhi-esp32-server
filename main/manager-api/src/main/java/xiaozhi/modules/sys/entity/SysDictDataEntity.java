package xiaozhi.modules.sys.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

/**
 * dataDictionary
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_dict_data")
public class SysDictDataEntity extends BaseEntity {
    /**
     * Dictionary type ID
     */
    private Long dictTypeId;
    /**
     * Dictionary label
     */
    private String dictLabel;
    /**
     * Dictionary value
     */
    private String dictValue;
    /**
     * Remark
     */
    private String remark;
    /**
     * Sort order
     */
    private Integer sort;
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