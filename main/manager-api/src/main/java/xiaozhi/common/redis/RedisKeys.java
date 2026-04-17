package xiaozhi.common.redis;

/**
 * Redis Key constantclass
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
public class RedisKeys {
    /**
     * systemparameterKey
     */
    public static String getSysParamsKey() {
        return "sys:params";
    }

    /**
     * Verification codeKey
     */
    public static String getCaptchaKey(String uuid) {
        return "sys:captcha:" + uuid;
    }

    /**
     * notregisterdeviceVerification codeKey
     */
    public static String getDeviceCaptchaKey(String captcha) {
        return "sys:device:captcha:" + captcha;
    }

    /**
     * userid Key
     */
    public static String getUserIdKey(Long userid) {
        return "sys:username:id:" + userid;
    }

    /**
     * Model name Key
     */
    public static String getModelNameById(String id) {
        return "model:name:" + id;
    }

    /**
     * Model configuration Key
     */
    public static String getModelConfigById(String id) {
        return "model:data:" + id;
    }

    /**
     * getvoicenamecachekey
     */
    public static String getTimbreNameById(String id) {
        return "timbre:name:" + id;
    }

    /**
     * getDevice countcachekey
     */
    public static String getAgentDeviceCountById(String id) {
        return "agent:device:count:" + id;
    }

    /**
     * getagentlastconnectiontimecachekey
     */
    public static String getAgentDeviceLastConnectedAtById(String id) {
        return "agent:device:lastConnected:" + id;
    }

    /**
     * getsystemconfigurationcachekey
     */
    public static String getServerConfigKey() {
        return "server:config";
    }

    /**
     * getvoicedetailscachekey
     */
    public static String getTimbreDetailsKey(String id) {
        return "timbre:details:" + id;
    }

    /**
     * getversionnumberKey
     */
    public static String getVersionKey() {
        return "sys:version";
    }

    /**
     * OTAfirmwareID Key
     */
    public static String getOtaIdKey(String uuid) {
        return "ota:id:" + uuid;
    }

    /**
     * OTAfirmwaredownloadtimes Key
     */
    public static String getOtaDownloadCountKey(String uuid) {
        return "ota:download:count:" + uuid;
    }

    /**
     * getDictionary data cachekey
     */
    public static String getDictDataByTypeKey(String dictType) {
        return "sys:dict:data:" + dictType;
    }

    /**
     * getagentaudioID cachekey
     */
    public static String getAgentAudioIdKey(String uuid) {
        return "agent:audio:id:" + uuid;
    }

    /**
     * getSMSVerification code cachekey
     */
    public static String getSMSValidateCodeKey(String phone) {
        return "sms:Validate:Code:" + phone;
    }

    /**
     * getSMSVerification codelastsendtime cachekey
     */
    public static String getSMSLastSendTimeKey(String phone) {
        return "sms:Validate:Code:" + phone + ":last_send_time";
    }

    /**
     * getSMSVerification codetodaysendtimes cachekey
     */
    public static String getSMSTodayCountKey(String phone) {
        return "sms:Validate:Code:" + phone + ":today_count";
    }

    /**
     * Chat historyUUIDmapping Key
     */
    public static String getChatHistoryKey(String uuid) {
        return "agent:chat:history:" + uuid;
    }

    /**
     * getVoice cloneaudioID cachekey
     */
    public static String getVoiceCloneAudioIdKey(String uuid) {
        return "voiceClone:audio:id:" + uuid;
    }

    /**
     * getKnowledge basecachekey
     */
    public static String getKnowledgeBaseCacheKey(String datasetId) {
        return "knowledge:base:" + datasetId;
    }

    /**
     * gettemporarywhenregisterdevicemarkkey
     */
    public static String getTmpRegisterMacKey(String deviceId) {
        return "tmp_register_mac:" + deviceId;
    }

    /**
     * OTAbinddevice
     */
    public static String getOtaActivationCode(String activationCode) {
        return "ota:activation:code:" + activationCode;
    }

    /**
     * OTAgetdevicemacrelatedinformation
     */
    public static String getOtaDeviceActivationInfo(String deviceId) {
        return "ota:activation:data:" + deviceId;
    }

    /**
     * OTAuploadtimes
     */
    public static String getOtaUploadCountKey(Long username) {
        return "ota:upload:count:" + username;
    }
}
