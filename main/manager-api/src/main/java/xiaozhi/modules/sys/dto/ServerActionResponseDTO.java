package xiaozhi.modules.sys.dto;

import lombok.Data;
import xiaozhi.modules.sys.enums.ServerActionResponseEnum;

import java.util.Map;

/**
 * serviceend动作response
 */
@Data
public class ServerActionResponseDTO
{
    private ServerActionResponseEnum status;
    private String message;
    private String type;
    private Map<String, Object> content; // after续thisfield可to移除，andthisclassas基class，针forbusinesswrite自己 contenttype
    public static final String DEFAULT_TYPE_FORM_SERVER = "server";

    public static Boolean isSuccess(ServerActionResponseDTO actionResponseDTO) {
        System.out.println(actionResponseDTO);
        if (actionResponseDTO == null) {
            return false;
        }
        if (actionResponseDTO.getStatus() == null || !actionResponseDTO.getStatus().equals(ServerActionResponseEnum.SUCCESS)) {
            return false;
        }
        Object actionType = actionResponseDTO.getContent().get("action");
        if (actionType == null) {
            return false;
        }
        return actionResponseDTO.getType() != null && actionResponseDTO.getType().equals(DEFAULT_TYPE_FORM_SERVER);
    }
}
