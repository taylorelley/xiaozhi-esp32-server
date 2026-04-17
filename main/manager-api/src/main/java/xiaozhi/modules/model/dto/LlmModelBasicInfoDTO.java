package xiaozhi.modules.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LLM model base展示data
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LlmModelBasicInfoDTO extends ModelBasicInfoDTO{
    private String type;
}