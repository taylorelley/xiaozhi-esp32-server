package xiaozhi.modules.sys.dto;

import lombok.Data;
import xiaozhi.modules.sys.enums.ServerActionEnum;

import java.util.Map;

/**
 * serviceend动作DTO
 */
@Data
public class ServerActionPayloadDTO
{
    /**
    * type（智控台发往serviceend allYesserver）
    */
    private String type;
    /**
    * 动作
    */
    private ServerActionEnum action;
    /**
    * content
    */
    private Map<String, Object> content;

    public static ServerActionPayloadDTO build(ServerActionEnum action, Map<String, Object> content) {
        ServerActionPayloadDTO serverActionPayloadDTO = new ServerActionPayloadDTO();
        serverActionPayloadDTO.setAction(action);
        serverActionPayloadDTO.setContent(content);
        serverActionPayloadDTO.setType("server");
        return serverActionPayloadDTO;
    }
    // 私has化
    private ServerActionPayloadDTO() {}
}
