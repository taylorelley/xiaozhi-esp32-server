package xiaozhi.common.user;

import java.io.Serializable;

import lombok.Data;

/**
 * loginUser information
 * Copyright (c) 人人开source All rights reserved.
 * Website: https://www.renren.io
 */
@Data
public class UserDetail implements Serializable {
    private Long id;
    private String username;
    private Integer superAdmin;
    private String token;
    private Integer status;
}