package xiaozhi.modules.sys.service;

import java.util.List;
import java.util.Map;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.sys.dto.SysParamsDTO;
import xiaozhi.modules.sys.entity.SysParamsEntity;

/**
 * Parameter management
 */
public interface SysParamsService extends BaseService<SysParamsEntity> {

    PageData<SysParamsDTO> page(Map<String, Object> params);

    List<SysParamsDTO> list(Map<String, Object> params);

    SysParamsDTO get(Long id);

    void save(SysParamsDTO dto);

    void update(SysParamsDTO dto);

    void delete(String[] ids);

    /**
     * according toParameter code，getparameter valuevalue
     *
     * @param paramCode Parameter code
     * @param fromCache YesNofromcacheget
     */
    String getValue(String paramCode, Boolean fromCache);

    /**
     * according toParameter code，getvalue Objectobject
     *
     * @param paramCode Parameter code
     * @param clazz     Objectobject
     */
    <T> T getValueObject(String paramCode, Class<T> clazz);

    /**
     * according toParameter code，updatevalue
     *
     * @param paramCode  Parameter code
     * @param paramValue Parameter value
     */
    int updateValueByCode(String paramCode, String paramValue);

    /**
     * initializeservicekey
     */
    void initServerSecret();
}
