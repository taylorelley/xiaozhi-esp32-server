package xiaozhi.modules.agent.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import xiaozhi.modules.agent.entity.AgentChatHistoryEntity;

/**
 * {@link AgentChatHistoryEntity} Agent chat historyrecordDaoobject
 *
 * @author Goody
 * @version 1.0, 2025/4/30
 * @since 1.0.0
 */
@Mapper
public interface AiAgentChatHistoryDao extends BaseMapper<AgentChatHistoryEntity> {

    /**
     * according toAgent IDdeletechat历史record
     *
     * @param agentId Agent ID
     */
    void deleteHistoryByAgentId(String agentId);

    /**
     * according toAgent IDdeleteaudioID
     *
     * @param agentId Agent ID
     */
    void deleteAudioIdByAgentId(String agentId);

    /**
     * according toAgent IDget allaudioIDlist
     *
     * @param agentId Agent ID
     * @return audioIDlist
     */
    List<String> getAudioIdsByAgentId(String agentId);

    /**
     * batchdeleteaudio
     *
     * @param audioIds audioIDlist
     */
    void deleteAudioByIds(@Param("audioIds") List<String> audioIds);
}
