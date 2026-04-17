package xiaozhi.common.exception;

/**
 * Error code composed of 5 digits. The first 2 digits represent the module code,
 * and the last 3 digits represent the business code.
 * <p>
 * For example: 10001 (10 represents the system module, 001 represents the business code)
 * </p>
 * Copyright (c) Renren Open Source All rights reserved.
 * Website: https://www.renren.io
 */
public interface ErrorCode {
    int INTERNAL_SERVER_ERROR = 500;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;

    int NOT_NULL = 10001;
    int DB_RECORD_EXISTS = 10002;
    int PARAMS_GET_ERROR = 10003;
    int ACCOUNT_PASSWORD_ERROR = 10004;
    int ACCOUNT_DISABLE = 10005;
    int IDENTIFIER_NOT_NULL = 10006;
    int CAPTCHA_ERROR = 10007;
    int PHONE_NOT_NULL = 10008;
    int PASSWORD_ERROR = 10009;

    int SUPERIOR_DEPT_ERROR = 10011;
    int SUPERIOR_MENU_ERROR = 10012;
    int DATA_SCOPE_PARAMS_ERROR = 10013;
    int DEPT_SUB_DELETE_ERROR = 10014;
    int DEPT_USER_DELETE_ERROR = 10015;

    int UPLOAD_FILE_EMPTY = 10019;
    int TOKEN_NOT_EMPTY = 10020;
    int TOKEN_INVALID = 10021;
    int ACCOUNT_LOCK = 10022;

    int OSS_UPLOAD_FILE_ERROR = 10024;

    int REDIS_ERROR = 10027;
    int JOB_ERROR = 10028;
    int INVALID_SYMBOL = 10029;
    int PASSWORD_LENGTH_ERROR = 10030;
    int PASSWORD_WEAK_ERROR = 10031;
    int DEL_MYSELF_ERROR = 10032;
    int DEVICE_CAPTCHA_ERROR = 10033;

    // Parameter validation related error codes
    int PARAM_VALUE_NULL = 10034;
    int PARAM_TYPE_NULL = 10035;
    int PARAM_TYPE_INVALID = 10036;
    int PARAM_NUMBER_INVALID = 10037;
    int PARAM_BOOLEAN_INVALID = 10038;
    int PARAM_ARRAY_INVALID = 10039;
    int PARAM_JSON_INVALID = 10040;

    int OTA_DEVICE_NOT_FOUND = 10041;
    int OTA_DEVICE_NEED_BIND = 10042;

    // New error codes
    int DELETE_DATA_FAILED = 10043;
    int USER_NOT_LOGIN = 10044;
    int WEB_SOCKET_CONNECT_FAILED = 10045;
    int VOICE_PRINT_SAVE_ERROR = 10046;
    int TODAY_SMS_LIMIT_REACHED = 10047;
    int OLD_PASSWORD_ERROR = 10048;
    int INVALID_LLM_TYPE = 10049;
    int TOKEN_GENERATE_ERROR = 10050;
    int RESOURCE_NOT_FOUND = 10051;

    // New error codes
    int DEFAULT_AGENT_NOT_FOUND = 10052;
    int AGENT_NOT_FOUND = 10053;
    int VOICEPRINT_API_NOT_CONFIGURED = 10054;
    int SMS_SEND_FAILED = 10055;
    int SMS_CONNECTION_FAILED = 10056;
    int AGENT_VOICEPRINT_CREATE_FAILED = 10057;
    int AGENT_VOICEPRINT_UPDATE_FAILED = 10058;
    int AGENT_VOICEPRINT_DELETE_FAILED = 10059;
    int SMS_SEND_TOO_FREQUENTLY = 10060;
    int ACTIVATION_CODE_EMPTY = 10061;
    int ACTIVATION_CODE_ERROR = 10062;
    int DEVICE_ALREADY_ACTIVATED = 10063;
    // Default model deletion error
    int DEFAULT_MODEL_DELETE_ERROR = 10064;
    // Login related error codes
    int ADD_DATA_FAILED = 10065; // Failed to add data
    int UPDATE_DATA_FAILED = 10066; // Failed to update data
    int SMS_CAPTCHA_ERROR = 10067; // SMS verification code error
    int MOBILE_REGISTER_DISABLED = 10068; // Mobile registration is not enabled
    int USERNAME_NOT_PHONE = 10069; // Username is not a mobile phone number
    int PHONE_ALREADY_REGISTERED = 10070; // Mobile phone number already registered
    int PHONE_NOT_REGISTERED = 10071; // Mobile phone number not registered
    int USER_REGISTER_DISABLED = 10072; // User registration not allowed
    int RETRIEVE_PASSWORD_DISABLED = 10073; // Password retrieval feature is not enabled
    int PHONE_FORMAT_ERROR = 10074; // Mobile phone number format is incorrect
    int SMS_CODE_ERROR = 10075; // SMS verification code error

    // Dictionary type related error codes
    int DICT_TYPE_NOT_EXIST = 10076; // Dictionary type does not exist
    int DICT_TYPE_DUPLICATE = 10077; // Dictionary type code duplicated

    // Resource processing related error codes
    int RESOURCE_READ_ERROR = 10078; // Failed to read resource

    // Agent related error codes
    int LLM_INTENT_PARAMS_MISMATCH = 10079; // LLM model and Intent recognition selected parameters do not match

    // Voiceprint related error codes
    int VOICEPRINT_ALREADY_REGISTERED = 10080; // This voiceprint has already been registered
    int VOICEPRINT_DELETE_ERROR = 10081; // Error occurred while deleting voiceprint
    int VOICEPRINT_UPDATE_NOT_ALLOWED = 10082; // Voiceprint modification not allowed, voice already registered
    int VOICEPRINT_UPDATE_ADMIN_ERROR = 10083; // Voiceprint modification error, please contact administrator
    int VOICEPRINT_API_URI_ERROR = 10084; // Voiceprint interface address error
    int VOICEPRINT_AUDIO_NOT_BELONG_AGENT = 10085; // Audio data does not belong to this agent
    int VOICEPRINT_AUDIO_EMPTY = 10086; // Audio data is empty
    int VOICEPRINT_REGISTER_REQUEST_ERROR = 10087; // Voiceprint save request failed
    int VOICEPRINT_REGISTER_PROCESS_ERROR = 10088; // Voiceprint save processing failed
    int VOICEPRINT_UNREGISTER_REQUEST_ERROR = 10089; // Voiceprint cancellation request failed
    int VOICEPRINT_UNREGISTER_PROCESS_ERROR = 10090; // Voiceprint cancellation processing failed
    int VOICEPRINT_IDENTIFY_REQUEST_ERROR = 10091; // Voiceprint identification request failed

    int LLM_NOT_EXIST = 10092; // The configured LLM does not exist
    int MODEL_REFERENCED_BY_AGENT = 10093; // This model configuration is referenced by an agent, cannot be deleted
    int LLM_REFERENCED_BY_INTENT = 10094; // This LLM model is referenced by intent recognition configuration, cannot be deleted

    // Server management related error codes
    int INVALID_SERVER_ACTION = 10095; // Invalid server operation
    int SERVER_WEBSOCKET_NOT_CONFIGURED = 10096; // Server WebSocket address not configured
    int TARGET_WEBSOCKET_NOT_EXIST = 10097; // Target WebSocket address does not exist

    // Parameter validation related error codes
    int WEBSOCKET_URLS_EMPTY = 10098; // WebSocket address list cannot be empty
    int WEBSOCKET_URL_LOCALHOST = 10099; // WebSocket address cannot use localhost or 127.0.0.1
    int WEBSOCKET_URL_FORMAT_ERROR = 10100; // WebSocket address format is incorrect
    int WEBSOCKET_CONNECTION_FAILED = 10101; // WebSocket connection test failed
    int OTA_URL_EMPTY = 10102; // OTA address cannot be empty
    int OTA_URL_LOCALHOST = 10103; // OTA address cannot use localhost or 127.0.0.1
    int OTA_URL_PROTOCOL_ERROR = 10104; // OTA address must start with http or https
    int OTA_URL_FORMAT_ERROR = 10105; // OTA address must end with /ota/
    int OTA_INTERFACE_ACCESS_FAILED = 10106; // OTA interface access failed
    int OTA_INTERFACE_FORMAT_ERROR = 10107; // OTA interface return content format is incorrect
    int OTA_INTERFACE_VALIDATION_FAILED = 10108; // OTA interface validation failed
    int MCP_URL_EMPTY = 10109; // MCP address cannot be empty
    int MCP_URL_LOCALHOST = 10110; // MCP address cannot use localhost or 127.0.0.1
    int MCP_URL_INVALID = 10111; // Not a valid MCP address
    int MCP_INTERFACE_ACCESS_FAILED = 10112; // MCP interface access failed
    int MCP_INTERFACE_FORMAT_ERROR = 10113; // MCP interface return content format is incorrect
    int MCP_INTERFACE_VALIDATION_FAILED = 10114; // MCP interface validation failed
    int VOICEPRINT_URL_EMPTY = 10115; // Voiceprint interface address cannot be empty
    int VOICEPRINT_URL_LOCALHOST = 10116; // Voiceprint interface address cannot use localhost or 127.0.0.1
    int VOICEPRINT_URL_INVALID = 10117; // Not a valid voiceprint interface address
    int VOICEPRINT_URL_PROTOCOL_ERROR = 10118; // Voiceprint interface address must start with http or https
    int VOICEPRINT_INTERFACE_ACCESS_FAILED = 10119; // Voiceprint interface access failed
    int VOICEPRINT_INTERFACE_FORMAT_ERROR = 10120; // Voiceprint interface return content format is incorrect
    int VOICEPRINT_INTERFACE_VALIDATION_FAILED = 10121; // Voiceprint interface validation failed
    int MQTT_SECRET_EMPTY = 10122; // MQTT secret cannot be empty
    int MQTT_SECRET_LENGTH_INSECURE = 10123; // MQTT secret length is insecure
    int MQTT_SECRET_CHARACTER_INSECURE = 10124; // MQTT secret must contain both uppercase and lowercase letters
    int MQTT_SECRET_WEAK_PASSWORD = 10125; // MQTT secret contains weak password
    int DICT_LABEL_DUPLICATE = 10128; // Dictionary label duplicated
    int SM2_KEY_NOT_CONFIGURED = 10129; // SM2 key not configured
    int SM2_DECRYPT_ERROR = 10130; // SM2 decryption failed
    int MODEL_TYPE_PROVIDE_CODE_NOT_NULL = 10131; // modelType and provideCode cannot be empty

    // Chat history related error codes
    int CHAT_HISTORY_NO_PERMISSION = 10132; // No permission to view the chat history of this agent
    int CHAT_HISTORY_SESSION_ID_NOT_NULL = 10133; // Session ID cannot be empty
    int CHAT_HISTORY_AGENT_ID_NOT_NULL = 10134; // Agent ID cannot be empty
    int CHAT_HISTORY_DOWNLOAD_FAILED = 10135; // Chat history download failed
    int DOWNLOAD_LINK_EXPIRED = 10136; // Download link expired or invalid
    int DOWNLOAD_LINK_INVALID = 10137; // Download link invalid
    int CHAT_ROLE_USER = 10138; // User role
    int CHAT_ROLE_AGENT = 10139; // Agent role

    // Voice clone related error codes
    int VOICE_CLONE_AUDIO_EMPTY = 10140; // Audio file cannot be empty
    int VOICE_CLONE_NOT_AUDIO_FILE = 10141; // Only audio files are supported
    int VOICE_CLONE_AUDIO_TOO_LARGE = 10142; // Audio file size cannot exceed 10MB
    int VOICE_CLONE_UPLOAD_FAILED = 10143; // Upload failed
    int VOICE_CLONE_RECORD_NOT_EXIST = 10144; // Voice clone record does not exist
    int VOICE_RESOURCE_INFO_EMPTY = 10145; // Voice resource information cannot be empty
    int VOICE_RESOURCE_PLATFORM_NAME_EMPTY = 10146; // Platform name cannot be empty
    int VOICE_RESOURCE_ID_EMPTY = 10147; // Voice ID cannot be empty
    int VOICE_RESOURCE_ACCOUNT_EMPTY = 10148; // Owner account cannot be empty
    int VOICE_RESOURCE_DELETE_ID_EMPTY = 10149; // Voice resource ID to delete cannot be empty
    int VOICE_RESOURCE_NO_PERMISSION = 10150; // You do not have permission to operate this record
    int VOICE_CLONE_AUDIO_NOT_UPLOADED = 10151; // Please upload audio file first
    int VOICE_CLONE_MODEL_CONFIG_NOT_FOUND = 10152; // Model configuration not found
    int VOICE_CLONE_MODEL_TYPE_NOT_FOUND = 10153; // Model type not found
    int VOICE_CLONE_TRAINING_FAILED = 10154; // Training failed
    int VOICE_CLONE_HUOSHAN_CONFIG_MISSING = 10155; // Huoshan Engine configuration missing
    int VOICE_CLONE_RESPONSE_FORMAT_ERROR = 10156; // Response format error
    int VOICE_CLONE_REQUEST_FAILED = 10157; // Request failed
    int VOICE_CLONE_PREFIX = 10158; // Cloned voice prefix
    int VOICE_ID_ALREADY_EXISTS = 10159; // Voice ID already exists
    int VOICE_CLONE_HUOSHAN_VOICE_ID_ERROR = 10160; // Huoshan Engine voice ID format error

    // Device related error codes
    int MAC_ADDRESS_ALREADY_EXISTS = 10161; // Mac address already exists
    // Model related error codes
    int MODEL_PROVIDER_NOT_EXIST = 10162; // Model provider does not exist

    // Knowledge base related error codes
    int Knowledge_Base_RECORD_NOT_EXISTS = 10163; // Knowledge base record does not exist
    int RAG_CONFIG_NOT_FOUND = 10164; // RAG configuration not found
    int RAG_CONFIG_TYPE_ERROR = 10165; // RAG configuration type error
    int RAG_DEFAULT_CONFIG_NOT_FOUND = 10166; // Default RAG configuration not found
    int RAG_API_ERROR = 10167; // RAG call failed
    int UPLOAD_FILE_ERROR = 10168; // Upload file failed
    int NO_PERMISSION = 10169; // No permission
    int KNOWLEDGE_BASE_NAME_EXISTS = 10170; // Knowledge base with same name already exists
    int RAG_API_ERROR_URL_NULL = 10171; // base_url in RAG configuration is empty, please complete the configuration
    int RAG_API_ERROR_API_KEY_NULL = 10172; // api_key in RAG configuration is empty, please complete the configuration
    int RAG_API_ERROR_API_KEY_INVALID = 10173; // api_key in RAG configuration contains placeholder, please replace with actual API key
    int RAG_API_ERROR_URL_INVALID = 10174; // base_url in RAG configuration has incorrect format, please check the protocol
    int RAG_DATASET_ID_NOT_NULL = 10176; // dataset_id in RAG configuration cannot be empty
    int RAG_MODEL_ID_NOT_NULL = 10177; // model_id in RAG configuration cannot be empty
    int RAG_DATASET_ID_AND_MODEL_ID_NOT_NULL = 10178; // dataset_id and model_id in RAG configuration cannot be empty
    int RAG_FILE_NAME_NOT_NULL = 10179; // File name cannot be empty
    int RAG_FILE_CONTENT_EMPTY = 10180; // File content cannot be empty

    // Device related error codes (supplement)
    int MCA_NOT_NULL = 10175; // Mac address cannot be empty

    // Voice clone (supplement)
    int VOICE_CLONE_NAME_NOT_NULL = 10181; // Voice clone name cannot be empty
    int VOICE_CLONE_AUDIO_NOT_FOUND = 10182; // Voice clone audio does not exist

    // Agent template related error codes (supplement)
    int AGENT_TEMPLATE_NOT_FOUND = 10183; // Default agent not found

    // Knowledge base adapter related error codes
    int RAG_ADAPTER_TYPE_NOT_SUPPORTED = 10184; // Unsupported adapter type
    int RAG_CONFIG_VALIDATION_FAILED = 10185; // RAG configuration validation failed
    int RAG_ADAPTER_CREATION_FAILED = 10186; // Adapter creation failed
    int RAG_ADAPTER_INIT_FAILED = 10187; // Adapter initialization failed
    int RAG_ADAPTER_CONNECTION_FAILED = 10188; // Adapter connection test failed
    int RAG_ADAPTER_OPERATION_FAILED = 10189; // Adapter operation failed
    int RAG_ADAPTER_NOT_FOUND = 10190; // Adapter not found
    int RAG_ADAPTER_CACHE_ERROR = 10191; // Adapter cache error
    int RAG_ADAPTER_TYPE_NOT_FOUND = 10192; // Adapter type not found

    // Device tool related error codes
    int DEVICE_ID_NOT_NULL = 10193; // Device ID cannot be empty
    int DEVICE_NOT_EXIST = 10194; // Device does not exist
    int OTA_UPLOAD_COUNT_EXCEED = 10195; // OTA upload count exceeds limit

    // Agent tag related error codes
    int AGENT_TAG_NAME_DUPLICATE = 10196; // Tag name already exists
    int AGENT_TAG_NAME_EMPTY = 10197; // Tag name cannot be empty
    int AGENT_TAG_NOT_EXIST = 10198; // Tag does not exist

    int RAG_DOCUMENT_PARSING_DELETE_ERROR = 10199; // Document is being parsed, deletion is forbidden

    // Agent MCP related error codes
    int MCP_ACCESS_POINT_ADDRESS_NO_PERMISSION = 10200; // No permission to view the MCP endpoint address of this agent
    int MCP_ACCESS_POINT_ADDRESS_NOT_CONFIGURED = 10201; // Please contact the administrator to configure the MCP endpoint address in parameter management
    int MCP_ACCESS_POINT_TOOLS_LIST_NO_PERMISSION = 10202; // No permission to view the MCP tool list of this agent
}
