package xiaozhi.modules.agent.service;

import java.util.List;

import xiaozhi.modules.agent.dto.AgentVoicePrintSaveDTO;
import xiaozhi.modules.agent.dto.AgentVoicePrintUpdateDTO;
import xiaozhi.modules.agent.vo.AgentVoicePrintVO;

/**
 * agentvoiceprintprocessservice
 *
 * @author zjy
 */
public interface AgentVoicePrintService {
    /**
     * addagentnew voiceprint
     *
     * @param dto saveagentvoiceprint data
     * @return T:success F：failed
     */
    boolean insert(AgentVoicePrintSaveDTO dto);

    /**
     * deleteagent point voiceprint
     *
     * @param userId       current logged-in userid
     * @param voicePrintId voiceprintid
     * @return YesNosuccess T:success F：failed
     */
    boolean delete(Long userId, String voicePrintId);

    /**
     * getspecifiedagent allvoiceprintdata
     *
     * @param userId  current logged-in userid
     * @param agentId agentid
     * @return voiceprintdatacollection
     */
    List<AgentVoicePrintVO> list(Long userId, String agentId);

    /**
     * updateagent point voiceprintdata
     *
     * @param userId current logged-in userid
     * @param dto    update voiceprint data
     * @return YesNosuccess T:success F：failed
     */
    boolean update(Long userId, AgentVoicePrintUpdateDTO dto);

}
