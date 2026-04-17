package xiaozhi.modules.knowledge.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.modules.knowledge.service.KnowledgeFilesService;

/**
 * Knowledge basedocumentstatussynchronous定whentask
 * 
 * 作用：
 * 1. automatic扫描处于 "RUNNING" (parse) status document
 * 2. call RAGFlow interfacegetmostnewstatus
 * 3. status翻转 (RUNNING -> SUCCESS/FAIL) when，synchronousupdatedatalibrary
 * 4. [关key] parsesuccesswhen，补偿updateKnowledge base statisticsinformation (TokenCount)
 */
@Component
@AllArgsConstructor
@Slf4j
public class DocumentStatusSyncTask {

    private final KnowledgeFilesService knowledgeFilesService;

    /**
     * 每 30 secondsexecuteonetimessynchronous
     * 采用 fixedDelay，ensure上onetimesexecute完 30 secondsafteronlystart下onetimes，prevent积压
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
