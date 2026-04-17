package xiaozhi.modules.knowledge.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.modules.knowledge.service.KnowledgeFilesService;

/**
 * Knowledge basedocumentstatussynchronouswhentask
 * 
 * operateuse：
 * 1. automaticscanplaceto "RUNNING" (parse) status document
 * 2. call RAGFlow interfacegetmostnewstatus
 * 3. statusturnconvert (RUNNING -> SUCCESS/FAIL) when，synchronousupdatedatalibrary
 * 4. [relatedkey] parsesuccesswhen，compensateupdateKnowledge base statisticsinformation (TokenCount)
 */
@Component
@AllArgsConstructor
@Slf4j
public class DocumentStatusSyncTask {

    private final KnowledgeFilesService knowledgeFilesService;

    /**
     * every 30 secondsexecuteonetimessynchronous
     * collectuse fixedDelay，ensureuponetimesexecutecomplete 30 secondsafteronlystartbelowonetimes，preventbacklog
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
