package xiaozhi.modules.sms.service;

/**
 * SMSservice 方法defineinterface
 *
 * @author zjy
 * @since 2025-05-12
 */
public interface SmsService {

    /**
     * sendVerification codeSMS
     * @param phone Mobile phone number
     * @param VerificationCode Verification code
     */
    void sendVerificationCodeSms(String phone, String VerificationCode) ;
}
