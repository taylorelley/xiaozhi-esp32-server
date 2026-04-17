package xiaozhi.modules.timbre.service;

import java.util.List;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.model.dto.VoiceDTO;
import xiaozhi.modules.timbre.dto.TimbreDataDTO;
import xiaozhi.modules.timbre.dto.TimbrePageDTO;
import xiaozhi.modules.timbre.entity.TimbreEntity;
import xiaozhi.modules.timbre.vo.TimbreDetailsVO;

/**
 * voice business层 define
 * 
 * @author zjy
 * @since 2025-3-21
 */
public interface TimbreService extends BaseService<TimbreEntity> {
    /**
     * paginationgetvoicespecifiedtts 下 voice
     * 
     * @param dto paginationfindparameter
     * @return voicelistpaginationdata
     */
    PageData<TimbreDetailsVO> page(TimbrePageDTO dto);

    /**
     * getvoicespecifiedid detailsinformation
     * 
     * @param timbreId voicetableid
     * @return voiceinformation
     */
    TimbreDetailsVO get(String timbreId);

    /**
     * savevoiceinformation
     * 
     * @param dto needSave data
     */
    void save(TimbreDataDTO dto);

    /**
     * savevoiceinformation
     * 
     * @param timbreId needupdate id
     * @param dto      needupdate data
     */
    void update(String timbreId, TimbreDataDTO dto);

    /**
     * batchdeletevoice
     * 
     * @param ids needisdelete voiceidlist
     */
    void delete(String[] ids);

    List<VoiceDTO> getVoiceNames(String ttsModelId, String voiceName);

    /**
     * according toIDgetvoicename
     * 
     * @param id Voice ID
     * @return voicename
     */
    String getTimbreNameById(String id);

    /**
     * according tovoicecodegetvoiceinformation
     * 
     * @param ttsModelId voiceModel ID
     * @param voiceCode  voicecode
     * @return voiceinformation
     */
    VoiceDTO getByVoiceCode(String ttsModelId, String voiceCode);
}