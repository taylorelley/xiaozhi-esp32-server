package xiaozhi.modules.sys.service;


import java.util.function.Consumer;

/**
 * defineonesystemusertoolclass，avoidandusermoduleloopdependency
 * e.g.useranddevicemutualdependency，userneedget alldevice，deviceandneedgeteverydevice Username
 * @author zjy
 * @since 2025-4-2
 */
public interface SysUserUtilService {
    /**
     * assignvalueUsername
     * @param userId userid
     * @param setter assignvaluemethod
     */
    void assignUsername( Long userId, Consumer<String> setter);
}
