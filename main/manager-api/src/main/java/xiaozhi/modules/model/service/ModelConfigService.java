package xiaozhi.modules.model.service;

import java.util.List;
import java.util.Map;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.model.dto.LlmModelBasicInfoDTO;
import xiaozhi.modules.model.dto.ModelBasicInfoDTO;
import xiaozhi.modules.model.dto.ModelConfigBodyDTO;
import xiaozhi.modules.model.dto.ModelConfigDTO;
import xiaozhi.modules.model.entity.ModelConfigEntity;

public interface ModelConfigService extends BaseService<ModelConfigEntity> {

    List<ModelBasicInfoDTO> getModelCodeList(String modelType, String modelName);

    List<LlmModelBasicInfoDTO> getLlmModelCodeList(String modelName);

    PageData<ModelConfigDTO> getPageList(String modelType, String modelName, String page, String limit);

    ModelConfigDTO add(String modelType, String provideCode, ModelConfigBodyDTO modelConfigBodyDTO);

    ModelConfigDTO edit(String modelType, String provideCode, String id, ModelConfigBodyDTO modelConfigBodyDTO);

    void delete(String id);

    /**
     * according toIDgetModel name
     * 
     * @param id Model ID
     * @return Model name
     */
    String getModelNameById(String id);

    /**
     * according toIDgetModel configuration
     * 
     * @param id Model ID
     * @return Model configurationentity
     */
    ModelConfigEntity getModelByIdFromCache(String id);

    /**
     * setdefaultmodel
     *
     * @param modelType Model type
     * @param isDefault YesNodefault（1:Yes，0:No）
     */
    void setDefaultModel(String modelType, int isDefault);

    /**
     * getmatchingitemsitem TTSplatformlist
     *
     * @return TTSplatformlist(idandmodelName)
     */
    List<Map<String, Object>> getTtsPlatformList();

    /**
     * according toModel typeget allenable Model configuration
     *
     * @param modelType Model type（For example: LLM, TTS, ASRetc.）
     * @return enable Model configurationlist
     */
    List<ModelConfigEntity> getEnabledModelsByType(String modelType);

    /**
     * Find a model by its modelType and modelCode (returns null if not found).
     */
    ModelConfigEntity getByTypeAndCode(String modelType, String modelCode);
}
