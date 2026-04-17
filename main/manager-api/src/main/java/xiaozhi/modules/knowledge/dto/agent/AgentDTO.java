package xiaozhi.modules.knowledge.dto.agent;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

@Schema(description = "agent (Agent) managementaggregation DTO")
public class AgentDTO {

    // ========== 1. Agent management (CRUD) - corresponding RAGFlow_Agentinterfacedetails ==========
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Agent createrequest")
    public static class CreateReq implements Serializable {
        @Schema(description = "Agent title", requiredMode = Schema.RequiredMode.REQUIRED, example = "My Agent")
        @NotBlank(message = "Agent titlecannot be empty")
        @JsonProperty("title")
        private String title;

        @Schema(description = "DSL define (canvas JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "DSL definecannot be empty")
        @JsonProperty("dsl")
        private Map<String, Object> dsl;

        @Schema(description = "Description", example = "thisYesonetest Agent")
        @JsonProperty("description")
        private String description;

        @Schema(description = "avatar URL", example = "http://example.com/avatar.png")
        @JsonProperty("avatar")
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Agent updaterequest")
    public static class UpdateReq implements Serializable {
        @Schema(description = "Agent title", example = "Updated Agent")
        @JsonProperty("title")
        private String title;

        @Schema(description = "DSL define (canvas JSON)")
        @JsonProperty("dsl")
        private Map<String, Object> dsl;

        @Schema(description = "Description")
        @JsonProperty("description")
        private String description;

        @Schema(description = "avatar URL")
        @JsonProperty("avatar")
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Agent listrequest")
    public static class ListReq implements Serializable {
        @Schema(description = "page number", defaultValue = "1")
        @JsonProperty("page")
        @Builder.Default
        private Integer page = 1;

        @Schema(description = "per pagelargesmall", defaultValue = "10")
        @JsonProperty("page_size")
        @Builder.Default
        private Integer pageSize = 10;

        @Schema(description = "Sort orderfield", defaultValue = "update_time")
        @JsonProperty("orderby")
        @Builder.Default
        private String orderby = "update_time";

        @Schema(description = "YesNodescending", defaultValue = "true")
        @JsonProperty("desc")
        @Builder.Default
        private Boolean desc = true;

        @Schema(description = "Agent ID filter")
        @JsonProperty("id")
        private String id;

        @Schema(description = "titlefuzzysearch")
        @JsonProperty("title")
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Agent responseobject")
    public static class AgentVO implements Serializable {
        @Schema(description = "Agent ID")
        @JsonProperty("id")
        private String id;

        @Schema(description = "title")
        @JsonProperty("title")
        private String title;

        @Schema(description = "Description")
        @JsonProperty("description")
        private String description;

        @Schema(description = "avatar")
        @JsonProperty("avatar")
        private String avatar;

        @Schema(description = "DSL define")
        @JsonProperty("dsl")
        private Map<String, Object> dsl;

        @Schema(description = "Creator ID")
        @JsonProperty("user_id")
        private String userId;

        @Schema(description = "canvasCategory")
        @JsonProperty("canvas_category")
        private String canvasCategory;

        @Schema(description = "Create time (timestamp)")
        @JsonProperty("create_time")
        private Long createTime;

        @Schema(description = "updatetime (timestamp)")
        @JsonProperty("update_time")
        private Long updateTime;
    }

    // ========== 2. Webhook adjusttryandtrace - corresponding RAGFlow_Agentinterfacedetails ==========
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Webhook triggersendrequest (parameterdynamic)")
    public static class WebhookTriggerReq implements Serializable {
        @Schema(description = "inputchangeamount", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "inputchangeamountcannot be empty")
        @JsonProperty("inputs")
        private Map<String, Object> inputs;

        @Schema(description = "queryword", example = "Hello")
        @JsonProperty("query")
        private String query;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Webhook tracerequest")
    public static class WebhookTraceReq implements Serializable {
        @Schema(description = "timestampcursor", example = "1700000000.0")
        @JsonProperty("since_ts")
        private Double sinceTs;

        @Schema(description = "Webhook ID")
        @JsonProperty("webhook_id")
        private String webhookId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Webhook traceresponse")
    public static class WebhookTraceVO implements Serializable {
        @Schema(description = "Webhook ID")
        @JsonProperty("webhook_id")
        private String webhookId;

        @Schema(description = "YesNoend")
        @JsonProperty("finished")
        private Boolean finished;

        @Schema(description = "belowonetimesquery timestampcursor")
        @JsonProperty("next_since_ts")
        private Double nextSinceTs;

        @Schema(description = "eventitemlist")
        @JsonProperty("events")
        private List<TraceEvent> events;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "traceeventitemitem")
        public static class TraceEvent implements Serializable {
            @Schema(description = "timestamp")
            @JsonProperty("ts")
            private Double ts;

            @Schema(description = "eventitemtype")
            @JsonProperty("event")
            private String event;

            @Schema(description = "eventitemdata")
            @JsonProperty("data")
            private Object data;
        }
    }

    // ========== 3. Agent session (Session) - corresponding RAGFlow_Agent_Difyinterfacedetails ==========
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Session createrequest")
    public static class SessionCreateReq implements Serializable {
        @Schema(description = "user ID")
        @JsonProperty("user_id")
        private String userId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Session listrequest")
    public static class SessionListReq implements Serializable {
        @Schema(description = "page number", defaultValue = "1")
        @JsonProperty("page")
        @Builder.Default
        private Integer page = 1;

        @Schema(description = "per pagelargesmall", defaultValue = "10")
        @JsonProperty("page_size")
        @Builder.Default
        private Integer pageSize = 10;

        @Schema(description = "Sort orderfield", defaultValue = "create_time")
        @JsonProperty("orderby")
        @Builder.Default
        private String orderby = "create_time";

        @Schema(description = "YesNodescending", defaultValue = "true")
        @JsonProperty("desc")
        @Builder.Default
        private Boolean desc = true;

        @Schema(description = "Session ID")
        @JsonProperty("id")
        private String id;

        @Schema(description = "user ID")
        @JsonProperty("user_id")
        private String userId;

        @Schema(description = "YesNoreturn DSL")
        @JsonProperty("dsl")
        @Builder.Default
        private Boolean dsl = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Session batchdeleterequest")
    public static class SessionBatchDeleteReq implements Serializable {
        @Schema(description = "session ID list", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("ids")
        @NotEmpty(message = "IDlistcannot be empty")
        private List<String> ids;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Session responseobject")
    public static class SessionVO implements Serializable {
        @Schema(description = "Session ID")
        @JsonProperty("id")
        private String id;

        @Schema(description = "Agent ID")
        @JsonProperty("agent_id")
        private String agentId;

        @Schema(description = "user ID")
        @JsonProperty("user_id")
        private String userId;

        @Schema(description = "source")
        @JsonProperty("source")
        private String source;

        @Schema(description = "DSL define")
        @JsonProperty("dsl")
        private Map<String, Object> dsl;

        @Schema(description = "messagelist")
        @JsonProperty("messages")
        private List<Map<String, Object>> messages;
    }

    // ========== 4. Agent conversation (Completion) - corresponding RAGFlow_Agent_Difyinterfacedetails ==========
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Completion conversationrequest")
    public static class CompletionReq implements Serializable {
        @Schema(description = "session ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "session ID cannot be empty")
        @JsonProperty("session_id")
        private String sessionId;

        @Schema(description = "userquestion")
        @JsonProperty("question")
        private String question;

        @Schema(description = "YesNostreamingreturn", defaultValue = "true")
        @JsonProperty("stream")
        @Builder.Default
        private Boolean stream = true;

        @Schema(description = "YesNoreturntraceinformation", defaultValue = "false")
        @JsonProperty("return_trace")
        @Builder.Default
        private Boolean returnTrace = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Completion conversationresponse")
    public static class CompletionVO implements Serializable {
        @Schema(description = "session ID")
        @JsonProperty("id")
        private String id;

        @Schema(description = "returnre-content")
        @JsonProperty("content")
        private String content;

        @Schema(description = "referencesource")
        @JsonProperty("reference")
        private Map<String, Object> reference;

        @Schema(description = "traceinformation")
        @JsonProperty("trace")
        private List<Object> trace;
    }

    // ========== 5. Dify compatibleretrieve - corresponding RAGFlow_Agent_Difyinterfacedetails ==========
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dify compatibleretrieverequest")
    public static class DifyRetrievalReq implements Serializable {
        @Schema(description = "Knowledge base ID")
        @JsonProperty("knowledge_id")
        private String knowledgeId;

        @Schema(description = "queryword")
        @JsonProperty("query")
        private String query;

        @Schema(description = "retrieveset")
        @JsonProperty("retrieval_setting")
        private Map<String, Object> retrievalSetting;

        @Schema(description = "datafilteritemsitem")
        @JsonProperty("metadata_condition")
        private Map<String, Object> metadataCondition;

        @Schema(description = "YesNouseknowledge graph")
        @JsonProperty("use_kg")
        private Boolean useKg;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dify compatibleretrieveresponse")
    public static class DifyRetrievalVO implements Serializable {
        @Schema(description = "retrieveresultlist")
        @JsonProperty("records")
        private List<Record> records;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "retrieverecord")
        public static class Record implements Serializable {
            @Schema(description = "content")
            @JsonProperty("content")
            private String content;

            @Schema(description = "similarnumber")
            @JsonProperty("score")
            private Double score;

            @Schema(description = "title")
            @JsonProperty("title")
            private String title;

            @Schema(description = "data")
            @JsonProperty("metadata")
            private Map<String, Object> metadata;
        }
    }
}
