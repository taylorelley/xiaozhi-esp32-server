package xiaozhi.modules.knowledge.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import xiaozhi.common.page.PageData;
import xiaozhi.modules.knowledge.dto.KnowledgeFilesDTO;
import xiaozhi.modules.knowledge.dto.document.ChunkDTO;
import xiaozhi.modules.knowledge.dto.document.RetrievalDTO;
import xiaozhi.modules.knowledge.dto.document.DocumentDTO;

/**
 * Knowledge basedocumentserviceinterface
 */
public interface KnowledgeFilesService {

        /**
         * paginationquerydocumentlist
         * 
         * @param knowledgeFilesDTO queryitems件
         * @param page              page number
         * @param limit             per pagecount
         * @return paginationdata
         */
        PageData<KnowledgeFilesDTO> getPageList(KnowledgeFilesDTO knowledgeFilesDTO, Integer page, Integer limit);

        /**
         * according todocumentIDandKnowledge baseIDgetdocumentdetails
         * 
         * @param documentId documentID
         * @param datasetId  Knowledge baseID
         * @return documentdetails (强type InfoVO)
         */
        DocumentDTO.InfoVO getByDocumentId(String documentId, String datasetId);

        /**
         * uploaddocumenttoKnowledge base
         * 
         * @param datasetId    Knowledge baseID
         * @param file         upload file
         * @param name         documentname
         * @param metaFields   datafield
         * @param chunkMethod  chunk方法
         * @param parserConfig parserconfiguration
         * @return upload documentinformation
         */
        KnowledgeFilesDTO uploadDocument(String datasetId, MultipartFile file, String name,
                        Map<String, Object> metaFields, String chunkMethod,
                        Map<String, Object> parserConfig);

        /**
         * batchdeletedocument
         * 
         * @param datasetId Knowledge baseID
         * @param req       deleterequestparameter (含documentIDlist)
         */
        void deleteDocuments(String datasetId, DocumentDTO.BatchIdReq req);

        /**
         * getRAGconfigurationinformation
         * 
         * @param ragModelId RAGModel configurationID
         * @return RAGconfigurationinformation
         */
        Map<String, Object> getRAGConfig(String ragModelId);

        /**
         * parsedocument（chunk）
         * 
         * @param datasetId   Knowledge baseID
         * @param documentIds documentIDlist
         * @return parseresult
         */
        boolean parseDocuments(String datasetId, List<String> documentIds);

        /**
         * 列出specifieddocument slice
         * 
         * @param datasetId  Knowledge baseID
         * @param documentId documentID
         * @param req        slicelistrequestparameter
         * @return slicelistinformation
         */
        ChunkDTO.ListVO listChunks(String datasetId, String documentId, ChunkDTO.ListReq req);

        /**
         * recalltest
         * 
         * @param req retrievetestrequestparameter
         * @return recalltestresult
         */
        RetrievalDTO.ResultVO retrievalTest(RetrievalDTO.TestReq req);

        /**
         * savedocument影子record
         */
        void saveDocumentShadow(String datasetId, KnowledgeFilesDTO result, String originalName, String chunkMethod,
                        Map<String, Object> parserConfig);

        /**
         * batchdeletedocument影子record并synchronousstatisticsdata
         * 
         * @param documentIds documentIDlist
         * @param datasetId   data集ID
         * @param chunkDelta  待扣减 totalchunknumber
         * @param tokenDelta  待扣减 totalTokennumber
         */
        void deleteDocumentShadows(List<String> documentIds, String datasetId, Long chunkDelta, Long tokenDelta);

        /**
         * according todata集ID清理allassociateddocument (cascadedelete专用)
         * 
         * @param datasetId data集ID
         */
        void deleteDocumentsByDatasetId(String datasetId);

        /**
         * synchronousall处于 RUNNING status document (供定时taskcall)
         */
        void syncRunningDocuments();
}