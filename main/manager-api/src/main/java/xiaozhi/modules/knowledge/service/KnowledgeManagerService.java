package xiaozhi.modules.knowledge.service;

import java.util.List;

/**
 * Knowledge basemoduledomain codesortservice
 * used forprocesscross KnowledgeBase and KnowledgeFiles  re-miscellaneousbusinessflowprocess，thoroughresolve Service between loopdependencyquestion。
 */
public interface KnowledgeManagerService {

    /**
     * cascadedeleteKnowledge baseanditsbelowalldocument (includethis DB and RAGFlow remotedata)
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
