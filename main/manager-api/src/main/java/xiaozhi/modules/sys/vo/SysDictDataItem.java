package xiaozhi.modules.sys.vo;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Dictionary dataVO
 */
@Data
@Schema(description = "Dictionary dataitem")
public class SysDictDataItem implements Serializable {

    @Schema(description = "Dictionary label")
    private String name;

    @Schema(description = "Dictionary value")
    private String key;
}
