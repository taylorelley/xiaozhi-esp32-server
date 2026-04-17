package xiaozhi.modules.timbre.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * voicetableentityclass
 * 
 * @author zjy
 * @since 2025-3-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_tts_voice")
@Schema(description = "voiceinformation")
public class TimbreEntity {

    @Schema(description = "id")
    private String id;

    @Schema(description = "Language")
    private String languages;

    @Schema(description = "voicename")
    private String name;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "referenceaudiopath")
    private String referenceAudio;

    @Schema(description = "referencetext")
    private String referenceText;

    @Schema(description = "Sort order")
    private long sort;

    @Schema(description = "corresponding TTS modelPrimary key")
    private String ttsModelId;

    @Schema(description = "voicecode")
    private String ttsVoice;

    @Schema(description = "audioplayAddress")
    private String voiceDemo;

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