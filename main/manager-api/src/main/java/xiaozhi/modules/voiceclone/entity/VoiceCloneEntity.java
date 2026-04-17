package xiaozhi.modules.voiceclone.entity;

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
@TableName("ai_voice_clone")
@Schema(description = "Voice clone")
public class VoiceCloneEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "unique identifier")
    private String id;

    @Schema(description = "voicename")
    private String name;

    @Schema(description = "modelid")
    private String modelId;

    @Schema(description = "voiceid")
    private String voiceId;

    @Schema(description = "Language")
    private String languages;

    @Schema(description = "user ID（associatedusertable）")
    private Long userId;

    @Schema(description = "voice")
    private byte[] voice;

    @Schema(description = "trainingstatus：0待training 1training 2trainingsuccess 3trainingfailed")
    private Integer trainStatus;

    @Schema(description = "trainingerrorreason")
    private String trainError;

    @Schema(description = "Creator")
    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @Schema(description = "Create time")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
}
