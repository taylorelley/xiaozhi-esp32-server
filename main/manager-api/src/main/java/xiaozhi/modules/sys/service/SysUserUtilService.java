package xiaozhi.modules.sys.service;


import java.util.function.Consumer;

/**
 * define一个systemusertoolclass，避免andusermodule循环依赖
 * e.g.useranddevice互相依赖，userneedget alldevice，device又needget每个device Username
 * @author zjy
 * @since 2025-4-2
 */
public interface SysUserUtilService {
    /**
     * 赋valueUsername
     * @param userId userid
     * @param setter 赋value方法
     */
    void assignUsername( Long userId, Consumer<String> setter);
}
