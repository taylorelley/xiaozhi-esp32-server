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
     * 通用维度原子updateKnowledge basestatisticsinformation
     * 
     * @param datasetId  data集ID
     * @param docDelta   documentnumber增量
     * @param chunkDelta chunknumber增量
     * @param tokenDelta Tokennumber增量
     */
    void updateStatsAfterChange(@Param("datasetId") String datasetId,
            @Param("docDelta") Integer docDelta,
            @Param("chunkDelta") Long chunkDelta,
            @Param("tokenDelta") Long tokenDelta);

}