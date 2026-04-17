package xiaozhi.modules.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.sys.entity.SysParamsEntity;

/**
 * Parameter management
 */
@Mapper
public interface SysParamsDao extends BaseDao<SysParamsEntity> {
    /**
     * according toParameter code，queryvalue
     *
     * @param paramCode Parameter code
     * @return Parameter value
     */
    String getValueByCode(String paramCode);

    /**
     * getParameter codelist
     *
     * @param ids ids
     * @return returnParameter codelist
     */
    List<String> getParamCodeList(String[] ids);

    /**
     * according toParameter code，updatevalue
     *
     * @param paramCode  Parameter code
     * @param paramValue Parameter value
     */
    int updateValueByCode(@Param("paramCode") String paramCode, @Param("paramValue") String paramValue);
}
