package xiaozhi.modules.knowledge.dto.file;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * filemanagementaggregation DTO
 * <p>
 * 容class，内含filemoduleallrequest/responseobject 静态内部classdefine。
 * </p>
 */
@Schema(description = "filemanagementaggregation DTO")
public class FileDTO {

    // ========== requestclass ==========

    /**
     * fileuploadrequest (correspondinginterface 1: upload)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "fileuploadrequest")
    public static class UploadReq implements Serializable {

        @NotNull(message = "filecannot be empty")
        @Schema(description = "upload file", requiredMode = Schema.RequiredMode.REQUIRED)
        private MultipartFile file;

        @Schema(description = "父file ID (asemptythenuploadto根目录)", example = "folder_001")
        @JsonProperty("parent_id")
        private String parentId;
    }

    /**
     * new建filerequest (correspondinginterface 2: create)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "new建filerequest")
    public static class CreateReq implements Serializable {

        @NotBlank(message = "filenamecannot be empty")
        @Schema(description = "filename", requiredMode = Schema.RequiredMode.REQUIRED, example = "new建file")
        private String name;

        @Schema(description = "父file ID (asemptythencreatein根目录)", example = "folder_001")
        @JsonProperty("parent_id")
        private String parentId;

        @NotBlank(message = "typecannot be empty")
        @Schema(description = "type: FOLDER", requiredMode = Schema.RequiredMode.REQUIRED, example = "FOLDER")
        @Builder.Default
        private String type = "FOLDER";
    }

    /**
     * renamerequest (correspondinginterface 6: rename)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "renamerequest")
    public static class RenameReq implements Serializable {

        @NotBlank(message = "file ID cannot be empty")
        @Schema(description = "file/file ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "file_001")
        @JsonProperty("file_id")
        private String fileId;

        @NotBlank(message = "newnamecannot be empty")
        @Schema(description = "newname", requiredMode = Schema.RequiredMode.REQUIRED, example = "rename后 file")
        private String name;
    }

    /**
     * moverequest (correspondinginterface 7: move)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "moverequest")
    public static class MoveReq implements Serializable {

        @NotEmpty(message = "sourcefile ID listcannot be empty")
        @Schema(description = "sourcefile/file ID list", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"file_001\", \"file_002\"]")
        @JsonProperty("src_file_ids")
        private List<String> srcFileIds;

        @NotBlank(message = "targetfile ID cannot be empty")
        @Schema(description = "targetfile ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "folder_002")
        @JsonProperty("dest_file_id")
        private String destFileId;
    }

    /**
     * batchdeleterequest (correspondinginterface 8: rm)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "batchdeleterequest")
    public static class RemoveReq implements Serializable {

        @NotEmpty(message = "file ID listcannot be empty")
        @Schema(description = "file/file ID list", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"file_001\", \"file_002\"]")
        @JsonProperty("file_ids")
        private List<String> fileIds;
    }

    /**
     * importKnowledge baserequest (correspondinginterface 9: convert)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "importKnowledge baserequest")
    public static class ConvertReq implements Serializable {

        @NotEmpty(message = "file ID listcannot be empty")
        @Schema(description = "file ID list", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"file_001\", \"file_002\"]")
        @JsonProperty("file_ids")
        private List<String> fileIds;

        @NotEmpty(message = "Knowledge base ID listcannot be empty")
        @Schema(description = "targetKnowledge base ID list", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"kb_001\"]")
        @JsonProperty("kb_ids")
        private List<String> kbIds;
    }

    /**
     * listqueryrequest (correspondinginterface 3: list_files)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "listqueryrequest")
    public static class ListReq implements Serializable {

        @Schema(description = "父file ID (asemptythenquery根目录)", example = "folder_001")
        @JsonProperty("parent_id")
        private String parentId;

        @Schema(description = "keywordsearch", example = "document")
        private String keywords;

        @Schema(description = "page number (from 1 start)", example = "1")
        private Integer page;

        @Schema(description = "per pagecount", example = "30")
        @JsonProperty("page_size")
        private Integer pageSize;

        @Schema(description = "Sort orderfield: create_time / update_time / name / size", example = "create_time")
        private String orderby;

        @Schema(description = "YesNodescending", example = "true")
        private Boolean desc;
    }

    // ========== responseclass ==========

    /**
     * file/filebaseinformation VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "file/filebaseinformation")
    public static class InfoVO implements Serializable {

        @Schema(description = "file/file ID", example = "file_001")
        private String id;

        @Schema(description = "父file ID", example = "folder_001")
        @JsonProperty("parent_id")
        private String parentId;

        @Schema(description = "tenant ID", example = "tenant_001")
        @JsonProperty("tenant_id")
        private String tenantId;

        @Schema(description = "Creator ID", example = "user_001")
        @JsonProperty("created_by")
        private String createdBy;

        @Schema(description = "type: FOLDER / FILE", example = "FOLDER")
        private String type;

        @Schema(description = "name", example = "I file")
        private String name;

        @Schema(description = "pathbit置", example = "/root/folder")
        private String location;

        @Schema(description = "File size (byte)", example = "1024")
        private Long size;

        @Schema(description = "sourcetype", example = "local")
        @JsonProperty("source_type")
        private String sourceType;

        @Schema(description = "Create time (timestamp)", example = "1700000000000")
        @JsonProperty("create_time")
        private Long createTime;

        @Schema(description = "createdate (format)", example = "2024-01-15 10:30:00")
        @JsonProperty("create_date")
        private String createDate;

        @Schema(description = "updatetime (timestamp)", example = "1700000001000")
        @JsonProperty("update_time")
        private Long updateTime;

        @Schema(description = "updatedate (format)", example = "2024-01-15 11:00:00")
        @JsonProperty("update_date")
        private String updateDate;

        @Schema(description = "fileextension", example = "pdf")
        private String extension;
    }

    /**
     * listresponse VO (correspondinginterface 3: list_files)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "filelistresponse")
    public static class ListVO implements Serializable {

        @Schema(description = "totalrecordnumber", example = "100")
        private Long total;

        @Schema(description = "current父fileinformation")
        @JsonProperty("parent_folder")
        private InfoVO parentFolder;

        @Schema(description = "file/filelist")
        private List<InfoVO> files;

        @Schema(description = "面包屑导航path")
        private List<InfoVO> breadcrumb;
    }

    /**
     * convertresultitem VO (correspondinginterface 9: convert)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "fileconvertresultitem")
    public static class ConvertVO implements Serializable {

        @Schema(description = "convertrecord ID", example = "convert_001")
        private String id;

        @Schema(description = "sourcefile ID", example = "file_001")
        @JsonProperty("file_id")
        private String fileId;

        @Schema(description = "targetdocument ID", example = "doc_001")
        @JsonProperty("document_id")
        private String documentId;

        @Schema(description = "Create time (timestamp)", example = "1700000000000")
        @JsonProperty("create_time")
        private Long createTime;

        @Schema(description = "createdate (format)", example = "2024-01-15 10:30:00")
        @JsonProperty("create_date")
        private String createDate;

        @Schema(description = "updatetime (timestamp)", example = "1700000001000")
        @JsonProperty("update_time")
        private Long updateTime;

        @Schema(description = "updatedate (format)", example = "2024-01-15 11:00:00")
        @JsonProperty("update_date")
        private String updateDate;
    }

    /**
     * convertstatus VO (correspondinginterface 10: get_convert_status)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "fileconvertstatus")
    public static class ConvertStatusVO implements Serializable {

        @Schema(description = "convertstatus: pending / processing / completed / failed", example = "completed")
        private String status;

        @Schema(description = "convert进度 (0.0 - 1.0)", example = "1.0")
        private Float progress;

        @Schema(description = "statusmessage", example = "convertcomplete")
        private String message;
    }

    /**
     * 面包屑 VO (correspondinginterface 12: all_parent_folder)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "面包屑导航 (all父file)")
    public static class BreadcrumbVO implements Serializable {

        @Schema(description = "父filelist (from根tocurrent path)")
        @JsonProperty("parent_folders")
        private List<InfoVO> parentFolders;
    }

    /**
     * 根目录information VO (correspondinginterface 10: get_root_folder)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "根目录information")
    public static class RootFolderVO implements Serializable {

        @Schema(description = "根fileinformation")
        @JsonProperty("root_folder")
        private InfoVO rootFolder;
    }

    /**
     * 父目录information VO (correspondinginterface 11: get_parent_folder)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "父目录information")
    public static class ParentFolderVO implements Serializable {

        @Schema(description = "父fileinformation")
        @JsonProperty("parent_folder")
        private InfoVO parentFolder;
    }
}
