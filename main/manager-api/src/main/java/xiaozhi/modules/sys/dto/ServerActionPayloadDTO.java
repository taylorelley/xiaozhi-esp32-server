package xiaozhi.modules.sys.dto;

import lombok.Data;
import xiaozhi.modules.sys.enums.ServerActionEnum;

import java.util.Map;

/**
 * serviceendactionDTO
 */
@Data
public class ServerActionPayloadDTO
{
    /**
    * type（intelligentcontrolconsolesendgoserviceend allYesserver）
    */
    private String type;
    /**
    * action
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
    // privatehas
    private ServerActionPayloadDTO() {}
}
