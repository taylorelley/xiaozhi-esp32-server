package xiaozhi.modules.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;

import xiaozhi.modules.agent.entity.AgentTemplateEntity;

/**
 * @author chenerlei
 * @description for table【ai_agent_template(Agent configurationtemplatetable)】 datalibraryoperationService
 * @createDate 2025-03-22 11:48:18
 */
public interface AgentTemplateService extends IService<AgentTemplateEntity> {

    /**
     * getdefaulttemplate
     * 
     * @return defaulttemplateentity
     */
    AgentTemplateEntity getDefaultTemplate();

    /**
     * updatedefaulttemplate Model ID
     * 
     * @param modelType Model type
     * @param modelId   Model ID
     */
    void updateDefaultTemplateModelId(String modelType, String modelId);

    /**
     * deletetemplateafterre-newSort order剩余template
     * 
     * @param deletedSort isdeletetemplate Sort ordervalue
     */
    void reorderTemplatesAfterDelete(Integer deletedSort);

    /**
     * get下oneavailable Sort orderordernumber（寻findminimum notuseordernumber）
     * 
     * @return 下oneavailable Sort orderordernumber
     */
    Integer getNextAvailableSort();
}
