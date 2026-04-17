package xiaozhi.modules.sys.service;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.sys.dto.AdminPageUserDTO;
import xiaozhi.modules.sys.dto.PasswordDTO;
import xiaozhi.modules.sys.dto.SysUserDTO;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.vo.AdminPageUserVO;

/**
 * systemuser
 */
public interface SysUserService extends BaseService<SysUserEntity> {

    SysUserDTO getByUsername(String username);

    SysUserDTO getByUserId(Long userId);

    void save(SysUserDTO dto);

    /**
     * deletespecifieduser，and有associated datadeviceandagent
     * 
     * @param ids
     */
    void deleteById(Long ids);

    /**
     * verificationYesNoallowupdatePassword更改
     * 
     * @param userId      userid
     * @param passwordDTO verificationPassword parameter
     */
    void changePassword(Long userId, PasswordDTO passwordDTO);

    /**
     * directlyupdatePassword，not needverification
     * 
     * @param userId   userid
     * @param password Password
     */
    void changePasswordDirectly(Long userId, String password);

    /**
     * 重置Password
     * 
     * @param userId userid
     * @return 随机generatematching规范 Password
     */
    String resetPassword(Long userId);

    /**
     * administratorpaginationUser information
     * 
     * @param dto paginationfindparameter
     * @return User listpaginationdata
     */
    PageData<AdminPageUserVO> page(AdminPageUserDTO dto);

    /**
     * batchupdateUser status
     * 
     * @param status  User status
     * @param userIds User IDarray
     */
    void changeStatus(Integer status, String[] userIds);

    /**
     * getYesNoallowUser registration
     * 
     * @return YesNoallowUser registration
     */
    boolean getAllowUserRegister();
}
