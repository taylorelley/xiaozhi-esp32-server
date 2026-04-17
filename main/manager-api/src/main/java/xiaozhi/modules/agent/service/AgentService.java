package xiaozhi.modules.agent.service;

import java.util.List;
import java.util.Map;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.agent.dto.AgentCreateDTO;
import xiaozhi.modules.agent.dto.AgentDTO;
import xiaozhi.modules.agent.dto.AgentUpdateDTO;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.agent.vo.AgentInfoVO;

/**
 * agenttableprocessservice
 *
 * @author Goody
 * @version 1.0, 2025/4/30
 * @since 1.0.0
 */
public interface AgentService extends BaseService<AgentEntity> {
    /**
     * getadministratorAgent list
     *
     * @param params queryparameter
     * @return paginationdata
     */
    PageData<AgentEntity> adminAgentList(Map<String, Object> params);

    /**
     * according toIDgetagent
     *
     * @param id Agent ID
     * @return agententity
     */
    AgentInfoVO getAgentById(String id);

    /**
     * insertagent
     *
     * @param entity agententity
     * @return YesNosuccess
     */
    boolean insert(AgentEntity entity);

    /**
     * according toUser IDdeleteagent
     *
     * @param userId User ID
     */
    void deleteAgentByUserId(Long userId);

    /**
     * getuserAgent list
     *
     * @param userId User ID
     * @param keyword searchkeyword
     * @param searchType searchtype（name - bynamesearch，mac - byMACAddresssearch）
     * @return Agent list
     */
    List<AgentDTO> getUserAgents(Long userId, String keyword, String searchType);

    /**
     * according toAgent IDgetDevice count
     *
     * @param agentId Agent ID
     * @return Device count
     */
    Integer getDeviceCountByAgentId(String agentId);

    /**
     * according todeviceMACAddressquerycorrespondingdevice defaultAgent information
     *
     * @param macAddress deviceMACAddress
     * @return defaultAgent information，does not existwhenreturnnull
     */
    AgentEntity getDefaultAgentByMacAddress(String macAddress);

    /**
     * checkuserYesNohasPermissionaccessagent
     *
     * @param agentId Agent ID
     * @param userId  User ID
     * @return YesNohasPermission
     */
    boolean checkAgentPermission(String agentId, Long userId);

    /**
     * updateagent
     *
     * @param agentId Agent ID
     * @param dto     updateagentrequired information
     */
    void updateAgentById(String agentId, AgentUpdateDTO dto);

    /**
     * createagent
     *
     * @param dto createagentrequired information
     * @return create Agent ID
     */
    String createAgent(AgentCreateDTO dto);


}
