package xiaozhi.modules.knowledge.service;

import java.util.List;

/**
 * Knowledge basemodule领域编排service
 * used forprocess跨 KnowledgeBase and KnowledgeFiles  复杂business流程，彻底解决 Service 间 循环依赖question。
 */
public interface KnowledgeManagerService {

    /**
     * cascadedeleteKnowledge base及其下属alldocument (包括this地 DB and RAGFlow remotedata)
     * 
     * @param datasetId Knowledge base ID
     */
    void deleteDatasetWithFiles(String datasetId);

    /**
     * batchcascadedeleteKnowledge base
     * 
     * @param datasetIds Knowledge base ID list
     */
    void batchDeleteDatasetsWithFiles(List<String> datasetIds);
}
