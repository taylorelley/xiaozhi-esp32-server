package xiaozhi.modules.knowledge.dto.chat;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

/**
 * conversationmanagementaggregation DTO
 * <p>
 * 容class，内含conversationassistant、sessionandmessage allrequest/responseobject。
 * </p>
 */
@Schema(description = "conversationmanagementaggregation DTO")
public class ChatDTO {

    // ========== 1. conversationassistant (Assistant/Bot) related ==========

    /**
     * promptconfiguration
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "promptconfiguration")
    public static class PromptConfig implements Serializable {

        @Schema(description = "systemprompt", example = "youYesone专业 customer serviceassistant...")
        @JsonProperty("prompt")
        private String systemPrompt;

        @Schema(description = "开场白", example = "您好，IYes您 intelligent assistant，请问has什么可to帮您？")
        private String opener;

        @Schema(description = "emptyresult回复", example = "抱歉，Inofindtorelatedinformation。")
        @JsonProperty("empty_response")
        private String emptyResponse;

        @Schema(description = "YesNodisplayreference", example = "true")
        @JsonProperty("show_quote")
        private Boolean quote;

        @Schema(description = "YesNoenable TTS", example = "false")
        private Boolean tts;

        @Schema(description = "similar度阈value (0.0 - 1.0)", example = "0.2")
        @JsonProperty("similarity_threshold")
        private Float similarityThreshold;

        @Schema(description = "keywordsimilar度权re- (0.0 - 1.0)", example = "0.7")
        @JsonProperty("keywords_similarity_weight")
        private Float vectorSimilarityWeight;

        @Schema(description = "retrieve Top N", example = "6")
        @JsonProperty("top_n")
        private Integer topK;

        @Schema(description = "Rerank model", example = "rerank_model_001")
        @JsonProperty("rerank_model")
        private String rerankId;

        @Schema(description = "YesNoenable多轮conversationpriority化", example = "false")
        @JsonProperty("refine_multiturn")
        private Boolean refineMultigraph;

        @Schema(description = "变量list")
        private List<Map<String, Object>> variables;
    }

    /**
     * LLM configuration
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "LLM Model configuration")
    public static class LLMConfig implements Serializable {

        @NotBlank(message = "Model namecannot be empty")
        @Schema(description = "Model name", requiredMode = Schema.RequiredMode.REQUIRED, example = "gpt-4")
        @JsonProperty("model_name")
        private String modelName;

        @Schema(description = "温度parameter (0.0 - 2.0)", example = "0.7")
        private Float temperature;

        @Schema(description = "Top P 采样", example = "0.9")
        @JsonProperty("top_p")
        private Float topP;

        @Schema(description = "mostlarge Token number", example = "4096")
        @JsonProperty("max_tokens")
        private Integer maxTokens;

        @Schema(description = "storein惩罚", example = "0.0")
        @JsonProperty("presence_penalty")
        private Float presencePenalty;

        @Schema(description = "频率惩罚", example = "0.0")
        @JsonProperty("frequency_penalty")
        private Float frequencyPenalty;
    }

    /**
     * createassistantrequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "createassistantrequest")
    public static class AssistantCreateReq implements Serializable {

        @NotBlank(message = "assistantnamecannot be empty")
        @Schema(description = "assistantname", requiredMode = Schema.RequiredMode.REQUIRED, example = "智cancustomer serviceassistant")
        private String name;

        @Schema(description = "assistantavatar (Base64 code)", example = "")
        private String avatar;

        @Schema(description = "associated Knowledge base ID list", example = "[\"kb_001\", \"kb_002\"]")
        @JsonProperty("dataset_ids")
        private List<String> datasetIds;

        @Schema(description = "assistantDescription", example = "thisYesone智cancustomer serviceassistant")
        private String description;

        @Schema(description = "LLM Model configuration")
        @JsonProperty("llm")
        private LLMConfig llm;

        @Schema(description = "promptconfiguration")
        @JsonProperty("prompt")
        private PromptConfig promptConfig;
    }

    /**
     * updateassistantrequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "updateassistantrequest")
    public static class AssistantUpdateReq implements Serializable {

        @Schema(description = "assistantname", example = "智cancustomer serviceassistant V2")
        private String name;

        @Schema(description = "assistantavatar (Base64 code)", example = "")
        private String avatar;

        @Schema(description = "associated Knowledge base ID list", example = "[\"kb_001\", \"kb_002\"]")
        @JsonProperty("dataset_ids")
        private List<String> datasetIds;

        @Schema(description = "assistantDescription", example = "thisYesone智cancustomer serviceassistant")
        private String description;

        @Schema(description = "LLM Model configuration")
        @JsonProperty("llm")
        private LLMConfig llm;

        @Schema(description = "promptconfiguration")
        @JsonProperty("prompt")
        private PromptConfig promptConfig;
    }

    /**
     * queryassistantlistrequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "queryassistantlistrequest")
    public static class AssistantListReq implements Serializable {

        @Schema(description = "page number (from 1 start)", example = "1")
        private Integer page;

        @Schema(description = "per pagecount", example = "30")
        @JsonProperty("page_size")
        private Integer pageSize;

        @Schema(description = "bynamefilter (模糊匹配)", example = "customer service")
        private String name;

        @Schema(description = "Sort orderfield: create_time / update_time", example = "create_time")
        private String orderby;

        @Schema(description = "YesNodescending", example = "true")
        private Boolean desc;

        @Schema(description = "by ID 精确filter", example = "assistant_001")
        private String id;
    }

    /**
     * assistantdetails VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "assistantdetails VO")
    public static class AssistantVO implements Serializable {

        @Schema(description = "assistant ID", example = "assistant_001")
        private String id;

        @Schema(description = "tenant ID", example = "tenant_001")
        @JsonProperty("tenant_id")
        private String tenantId;

        @Schema(description = "assistantname", example = "智cancustomer serviceassistant")
        private String name;

        @Schema(description = "assistantavatar", example = "")
        private String avatar;

        @Schema(description = "associated Knowledge base ID list")
        @JsonProperty("dataset_ids")
        private List<String> datasetIds;

        @Schema(description = "associated Knowledge baselist (details)")
        private List<SimpleDatasetVO> datasets;

        @Schema(description = "assistantDescription")
        private String description;

        @Schema(description = "LLM Model configuration")
        @JsonProperty("llm")
        private LLMConfig llm;

        @Schema(description = "promptconfiguration")
        @JsonProperty("prompt")
        private PromptConfig promptConfig;

        @Schema(description = "Create time (timestamp)", example = "1700000000000")
        @JsonProperty("create_time")
        private Long createTime;

        @Schema(description = "updatetime (timestamp)", example = "1700000001000")
        @JsonProperty("update_time")
        private Long updateTime;
    }

    /**
     * deleteassistantrequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "deleteassistantrequest")
    public static class AssistantDeleteReq implements Serializable {

        @Schema(description = "need todelete assistant ID list", example = "[\"assistant_001\", \"assistant_002\"]")
        private List<String> ids;
    }

    // ========== 2. session (Session) related ==========

    /**
     * createsessionrequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "createsessionrequest")
    public static class SessionCreateReq implements Serializable {

        @Schema(description = "sessionname", example = "技术咨询session")
        private String name;

        @Schema(description = "user ID", example = "user_001")
        @JsonProperty("user_id")
        private String userId;
    }

    /**
     * updatesessionrequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "updatesessionrequest")
    public static class SessionUpdateReq implements Serializable {

        @Schema(description = "sessionname", example = "技术咨询session - update")
        private String name;
    }

    /**
     * querysessionlistrequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "querysessionlistrequest")
    public static class SessionListReq implements Serializable {

        @Schema(description = "assistant ID", example = "assistant_001")
        @JsonProperty("assistant_id")
        private String assistantId;

        @Schema(description = "page number (from 1 start)", example = "1")
        private Integer page;

        @Schema(description = "per pagecount", example = "30")
        @JsonProperty("page_size")
        private Integer pageSize;

        @Schema(description = "bynamefilter", example = "技术")
        private String name;

        @Schema(description = "Sort orderfield", example = "create_time")
        private String orderby;

        @Schema(description = "YesNodescending", example = "true")
        private Boolean desc;

        @Schema(description = "session ID 精确filter", example = "session_001")
        private String id;

        @Schema(description = "useridentifierfilter", example = "user_001")
        @JsonProperty("user_id")
        private String userId;
    }

    /**
     * sessiondetails VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "sessiondetails VO")
    public static class SessionVO implements Serializable {

        @Schema(description = "session ID", example = "session_001")
        private String id;

        @Schema(description = "assistant ID", example = "assistant_001")
        @JsonProperty("chat_id")
        private String chatId;

        @Schema(description = "assistant ID (compatible旧版)", example = "assistant_001")
        @JsonProperty("assistant_id")
        private String assistantId;

        @Schema(description = "sessionname", example = "技术咨询session")
        private String name;

        @Schema(description = "Create time (timestamp)", example = "1700000000000")
        @JsonProperty("create_time")
        private Long createTime;

        @Schema(description = "updatetime (timestamp)", example = "1700000001000")
        @JsonProperty("update_time")
        private Long updateTime;

        @Schema(description = "createdate", example = "2024-05-01 10:00:00")
        @JsonProperty("create_date")
        private String createDate;

        @Schema(description = "updatedate", example = "2024-05-01 10:00:00")
        @JsonProperty("update_date")
        private String updateDate;

        @Schema(description = "user ID", example = "user_001")
        @JsonProperty("user_id")
        private String userId;

        @Schema(description = "conversationhistorymessagelist")
        private List<Map<String, Object>> messages;
    }

    /**
     * deletesessionrequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "deletesessionrequest")
    public static class SessionDeleteReq implements Serializable {

        @Schema(description = "need todelete session ID list", example = "[\"session_001\", \"session_002\"]")
        private List<String> ids;
    }

    // ========== 3. message/conversation (Completion) related ==========

    /**
     * sendmessagerequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "sendmessagerequest")
    public static class CompletionReq implements Serializable {

        @NotBlank(message = "questioncontentcannot be empty")
        @Schema(description = "userquestion", requiredMode = Schema.RequiredMode.REQUIRED, example = "请介绍one下yous 产品")
        private String question;

        @Schema(description = "YesNousestreamingresponse (SSE)", example = "true")
        @Builder.Default
        private Boolean stream = true;

        @NotBlank(message = "session ID cannot be empty")
        @Schema(description = "session ID (可select，not 传thencreatenewsession)", example = "session_001")
        @JsonProperty("session_id")
        private String sessionId;

        @Schema(description = "YesNodisplayreference", example = "true")
        private Boolean quote;

        @Schema(description = "specifiedretrieve document ID list (逗number分隔)", example = "doc_001,doc_002")
        @JsonProperty("doc_ids")
        private String docIds;

        @Schema(description = "datafilteritemsitem")
        @JsonProperty("metadata_condition")
        private Map<String, Object> metadataCondition;
    }

    /**
     * messageresponse VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "messageresponse VO")
    public static class CompletionVO implements Serializable {

        @Schema(description = "AI 回答content")
        private String answer;

        @Schema(description = "referenceinformation")
        private Reference reference;

        @Schema(description = "session ID", example = "session_001")
        @JsonProperty("session_id")
        private String sessionId;

        @Schema(description = "task ID (used forstreamingresponsetrace)", example = "task_001")
        @JsonProperty("task_id")
        private String taskId;

        /**
         * referenceinformation (retrieve命result)
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "referenceinformation")
        public static class Reference implements Serializable {

            @Schema(description = "命 documentblocklist")
            private List<xiaozhi.modules.knowledge.dto.document.RetrievalDTO.HitVO> chunks;

            @Schema(description = "Document aggregationinformation")
            @JsonProperty("doc_aggs")
            private List<DocAgg> docAggs;
        }

        /**
         * Document aggregationinformation
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "Document aggregationinformation")
        public static class DocAgg implements Serializable {

            @Schema(description = "document ID", example = "doc_001")
            @JsonProperty("doc_id")
            private String docId;

            @Schema(description = "documentname", example = "产品手册.pdf")
            @JsonProperty("doc_name")
            private String docName;

            @Schema(description = "命times", example = "3")
            private Integer count;
        }
    }

    /**
     * 简易Knowledge base VO (used for Assistant list)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "简易Knowledge base VO")
    public static class SimpleDatasetVO implements Serializable {
        @Schema(description = "Knowledge base ID")
        private String id;
        @Schema(description = "Knowledge base name")
        private String name;
        @Schema(description = "avatar")
        private String avatar;
        @Schema(description = "chunkcount")
        @JsonProperty("chunk_num")
        private Integer chunkNum;
    }
}
