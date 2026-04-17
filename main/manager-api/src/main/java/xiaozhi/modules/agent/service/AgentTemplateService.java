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
     * deletetemplate后重newSort order剩余template
     * 
     * @param deletedSort isdeletetemplate Sort ordervalue
     */
    void reorderTemplatesAfterDelete(Integer deletedSort);

    /**
     * get下一个available Sort order序number（寻找minimum notuse序number）
     * 
     * @return 下一个available Sort order序number
     */
    Integer getNextAvailableSort();
}
