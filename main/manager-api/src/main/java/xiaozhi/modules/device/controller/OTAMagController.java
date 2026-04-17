package xiaozhi.modules.device.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.utils.Result;
import xiaozhi.common.validator.ValidatorUtils;
import xiaozhi.modules.device.entity.OtaEntity;
import xiaozhi.modules.device.service.OtaService;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.enums.SuperAdminEnum;
import xiaozhi.modules.sys.service.SysParamsService;

@Tag(name = "firmwareupgrademanagement", description = "OTA relatedinterface")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/otaMag")
public class OTAMagController {
    private static final Logger logger = LoggerFactory.getLogger(OTAController.class);
    private final OtaService otaService;
    private final RedisUtils redisUtils;
    private final SysParamsService sysParamsService;

    @GetMapping
    @Operation(summary = "paginationquery OTA firmwareinformation")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "currentpage number，from1start", required = true),
            @Parameter(name = Constant.LIMIT, description = "per pagerecordnumber", required = true)
    })
    @RequiresPermissions("sys:role:superAdmin")
    public Result<PageData<OtaEntity>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        ValidatorUtils.validateEntity(params);
        PageData<OtaEntity> page = otaService.page(params);
        return new Result<PageData<OtaEntity>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "information OTA firmwareinformation")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<OtaEntity> get(@PathVariable("id") String id) {
        OtaEntity data = otaService.selectById(id);
        return new Result<OtaEntity>().ok(data);
    }

    @PostMapping
    @Operation(summary = "save OTA firmwareinformation")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<Void> save(@RequestBody OtaEntity entity) {
        if (entity == null) {
            return new Result<Void>().error("firmwareinformationcannot be empty");
        }
        if (StringUtils.isBlank(entity.getFirmwareName())) {
            return new Result<Void>().error("firmwarenamecannot be empty");
        }
        if (StringUtils.isBlank(entity.getType())) {
            return new Result<Void>().error("firmwaretypecannot be empty");
        }
        if (StringUtils.isBlank(entity.getVersion())) {
            return new Result<Void>().error("versionnumbercannot be empty");
        }
        try {
            otaService.save(entity);
            return new Result<Void>();
        } catch (RuntimeException e) {
            return new Result<Void>().error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "OTA delete")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<Void> delete(@PathVariable("id") String[] ids) {
        if (ids == null || ids.length == 0) {
            return new Result<Void>().error("delete firmwareIDcannot be empty");
        }
        otaService.delete(ids);
        return new Result<Void>();
    }

    @PutMapping("/{id}")
    @Operation(summary = "update OTA firmwareinformation")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<?> update(@PathVariable("id") String id, @RequestBody OtaEntity entity) {
        if (entity == null) {
            return new Result<>().error("firmwareinformationcannot be empty");
        }
        entity.setId(id);
        try {
            otaService.update(entity);
            return new Result<>();
        } catch (RuntimeException e) {
            return new Result<>().error(e.getMessage());
        }
    }

    @GetMapping("/getDownloadUrl/{id}")
    @Operation(summary = "get OTA firmwaredownloadlink")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<String> getDownloadUrl(@PathVariable("id") String id) {
        String uuid = UUID.randomUUID().toString();
        redisUtils.set(RedisKeys.getOtaIdKey(uuid), id);
        return new Result<String>().ok(uuid);
    }

    @GetMapping("/download/{uuid}")
    @Operation(summary = "downloadfirmwarefile")
    public ResponseEntity<byte[]> downloadFirmware(@PathVariable("uuid") String uuid) {
        String id = (String) redisUtils.get(RedisKeys.getOtaIdKey(uuid));
        if (StringUtils.isBlank(id)) {
            return ResponseEntity.notFound().build();
        }

        // checkdownloadtimes
        String downloadCountKey = RedisKeys.getOtaDownloadCountKey(uuid);
        Integer downloadCount = (Integer) Optional.ofNullable(redisUtils.get(downloadCountKey)).orElse(0);

        // ifdownloadtimesexceed3times，return404
        if (downloadCount >= 3) {
            redisUtils.delete(List.of(downloadCountKey, RedisKeys.getOtaIdKey(uuid)));
            logger.warn("Download limit exceeded for UUID: {}", uuid);
            return ResponseEntity.notFound().build();
        }

        redisUtils.set(downloadCountKey, downloadCount + 1);

        try {
            // getfirmwareinformation
            OtaEntity otaEntity = null;
            if (id.indexOf("file:") == 0) {
                id = id.substring(5);
                otaEntity = new OtaEntity();
                otaEntity.setFirmwarePath(id);
                otaEntity.setType("assets");
                otaEntity.setVersion("1.0.0");
            } else {
                otaEntity = otaService.selectById(id);
            }

            if (otaEntity == null || StringUtils.isBlank(otaEntity.getFirmwarePath())) {
                logger.warn("Firmware not found or path is empty for ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            // getfilepath - ensurepathYesabsolutelyforpathorexact forpath
            String firmwarePath = otaEntity.getFirmwarePath();
            String originalFilename = otaEntity.getType() + "_" + otaEntity.getVersion();
            Path path;

            // checkYesNoYesabsolutelyforpath
            if (Paths.get(firmwarePath).isAbsolute()) {
                path = Paths.get(firmwarePath);
            } else {
                // ifYesforpath，thenfromcurrentworkdirectoryparse
                path = Paths.get(System.getProperty("user.dir"), firmwarePath);
            }

            logger.info("Attempting to download firmware for ID: {}, DB path: {}, resolved path: {}",
                    id, firmwarePath, path.toAbsolutePath());

            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                // trydirectlyfromfirmwaredirectorybelowfindFile name
                String fileName = new File(firmwarePath).getName();
                Path altPath = Paths.get(System.getProperty("user.dir"), "firmware", fileName);

                logger.info("File not found at primary path, trying alternative path: {}", altPath.toAbsolutePath());

                if (Files.exists(altPath) && Files.isRegularFile(altPath)) {
                    path = altPath;
                } else {
                    logger.error("Firmware file not found at either path: {} or {}",
                            path.toAbsolutePath(), altPath.toAbsolutePath());
                    return ResponseEntity.notFound().build();
                }
            }

            // readgetFile content
            byte[] fileContent = Files.readAllBytes(path);

            // setresponseheader

            if (firmwarePath.contains(".")) {
                String extension = firmwarePath.substring(firmwarePath.lastIndexOf("."));
                originalFilename += extension;
            }

            // clean upFile name，removenot safeallcharactersymbol
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

            logger.info("Providing download for firmware ID: {}, filename: {}, size: {} bytes",
                    id, safeFilename, fileContent.length);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFilename + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            logger.error("Error reading firmware file for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Unexpected error during firmware download for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    @Operation(summary = "uploadfirmwarefile")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<String> uploadFirmware(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new Result<String>().error("uploadfilecannot be empty");
        }

        // checkfileextension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return new Result<String>().error("File namecannot be empty");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!extension.equals(".bin") && !extension.equals(".apk")) {
            return new Result<String>().error("onlyallowupload.binand.apkformat file");
        }

        try {
            // calculatefile MD5value
            String md5 = calculateMD5(file);

            // setstorestorepath
            String uploadDir = "uploadfile";
            Path uploadPath = Paths.get(uploadDir);

            // ifdirectorydoes not exist，createdirectory
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // useMD5asFile name，fixeduse.binextension
            String uniqueFileName = md5 + extension;
            Path filePath = uploadPath.resolve(uniqueFileName);

            // checkfileYesNoalready exists
            if (Files.exists(filePath)) {
                return new Result<String>().ok(filePath.toString());
            }

            // savefile
            Files.copy(file.getInputStream(), filePath);

            // returnfilepath
            return new Result<String>().ok(filePath.toString());
        } catch (IOException | NoSuchAlgorithmException e) {
            return new Result<String>().error("fileuploadfailed：" + e.getMessage());
        }
    }

    @PostMapping("/uploadAssetsBin")
    @Operation(summary = "uploadresourcesourcefirmwarefile")
    @RequiresPermissions("sys:role:normal")
    public Result<String> uploadAssetsBin(@RequestParam("file") MultipartFile file) {
        String otaUrl = sysParamsService.getValue(Constant.SERVER_OTA, true);
        if (StringUtils.isBlank(otaUrl) || otaUrl.equals("null")) {
            return new Result<String>().error(ErrorCode.OTA_URL_EMPTY);
        }
        logger.info("username:{},uploadAssetsBin size: {}", SecurityUser.getUser().getUsername(), file.getSize());
        // verificationFile size (resourcesourcefirmwaremostlarge20MB)
        if (file.getSize() > 20 * 1024 * 1024) {
            return new Result<String>().error(ErrorCode.VOICE_CLONE_AUDIO_TOO_LARGE);
        }
        // Normal useronlycaneverydayupload50times
        if (SecurityUser.getUser().getSuperAdmin() == SuperAdminEnum.NO.value()) {
            String uploadCountKey = RedisKeys.getOtaUploadCountKey(SecurityUser.getUser().getId());
            Integer uploadCount = (Integer) Optional.ofNullable(redisUtils.get(uploadCountKey)).orElse(0);
            if (uploadCount >= 50) {
                return new Result<String>().error(ErrorCode.OTA_UPLOAD_COUNT_EXCEED);
            }
            // increaseadduploadtimes
            redisUtils.increment(RedisKeys.getOtaUploadCountKey(SecurityUser.getUser().getId()),
                    RedisUtils.DEFAULT_EXPIRE);
        }
        Result<String> result = uploadFirmware(file);

        // generateresourcesourcefilepath
        if (StringUtils.isNotBlank(result.getData())) {
            String uuid = UUID.randomUUID().toString();
            redisUtils.set(RedisKeys.getOtaIdKey(uuid), "file:" + result.getData());
            String downloadUrl = otaUrl.replace("/ota/", "/otaMag/download/") + uuid;
            result.setData(downloadUrl);
        }
        return result;
    }

    private String calculateMD5(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(file.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
