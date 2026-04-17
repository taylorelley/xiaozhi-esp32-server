package xiaozhi.modules.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;

import xiaozhi.modules.agent.entity.AgentChatAudioEntity;

/**
 * agentchataudio datatableprocessservice
 *
 * @author Goody
 * @version 1.0, 2025/5/8
 * @since 1.0.0
 */
public interface AgentChatAudioService extends IService<AgentChatAudioEntity> {
    /**
     * saveaudio data
     *
     * @param audioData audio data
     * @return audioID
     */
    String saveAudio(byte[] audioData);

    /**
     * getaudio data
     *
     * @param audioId audioID
     * @return audio data
     */
    byte[] getAudio(String audioId);
}
