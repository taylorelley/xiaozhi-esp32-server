package xiaozhi.modules.sys.service;

import java.util.List;
import java.util.Map;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.sys.dto.SysDictTypeDTO;
import xiaozhi.modules.sys.entity.SysDictTypeEntity;
import xiaozhi.modules.sys.vo.SysDictTypeVO;

/**
 * dataDictionary
 */
public interface SysDictTypeService extends BaseService<SysDictTypeEntity> {

    /**
     * paginationqueryDictionary typeinformation
     *
     * @param params queryparameter，containpaginationinformationandqueryitemsitem
     * @return returnpagination Dictionary typedata
     */
    PageData<SysDictTypeVO> page(Map<String, Object> params);

    /**
     * according toIDgetDictionary typeinformation
     *
     * @param id Dictionary type ID
     * @return returnDictionary typeobject
     */
    SysDictTypeVO get(Long id);

    /**
     * saveDictionary typeinformation
     *
     * @param dto Dictionary typedatatransfer object
     */
    void save(SysDictTypeDTO dto);

    /**
     * updateDictionary typeinformation
     *
     * @param dto Dictionary typedatatransfer object
     */
    void update(SysDictTypeDTO dto);

    /**
     * deleteDictionary typeinformation
     *
     * @param ids need todelete Dictionary type IDarray
     */
    void delete(Long[] ids);

    /**
     * listallDictionary typeinformation
     *
     * @return returnDictionary typelist
     */
    List<SysDictTypeVO> list(Map<String, Object> params);
}