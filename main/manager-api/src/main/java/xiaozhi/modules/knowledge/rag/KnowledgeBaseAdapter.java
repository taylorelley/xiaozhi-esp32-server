package xiaozhi.modules.knowledge.rag;

import java.util.List;
import java.util.Map;

import xiaozhi.modules.knowledge.dto.dataset.DatasetDTO;

import xiaozhi.common.page.PageData;
import xiaozhi.modules.knowledge.dto.KnowledgeFilesDTO;
import xiaozhi.modules.knowledge.dto.document.DocumentDTO;
import xiaozhi.modules.knowledge.dto.document.ChunkDTO;
import xiaozhi.modules.knowledge.dto.document.RetrievalDTO;
import java.util.function.Consumer;

/**
 * Knowledge baseAPIadapterabstractbaseclass
 * defineuse Knowledge baseoperationinterface，supportmultiplekindafterendAPIimplement
 */
public abstract class KnowledgeBaseAdapter {

        /**
         * getadaptertypeidentifier
         * 
         * @return adaptertype（For example: ragflow, milvus, pineconeetc.）
         */
        public abstract String getAdapterType();

        /**
         * initializeadapterconfiguration
         * 
         * @param config configurationparameter
         */
        public abstract void initialize(Map<String, Object> config);

        /**
         * verificationconfigurationYesNovalid
         * 
         * @param config configurationparameter
         * @return verificationresult
         */
        public abstract boolean validateConfig(Map<String, Object> config);

        /**
         * paginationquerydocumentlist
         * 
         * @param datasetId   Knowledge baseID
         * @param queryParams queryparameter
         * @param page        page number
         * @param limit       per pagecount
         * @return paginationdata
         */
        public abstract PageData<KnowledgeFilesDTO> getDocumentList(String datasetId,
                        DocumentDTO.ListReq req);

        /**
         * according todocumentIDgetdocumentdetails
         * 
         * @param datasetId  Knowledge baseID
         * @param documentId documentID
         * @return documentdetails (strongtype InfoVO)
         */
        public abstract DocumentDTO.InfoVO getDocumentById(String datasetId, String documentId);

        /**
         * uploaddocumenttoKnowledge base
         * 
         * @param req uploadrequestparameter
         * @return upload documentinformation
         */
        public abstract KnowledgeFilesDTO uploadDocument(DocumentDTO.UploadReq req);

        /**
         * according tostatuspaginationquerydocumentlist
         * 
         * @param datasetId Knowledge baseID
         * @param status    Document parsingstatus
         * @param page      page number
         * @param limit     per pagecount
         * @return paginationdata
         */
        public abstract PageData<KnowledgeFilesDTO> getDocumentListByStatus(String datasetId,
                        Integer status,
                        Integer page,
                        Integer limit);

        /**
         * deletedocument (supportbatchdelete)
         * 
         * @param datasetId Knowledge baseID
         * @param req       containdocumentIDlist requestobject
         */
        public abstract void deleteDocument(String datasetId, DocumentDTO.BatchIdReq req);

        /**
         * parsedocument（chunk）
         * 
         * @param datasetId   Knowledge baseID
         * @param documentIds documentIDlist
         * @return parseresult
         */
        public abstract boolean parseDocuments(String datasetId, List<String> documentIds);

        /**
         * listspecifieddocument slice
         * 
         * @param datasetId  Knowledge baseID
         * @param documentId documentID
         * @param req        listrequestparameter (pagination、keywordetc.)
         * @return slicelistVO
         */
        public abstract ChunkDTO.ListVO listChunks(String datasetId,
                        String documentId,
                        ChunkDTO.ListReq req);

        /**
         * recalltest - fromKnowledge baseretrieverelatedslice
         * 
         * @param req retrievetestrequestparameter
         * @return recalltestresult
         */
        public abstract RetrievalDTO.ResultVO retrievalTest(
                        RetrievalDTO.TestReq req);

        /**
         * testconnection
         * 
         * @return connectiontestresult
         */
        public abstract boolean testConnection();

        /**
         * getadapterstatusinformation
         * 
         * @return statusinformation
         */
        public abstract Map<String, Object> getStatus();

        /**
         * getsupport configurationparameter
         * 
         * @return configurationparameterDescription
         */
        public abstract Map<String, Object> getSupportedConfig();

        /**
         * getdefaultconfiguration
         * 
         * @return defaultconfiguration
         */
        public abstract Map<String, Object> getDefaultConfig();

        /**
         * createdatacollection
         * 
         * @param req createparameter
         * @return datacollectiondetails
         */
        public abstract DatasetDTO.InfoVO createDataset(DatasetDTO.CreateReq req);

        /**
         * updatedatacollection
         * 
         * @param datasetId datacollectionID
         * @param req       updateparameter
         * @return datacollectiondetails
         */
        public abstract DatasetDTO.InfoVO updateDataset(String datasetId, DatasetDTO.UpdateReq req);

        /**
         * Delete datacollection
         * 
         * @param req deleterequestparameter（containIDlist）
         * @return batchoperationresult
         */
        public abstract DatasetDTO.BatchOperationVO deleteDataset(DatasetDTO.BatchIdReq req);

        /**
         * getdatacollection documentcount
         * 
         * @param datasetId datacollectionID
         * @return documentcount
         */
        public abstract Integer getDocumentCount(String datasetId);

        /**
         * sendstreamingrequest (SSE)
         * 
         * @param endpoint APIendpoint
         * @param body     request
         * @param onData   datacallback
         */
        public abstract void postStream(String endpoint, Object body, Consumer<String> onData);

        /**
         * SearchBot ask
         *
         * @param config RAGconfiguration
         * @param body   request
         * @param onData datacallback
         * @return responseobject
         */
        public abstract Object postSearchBotAsk(Map<String, Object> config, Object body,
                        Consumer<String> onData);

        /**
         * AgentBot conversation
         *
         * @param config  RAGconfiguration
         * @param agentId Agent ID
         * @param body    request
         * @param onData  datacallback
         */
        public abstract void postAgentBotCompletion(Map<String, Object> config, String agentId, Object body,
                        Consumer<String> onData);
}