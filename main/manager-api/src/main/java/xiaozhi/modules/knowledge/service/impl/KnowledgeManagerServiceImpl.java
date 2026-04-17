package xiaozhi.modules.knowledge.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.modules.knowledge.service.KnowledgeBaseService;
import xiaozhi.modules.knowledge.service.KnowledgeFilesService;
import xiaozhi.modules.knowledge.service.KnowledgeManagerService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeManagerServiceImpl implements KnowledgeManagerService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeFilesService knowledgeFilesService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDatasetWithFiles(String datasetId) {
        log.info("=== cascadedeletestart: datasetId={} ===", datasetId);

        // 1. firstcallfileservice，clean upthisdatacollectionbelow alldocumentrecord (contain RAGFlow end)
        log.info("Step 1: clean upassociateddocument...");
        knowledgeFilesService.deleteDocumentsByDatasetId(datasetId);

        // 2. againcallKnowledge baseservice，fully unregisterdatacollection (contain RAGFlow end)
        log.info("Step 2: Delete datacollectionmain...");
        knowledgeBaseService.deleteByDatasetId(datasetId);

        log.info("=== cascadedeletesuccess: datasetId={} ===", datasetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteDatasetsWithFiles(List<String> datasetIds) {
        if (datasetIds == null || datasetIds.isEmpty())
            return;
        log.info("=== batchcascadedeletestart: count={} ===", datasetIds.size());
        for (String id : datasetIds) {
            deleteDatasetWithFiles(id);
        }
    }
}
