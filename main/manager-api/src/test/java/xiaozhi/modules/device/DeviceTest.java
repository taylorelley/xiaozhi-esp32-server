package xiaozhi.modules.device;

import java.util.HashMap;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.modules.sys.dto.SysUserDTO;
import xiaozhi.modules.sys.service.SysUserService;

@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("set备测try")
public class DeviceTest {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SysUserService sysUserService;

    @Test
    public void testSaveUser() {
        SysUserDTO userDTO = new SysUserDTO();
        userDTO.setUsername("test");
        userDTO.setPassword(UUID.randomUUID().toString());
        sysUserService.save(userDTO);
    }

    @Test
    @DisplayName("测trywriteinset备info息")
    public void testWriteDeviceInfo() {
        log.info("start测trywriteinset备info息...");
        // 模拟set备MAC址
        String macAddress = "00:11:22:33:44:66";
        // 模拟set备verificationcode
        String deviceCode = "123456";

        HashMap<String, Object> map = new HashMap<>();
        map.put("mac_address", macAddress);
        map.put("activation_code", deviceCode);
        map.put("board", "harditemtypenumber");
        map.put("app_version", "0.3.13");

        String safeDeviceId = macAddress.replace(":", "_").toLowerCase();
        String cacheDeviceKey = String.format("ota:activation:data:%s", safeDeviceId);
        redisUtils.set(cacheDeviceKey, map, 300);

        String redisKey = "ota:activation:code:" + deviceCode;
        log.info("Redis Key: {}", redisKey);

        // willset备info息writeinRedis
        redisUtils.set(redisKey, macAddress, 300);
        log.info("set备info息alreadywriteinRedis");

        // verificationYesNowriteinsuccess
        String savedMacAddress = (String) redisUtils.get(redisKey);
        log.info("fromRedisreadget MAC址: {}", savedMacAddress);

        // usebreak言verification
        Assertions.assertNotNull(savedMacAddress, "fromRedisreadget MAC址not shouldasempty");
        Assertions.assertEquals(macAddress, savedMacAddress, "保store MAC址andoriginalMAC址not match");

        log.info("测trycomplete");
    }
}