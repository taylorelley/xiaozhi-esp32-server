package xiaozhi.modules.model.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("ai_model_provider")
@Schema(description = "Model providertable")
public class ModelProviderEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "Primary key")
    private String id;

    @Schema(description = "Model type(Memory/ASR/VAD/LLM/TTS)")
    private String modelType;

    @Schema(description = "providertype，e.g. openai、")
    private String providerCode;

    @Schema(description = "providername")
    private String name;

    @Schema(description = "providerfieldlist(JSONformat)")
    private String fields;

    @Schema(description = "Sort order")
    private Integer sort;

    @Schema(description = "Creator")
    private Long creator;

    @Schema(description = "Create time")
    private Date createDate;

    @Schema(description = "update")
    private Long updater;

    @Schema(description = "updatetime")
    private Date updateDate;
}
