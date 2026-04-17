package xiaozhi.modules.security.service;

import xiaozhi.modules.security.entity.SysUserTokenEntity;
import xiaozhi.modules.sys.entity.SysUserEntity;

/**
 * shirorelatedinterface
 * Copyright (c) 人人开source All rights reserved.
 * Website: https://www.renren.io
 */
public interface ShiroService {

    SysUserTokenEntity getByToken(String token);

    /**
     * according toUser ID，queryuser
     *
     * @param userId
     */
    SysUserEntity getUser(Long userId);

}
