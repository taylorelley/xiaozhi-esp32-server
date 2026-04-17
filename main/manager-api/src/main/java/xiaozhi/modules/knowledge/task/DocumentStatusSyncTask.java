package xiaozhi.modules.knowledge.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.modules.knowledge.service.KnowledgeFilesService;

/**
 * Knowledge basedocumentstatussynchronous定时task
 * 
 * 作用：
 * 1. 自动扫描处于 "RUNNING" (parse) status document
 * 2. call RAGFlow interfaceget最newstatus
 * 3. status翻转 (RUNNING -> SUCCESS/FAIL) 时，synchronousupdatedatalibrary
 * 4. [关键] parsesuccess时，补偿updateKnowledge base statisticsinformation (TokenCount)
 */
@Component
@AllArgsConstructor
@Slf4j
public class DocumentStatusSyncTask {

    private final KnowledgeFilesService knowledgeFilesService;

    /**
     * 每 30 secondsexecute一timessynchronous
     * 采用 fixedDelay，ensure上一timesexecute完 30 seconds后才start下一times，prevent积压
     */
    @Scheduled(fixedDelay = 30000)
    public void syncRunningDocuments() {
        try {
            // log.debug("startexecutedocumentstatussynchronoustask...");
            knowledgeFilesService.syncRunningDocuments();
        } catch (Exception e) {
            log.error("documentstatussynchronoustaskexception", e);
        }
    }
}
