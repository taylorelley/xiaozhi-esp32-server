package xiaozhi.common.constant;

import lombok.Getter;

/**
 * 常量
 * Copyright (c) 人人开source All rights reserved.
 * Website: https://www.renren.io
 */
public interface Constant {
    /**
     * success
     */
    int SUCCESS = 1;
    /**
     * failed
     */
    int FAIL = 0;
    /**
     * OK
     */
    String OK = "OK";
    /**
     * useridentifier
     */
    String USER_KEY = "userId";
    /**
     * Menu根节点identifier
     */
    Long MENU_ROOT = 0L;
    /**
     * department根节点identifier
     */
    Long DEPT_ROOT = 0L;
    /**
     * dataDictionary根节点identifier
     */
    Long DICT_ROOT = 0L;
    /**
     * ascending
     */
    String ASC = "asc";
    /**
     * descending
     */
    String DESC = "desc";
    /**
     * Create timefield name
     */
    String CREATE_DATE = "create_date";

    /**
     * Create timefield name
     */
    String ID = "id";

    /**
     * dataPermissionfilter
     */
    String SQL_FILTER = "sqlFilter";

    /**
     * currentpage number
     */
    String PAGE = "page";
    /**
     * per pagerecordnumber
     */
    String LIMIT = "limit";
    /**
     * Sort orderfield
     */
    String ORDER_FIELD = "orderField";
    /**
     * Sort order方式
     */
    String ORDER = "order";

    /**
     * requestheader授权identifier
     */
    String AUTHORIZATION = "Authorization";

    /**
     * servicekey
     */
    String SERVER_SECRET = "server.secret";

    /**
     * SM2public key
     */
    String SM2_PUBLIC_KEY = "server.public_key";

    /**
     * SM2private key
     */
    String SM2_PRIVATE_KEY = "server.private_key";

    /**
     * websocketAddress
     */
    String SERVER_WEBSOCKET = "server.websocket";

    /**
     * mqtt gateway configuration
     */
    String SERVER_MQTT_GATEWAY = "server.mqtt_gateway";

    /**
     * otaAddress
     */
    String SERVER_OTA = "server.ota";

    /**
     * YesNoallowUser registration
     */
    String SERVER_ALLOW_USER_REGISTER = "server.allow_user_register";

    /**
     * 下发六bitVerification code时显示 control面板Address
     */
    String SERVER_FRONTED_URL = "server.fronted_url";

    /**
     * path分割符
     */
    String FILE_EXTENSION_SEG = ".";

    /**
     * mcpendpointpath
     */
    String SERVER_MCP_ENDPOINT = "server.mcp_endpoint";

    /**
     * mcpendpointpath
     */
    String SERVER_VOICE_PRINT = "server.voice_print";

    /**
     * mqttkey
     */
    String SERVER_MQTT_SECRET = "server.mqtt_signature_key";

    /**
     * WebSocketauthentication开关
     */
    String SERVER_AUTH_ENABLED = "server.auth.enabled";

    /**
     * 无memory
     */
    String MEMORY_NO_MEM = "Memory_nomem";

    /**
     * only上报Chat history（not summary memory）
     */
    String MEMORY_MEM_REPORT_ONLY = "Memory_mem_report_only";

    /**
     * Mem0AImemory
     */
    String MEMORY_MEM0AI = "Memory_mem0ai";

    /**
     * PowerMemmemory
     */
    String MEMORY_POWERMEM = "Memory_powermem";

    /**
     * 火山引擎双声道voice克隆
     */
    String VOICE_CLONE_HUOSHAN_DOUBLE_STREAM = "huoshan_double_stream";

    /**
     * RAGconfigurationtype
     */
    String RAG_CONFIG_TYPE = "RAG";

    enum SysBaseParam {
        /**
         * ICP备案number
         */
        BEIAN_ICP_NUM("server.beian_icp_num"),
        /**
         * GA备案number
         */
        BEIAN_GA_NUM("server.beian_ga_num"),
        /**
         * systemname
         */
        SERVER_NAME("server.name");

        private String value;

        SysBaseParam(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * trainingstatus
     */
    enum TrainStatus {
        /**
         * nottraining
         */
        NOT_TRAINED(0),
        /**
         * training
         */
        TRAINING(1),
        /**
         * alreadytraining
         */
        TRAINED(2),
        /**
         * trainingfailed
         */
        TRAIN_FAILED(3);

        private final int code;

        TrainStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * systemSMS
     */
    enum SysMSMParam {
        /**
         * 阿里云授权keyID
         */
        ALIYUN_SMS_ACCESS_KEY_ID("aliyun.sms.access_key_id"),
        /**
         * 阿里云授权key
         */
        ALIYUN_SMS_ACCESS_KEY_SECRET("aliyun.sms.access_key_secret"),
        /**
         * 阿里云SMSsignature
         */
        ALIYUN_SMS_SIGN_NAME("aliyun.sms.sign_name"),
        /**
         * 阿里云SMStemplate
         */
        ALIYUN_SMS_SMS_CODE_TEMPLATE_CODE("aliyun.sms.sms_code_template_code"),
        /**
         * 单numbercode最largeSMSsenditemsnumber
         */
        SERVER_SMS_MAX_SEND_COUNT("server.sms_max_send_count"),
        /**
         * YesNoenableMobile registration
         */
        SERVER_ENABLE_MOBILE_REGISTER("server.enable_mobile_register");

        private String value;

        SysMSMParam(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * datastatus
     */
    enum DataOperation {
        /**
         * insert
         */
        INSERT("I"),
        /**
         * alreadyupdate
         */
        UPDATE("U"),
        /**
         * alreadydelete
         */
        DELETE("D");

        private String value;

        DataOperation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Getter
    enum ChatHistoryConfEnum {
        IGNORE(0, "not record"),
        RECORD_TEXT(1, "recordtext"),
        RECORD_TEXT_AUDIO(2, "textaudio都record");

        private final int code;
        private final String name;

        ChatHistoryConfEnum(int code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * versionnumber
     */
    public static final String VERSION = "0.9.2";

    /**
     * 无效firmwareURL
     */
    String INVALID_FIRMWARE_URL = "http://xiaozhi.server.com:8002/xiaozhi/otaMag/download/NOT_ACTIVATED_FIRMWARE_THIS_IS_A_INVALID_URL";

    /**
     * Dictionary type
     */
    enum DictType {
        /**
         * mobile区number
         */
        MOBILE_AREA("MOBILE_AREA");

        private String value;

        DictType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}