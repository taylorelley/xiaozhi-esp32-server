package xiaozhi.modules.voiceclone.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.voiceclone.dto.VoiceCloneDTO;
import xiaozhi.modules.voiceclone.dto.VoiceCloneResponseDTO;
import xiaozhi.modules.voiceclone.entity.VoiceCloneEntity;

/**
 * Voice clonemanagement
 */
public interface VoiceCloneService extends BaseService<VoiceCloneEntity> {

    /**
     * paginationquery
     */
    PageData<VoiceCloneEntity> page(Map<String, Object> params);

    /**
     * saveVoice clone
     */
    void save(VoiceCloneDTO dto);

    /**
     * batchdelete
     */
    void delete(String[] ids);

    /**
     * according toUser IDqueryVoice clonelist
     * 
     * @param userId User ID
     * @return Voice clonelist
     */
    List<VoiceCloneEntity> getByUserId(Long userId);

    /**
     * paginationquerywithModel nameandUsername称 Voice clonelist
     */
    PageData<VoiceCloneResponseDTO> pageWithNames(Map<String, Object> params);

    /**
     * according toIDquerywithModel nameandUsername称 Voice cloneinformation
     */
    VoiceCloneResponseDTO getByIdWithNames(String id);

    /**
     * according toUser IDquerywithModel name Voice clonelist
     */
    List<VoiceCloneResponseDTO> getByUserIdWithNames(Long userId);

    /**
     * uploadaudio file
     */
    void uploadVoice(String id, MultipartFile voiceFile) throws Exception;

    /**
     * updateVoice clonename
     */
    void updateName(String id, String name);

    /**
     * getaudio data
     */
    byte[] getVoiceData(String id);

    /**
     * 克隆audio，call火山引擎performvoice复刻training
     * 
     * @param cloneId voice克隆recordID
     */
    void cloneAudio(String cloneId);
}
