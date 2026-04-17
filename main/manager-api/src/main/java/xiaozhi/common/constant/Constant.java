package xiaozhi.common.constant;

import lombok.Getter;

/**
 * constant
 * Copyright (c) Renren Opensource All rights reserved.
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
     * Menurootnodeidentifier
     */
    Long MENU_ROOT = 0L;
    /**
     * departmentrootnodeidentifier
     */
    Long DEPT_ROOT = 0L;
    /**
     * dataDictionaryrootnodeidentifier
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
     * Sort orderway
     */
    String ORDER = "order";

    /**
     * requestheadergrantpermissionidentifier
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
     * belowsendsixbitVerification codewhendisplay controlsideboardAddress
     */
    String SERVER_FRONTED_URL = "server.fronted_url";

    /**
     * pathcutsymbol
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
     * WebSocketauthenticationopenrelated
     */
    String SERVER_AUTH_ENABLED = "server.auth.enabled";

    /**
     * nomemory
     */
    String MEMORY_NO_MEM = "Memory_nomem";

    /**
     * onlyreportChat history（not summary memory）
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
     * Huoshan Enginedual channelvoiceclone
     */
    String VOICE_CLONE_HUOSHAN_DOUBLE_STREAM = "huoshan_double_stream";

    /**
     * RAGconfigurationtype
     */
    String RAG_CONFIG_TYPE = "RAG";

    enum SysBaseParam {
        /**
         * ICPrecordnumber
         */
        BEIAN_ICP_NUM("server.beian_icp_num"),
        /**
         * GArecordnumber
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
         * incloudgrantpermissionkeyID
         */
        ALIYUN_SMS_ACCESS_KEY_ID("aliyun.sms.access_key_id"),
        /**
         * incloudgrantpermissionkey
         */
        ALIYUN_SMS_ACCESS_KEY_SECRET("aliyun.sms.access_key_secret"),
        /**
         * incloudSMSsignature
         */
        ALIYUN_SMS_SIGN_NAME("aliyun.sms.sign_name"),
        /**
         * incloudSMStemplate
         */
        ALIYUN_SMS_SMS_CODE_TEMPLATE_CODE("aliyun.sms.sms_code_template_code"),
        /**
         * numbercodemostlargeSMSsenditemsnumber
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
        RECORD_TEXT_AUDIO(2, "textaudioallrecord");

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
     * noeffectivefirmwareURL
     */
    String INVALID_FIRMWARE_URL = "http://xiaozhi.server.com:8002/xiaozhi/otaMag/download/NOT_ACTIVATED_FIRMWARE_THIS_IS_A_INVALID_URL";

    /**
     * Dictionary type
     */
    enum DictType {
        /**
         * mobileareanumber
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