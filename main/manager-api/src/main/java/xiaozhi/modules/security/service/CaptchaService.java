package xiaozhi.modules.security.service;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Verification code
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
public interface CaptchaService {

    /**
     * imageVerification code
     */
    void create(HttpServletResponse response, String uuid) throws IOException;

    /**
     * Verification codevalidate
     * 
     * @param uuid   uuid
     * @param code   Verification code
     * @param delete YesNodeleteVerification code
     * @return true：success false：failed
     */
    boolean validate(String uuid, String code, Boolean delete);

    /**
     * sendSMSVerification code
     * 
     * @param phone mobile
     */
    void sendSMSValidateCode(String phone);

    /**
     * verificationSMSVerification code
     * 
     * @param phone  mobile
     * @param code   Verification code
     * @param delete YesNodeleteVerification code
     * @return true：success false：failed
     */
    boolean validateSMSValidateCode(String phone, String code, Boolean delete);
}
