package xiaozhi.modules.knowledge.service;

import java.util.List;
import java.util.Map;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.knowledge.dto.KnowledgeBaseDTO;
import xiaozhi.modules.knowledge.entity.KnowledgeBaseEntity;
import xiaozhi.modules.model.entity.ModelConfigEntity;

/**
 * Knowledge baseKnowledge baseserviceinterface
 */
public interface KnowledgeBaseService extends BaseService<KnowledgeBaseEntity> {

    /**
     * paginationqueryKnowledge baselist
     * 
     * @param knowledgeBaseDTO queryitems件
     * @param page             page number
     * @param limit            per pagecount
     * @return paginationdata
     */
    PageData<KnowledgeBaseDTO> getPageList(KnowledgeBaseDTO knowledgeBaseDTO, Integer page, Integer limit);

    /**
     * according toIDgetKnowledge basedetails
     * 
     * @param id Knowledge baseID
     * @return Knowledge basedetails
     */
    KnowledgeBaseDTO getById(String id);

    /**
     * addKnowledge base
     * 
     * @param knowledgeBaseDTO Knowledge baseinformation
     * @return add Knowledge base
     */
    KnowledgeBaseDTO save(KnowledgeBaseDTO knowledgeBaseDTO);

    /**
     * updateKnowledge base
     * 
     * @param knowledgeBaseDTO Knowledge baseinformation
     * @return update Knowledge base
     */
    KnowledgeBaseDTO update(KnowledgeBaseDTO knowledgeBaseDTO);

    /**
     * according toKnowledge baseIDqueryKnowledge base
     * 
     * @param datasetId Knowledge baseID
     * @return Knowledge basedetails
     */
    KnowledgeBaseDTO getByDatasetId(String datasetId);

    /**
     * according toKnowledge baseIDcollectionqueryKnowledge base
     *
     * @param datasetIdList Knowledge baseIDcollection
     * @return Knowledge basedetails
     */
    List<KnowledgeBaseDTO> getByDatasetIdList(List<String> datasetIdList);

    /**
     * according toKnowledge baseIDdeleteKnowledge base
     * 
     * @param datasetId Knowledge baseID
     */
    void deleteByDatasetId(String datasetId);

    /**
     * getRAGconfigurationinformation
     * 
     * @param ragModelId RAGModel configurationID
     * @return RAGconfigurationinformation
     */
    Map<String, Object> getRAGConfig(String ragModelId);

    /**
     * according toKnowledge baseIDgetcorresponding RAGconfiguration
     * 
     * @param datasetId Knowledge baseID
     * @return RAGconfiguration
     */
    Map<String, Object> getRAGConfigByDatasetId(String datasetId);

    /**
     * getRAGmodellist
     * 
     * @return RAGmodellist
     */
    List<ModelConfigEntity> getRAGModels();

    /**
     * updateKnowledge basestatisticsinformation (used forisfileservicecallback)
     * 
     * @param datasetId  Knowledge baseID
     * @param docDelta   documentnumber增量
     * @param chunkDelta chunknumber增量
     * @param tokenDelta Tokennumber增量
     */
    void updateStatistics(String datasetId, Integer docDelta, Long chunkDelta, Long tokenDelta);
}