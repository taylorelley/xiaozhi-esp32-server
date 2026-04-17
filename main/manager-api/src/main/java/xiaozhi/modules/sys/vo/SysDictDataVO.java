package xiaozhi.modules.sys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Dictionary dataVO
 */
@Data
@Schema(description = "Dictionary dataVO")
public class SysDictDataVO implements Serializable {
    @Schema(description = "Primary key")
    private Long id;

    @Schema(description = "Dictionary type ID")
    private Long dictTypeId;

    @Schema(description = "Dictionary label")
    private String dictLabel;

    @Schema(description = "Dictionary value")
    private String dictValue;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Sort order")
    private Integer sort;

    @Schema(description = "Creator")
    private Long creator;

    @Schema(description = "Creatorname")
    private String creatorName;

    @Schema(description = "Create time")
    private Date createDate;

    @Schema(description = "update")
    private Long updater;

    @Schema(description = "updatename")
    private String updaterName;

    @Schema(description = "updatetime")
    private Date updateDate;
}
