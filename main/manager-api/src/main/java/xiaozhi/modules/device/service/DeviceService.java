package xiaozhi.modules.device.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.device.dto.DeviceManualAddDTO;
import xiaozhi.modules.device.dto.DevicePageUserDTO;
import xiaozhi.modules.device.dto.DeviceReportReqDTO;
import xiaozhi.modules.device.dto.DeviceReportRespDTO;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.device.vo.UserShowDeviceListVO;

public interface DeviceService extends BaseService<DeviceEntity> {
    /**
     * getdevicein线data
     */
    String getDeviceOnlineData(String agentId);

    /**
     * 检查deviceYesNoactivation
     */
    DeviceReportRespDTO checkDeviceActive(String macAddress, String clientId,
            DeviceReportReqDTO deviceReport);

    /**
     * getuserspecifiedagent Device list，
     */
    List<DeviceEntity> getUserDevices(Long userId, String agentId);

    /**
     * 解绑device
     */
    void unbindDevice(Long userId, String deviceId);

    /**
     * deviceactivation
     */
    Boolean deviceActivation(String agentId, String activationCode);

    /**
     * deletethisuser alldevice
     * 
     * @param userId userid
     */
    void deleteByUserId(Long userId);

    /**
     * deletespecifiedagentassociated alldevice
     * 
     * @param agentId agentid
     */
    void deleteByAgentId(String agentId);

    /**
     * getspecifieduser Device count
     * 
     * @param userId userid
     * @return Device count
     */
    Long selectCountByUserId(Long userId);

    /**
     * paginationgetAllDevice information
     *
     * @param dto paginationfindparameter
     * @return User listpaginationdata
     */
    PageData<UserShowDeviceListVO> page(DevicePageUserDTO dto);

    /**
     * according toMACAddressgetDevice information
     * 
     * @param macAddress MACAddress
     * @return Device information
     */
    DeviceEntity getDeviceByMacAddress(String macAddress);

    /**
     * according toDevice IDgetActivation code
     * 
     * @param deviceId Device ID
     * @return Activation code
     */
    String geCodeByDeviceId(String deviceId);

    /**
     * getthis个Agent device理 最近 lastconnectiontime
     * 
     * @param agentId agentid
     * @return returndevice最近 lastconnectiontime
     */
    Date getLatestLastConnectionTime(String agentId);

    /**
     * 手动adddevice
     */
    void manualAddDevice(Long userId, DeviceManualAddDTO dto);

    /**
     * updatedeviceconnectioninformation
     */
    void updateDeviceConnectionInfo(String agentId, String deviceId, String appVersion);

    /**
     * generateWebSocketauthenticationtoken
     *
     * @param clientId clientID
     * @param username Username(usually deviceId)
     * @return authenticationtokenstring
     * @throws Exception generatetoken时 exception
     */
    String generateWebSocketToken(String clientId, String username) throws Exception;

    /**
     * according toMACAddresssearchdevice
     *
     * @param macAddress MACAddresskeyword
     * @param userId     User ID
     * @return Device list
     */
    List<DeviceEntity> searchDevicesByMacAddress(String macAddress, Long userId);

    /**
     * getdevicetool list
     */
    Object getDeviceTools(String deviceId);

    /**
     * calldevicetool
     */
    Object callDeviceTool(String deviceId, String toolName, Map<String, Object> arguments);

}