package xiaozhi.modules.agent.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import xiaozhi.common.page.PageData;
import xiaozhi.modules.agent.dto.AgentChatHistoryDTO;
import xiaozhi.modules.agent.dto.AgentChatSessionDTO;
import xiaozhi.modules.agent.entity.AgentChatHistoryEntity;
import xiaozhi.modules.agent.vo.AgentChatHistoryUserVO;

/**
 * agentChat historytableprocessservice
 *
 * @author Goody
 * @version 1.0, 2025/4/30
 * @since 1.0.0
 */
public interface AgentChatHistoryService extends IService<AgentChatHistoryEntity> {

    /**
     * according toAgent IDgetsessionlist
     *
     * @param params queryparameter，containagentId、page、limit
     * @return pagination sessionlist
     */
    PageData<AgentChatSessionDTO> getSessionListByAgentId(Map<String, Object> params);

    /**
     * according toSession IDgetChat historylist
     *
     * @param agentId   Agent ID
     * @param sessionId Session ID
     * @return Chat historylist
     */
    List<AgentChatHistoryDTO> getChatHistoryBySessionId(String agentId, String sessionId);

    /**
     * according toAgent IDdeleteChat history
     *
     * @param agentId     Agent ID
     * @param deleteAudio YesNodeleteaudio
     * @param deleteText  YesNodeletetext
     */
    void deleteByAgentId(String agentId, Boolean deleteAudio, Boolean deleteText);

    /**
     * according toAgent IDgetmost近50itemsuser Chat historydata（withaudio data）
     *
     * @param agentId agentid
     * @return Chat historylist（onlyhasuser）
     */
    List<AgentChatHistoryUserVO> getRecentlyFiftyByAgentId(String agentId);

    /**
     * according toaudio dataIDgetchatcontent
     *
     * @param audioId audioid
     * @return chatcontent
     */
    String getContentByAudioId(String audioId);


    /**
     * querythisaudioidYesNobelongs tothisagent
     *
     * @param audioId audioid
     * @param agentId audioid
     * @return T：belongs to F：not belongs to
     */
    boolean isAudioOwnedByAgent(String audioId,String agentId);
}
