package xiaozhi.modules.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * administratorpaginationuser parameterDTO
 * 
 * @author zjy
 * @since 2025-3-21
 */
@Data
@Schema(description = "administratorpaginationuser parameterDTO")
public class AdminPageUserDTO {

    @Schema(description = "Mobile phone number")
    private String mobile;

    @Schema(description = "page count")
    @Min(value = 0, message = "{sort.number}")
    private String page;

    @Schema(description = "column count")
    @Min(value = 0, message = "{sort.number}")
    private String limit;
}
