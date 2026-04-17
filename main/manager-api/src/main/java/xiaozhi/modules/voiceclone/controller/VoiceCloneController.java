package xiaozhi.modules.voiceclone.controller;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.Result;
import xiaozhi.common.validator.ValidatorUtils;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.voiceclone.dto.VoiceCloneResponseDTO;
import xiaozhi.modules.voiceclone.entity.VoiceCloneEntity;
import xiaozhi.modules.voiceclone.service.VoiceCloneService;

@Tag(name = "Voice resourcemanagement", description = "Voice resource开通relatedinterface")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/voiceClone")
public class VoiceCloneController {

    private final VoiceCloneService voiceCloneService;
    private final RedisUtils redisUtils;

    @GetMapping
    @Operation(summary = "paginationqueryVoice resource")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "currentpage number，from1start", required = true),
            @Parameter(name = Constant.LIMIT, description = "per pagerecordnumber", required = true)
    })
    @RequiresPermissions("sys:role:normal")
    public Result<PageData<VoiceCloneResponseDTO>> page(
            @Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        ValidatorUtils.validateEntity(params);
        UserDetail user = SecurityUser.getUser();
        params.put("userId", user.getId().toString());
        PageData<VoiceCloneResponseDTO> page = voiceCloneService.pageWithNames(params);
        return new Result<PageData<VoiceCloneResponseDTO>>().ok(page);
    }

    @PostMapping("/upload")
    @Operation(summary = "uploadaudioperformVoice clone")
    @Parameters({
            @Parameter(name = "id", description = "Voice clonerecordID", required = true),
            @Parameter(name = "voiceFile", description = "audio file", required = true)
    })
    @RequiresPermissions("sys:role:normal")
    public Result<String> uploadVoice(
            @RequestParam("id") String id,
            @RequestParam("voiceFile") MultipartFile voiceFile) {
        try {
            // verificationfile
            if (voiceFile == null || voiceFile.isEmpty()) {
                return new Result<String>().error(ErrorCode.VOICE_CLONE_AUDIO_EMPTY);
            }

            // verificationFile type
            String contentType = voiceFile.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                return new Result<String>().error(ErrorCode.VOICE_CLONE_NOT_AUDIO_FILE);
            }

            // 加strongverificationfileextension
            String originalFilename = voiceFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!extension.equals(".mp3") && !extension.equals(".wav")) {
                return new Result<String>().error("onlyallowupload.mp3and.wavformat file");
            }

            // verificationFile size (mostlarge10MB)
            if (voiceFile.getSize() > 10 * 1024 * 1024) {
                return new Result<String>().error(ErrorCode.VOICE_CLONE_AUDIO_TOO_LARGE);
            }
            // checkPermission
            checkPermission(id);
            // callservicelayerprocess
            voiceCloneService.uploadVoice(id, voiceFile);

            return new Result<String>();
        } catch (Exception e) {
            return new Result<String>().error(ErrorCode.VOICE_CLONE_UPLOAD_FAILED, e.getMessage());
        }
    }

    @PostMapping("/updateName")
    @Operation(summary = "updateVoice clonename")
    @RequiresPermissions("sys:role:normal")
    public Result<String> updateName(@RequestBody Map<String, String> params) {
        try {
            String id = params.get("id");
            String name = params.get("name");

            if (id == null || id.isEmpty()) {
                return new Result<String>().error(ErrorCode.IDENTIFIER_NOT_NULL);
            }
            if (name == null || name.isEmpty()) {
                return new Result<String>().error(ErrorCode.VOICE_CLONE_NAME_NOT_NULL);
            }
            // checkPermission
            checkPermission(id);

            voiceCloneService.updateName(id, name);
            redisUtils.delete(RedisKeys.getTimbreNameById(id));
            return new Result<String>();
        } catch (Exception e) {
            return new Result<String>().error(ErrorCode.UPDATE_DATA_FAILED, e.getMessage());
        }
    }

    @PostMapping("/audio/{id}")
    @Operation(summary = "getaudiodownloadID")
    @RequiresPermissions("sys:role:normal")
    public Result<String> getAudioId(@PathVariable("id") String id) {
        // checkPermission
        checkPermission(id);
        byte[] audioData = voiceCloneService.getVoiceData(id);
        if (audioData == null) {
            return new Result<String>().error(ErrorCode.VOICE_CLONE_AUDIO_NOT_FOUND);
        }
        String uuid = UUID.randomUUID().toString();
        redisUtils.set(RedisKeys.getVoiceCloneAudioIdKey(uuid), id);
        return new Result<String>().ok(uuid);
    }

    @GetMapping("/play/{uuid}")
    @Operation(summary = "playaudio")
    public void playVoice(@PathVariable("uuid") String uuid, HttpServletResponse response) {
        try {
            String id = (String) redisUtils.get(RedisKeys.getVoiceCloneAudioIdKey(uuid));
            redisUtils.delete(RedisKeys.getVoiceCloneAudioIdKey(uuid));
            if (StringUtils.isBlank(id)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // getaudio data
            byte[] voiceData = voiceCloneService.getVoiceData(id);

            if (voiceData == null || voiceData.length == 0) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // setresponseheader
            response.setContentType("audio/wav");
            response.setContentLength(voiceData.length);
            response.setHeader("Content-Disposition", "inline; filename=voice.wav");

            // write入audio data
            response.getOutputStream().write(voiceData);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("playaudiofailed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cloneAudio")
    @Operation(summary = "复刻audio")
    @RequiresPermissions("sys:role:normal")
    public Result<String> cloneAudio(@RequestBody Map<String, String> params) {
        String cloneId = params.get("cloneId");
        checkPermission(cloneId);
        // callservicelayerperformvoiceclonetraining
        voiceCloneService.cloneAudio(cloneId);
        return new Result<String>();
    }

    private void checkPermission(String id) {
        VoiceCloneEntity voiceClone = voiceCloneService.selectById(id);
        if (voiceClone == null) {
            throw new RenException(ErrorCode.VOICE_CLONE_RECORD_NOT_EXIST);
        }
        if (!voiceClone.getUserId().equals(SecurityUser.getUser().getId())) {
            throw new RenException(ErrorCode.VOICE_RESOURCE_NO_PERMISSION);
        }
    }
}
