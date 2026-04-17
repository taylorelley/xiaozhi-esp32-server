package xiaozhi.modules.knowledge.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.knowledge.entity.KnowledgeBaseEntity;

/**
 * Knowledge baseKnowledge base
 */
@Mapper
public interface KnowledgeBaseDao extends BaseDao<KnowledgeBaseEntity> {

    /**
     * according toKnowledge baseIDdeleterelated pluginmappingrecord
     * 
     * @param knowledgeBaseId Knowledge baseID
     */
    void deletePluginMappingByKnowledgeBaseId(@Param("knowledgeBaseId") String knowledgeBaseId);

    /**
     * usedimoriginalchildupdateKnowledge basestatisticsinformation
     * 
     * @param datasetId  datacollectionID
     * @param docDelta   documentnumberincremental
     * @param chunkDelta chunknumberincremental
     * @param tokenDelta Tokennumberincremental
     */
    void updateStatsAfterChange(@Param("datasetId") String datasetId,
            @Param("docDelta") Integer docDelta,
            @Param("chunkDelta") Long chunkDelta,
            @Param("tokenDelta") Long tokenDelta);

}