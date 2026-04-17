package xiaozhi.modules.sys.service;

import java.util.List;
import java.util.Map;

import xiaozhi.common.page.PageData;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.sys.dto.SysDictDataDTO;
import xiaozhi.modules.sys.entity.SysDictDataEntity;
import xiaozhi.modules.sys.vo.SysDictDataItem;
import xiaozhi.modules.sys.vo.SysDictDataVO;

/**
 * dataDictionary
 */
public interface SysDictDataService extends BaseService<SysDictDataEntity> {

    /**
     * paginationQuery dataDictionaryinformation
     *
     * @param params queryparameter，containpaginationinformationandqueryitemsitem
     * @return returndataDictionary paginationqueryresult
     */
    PageData<SysDictDataVO> page(Map<String, Object> params);

    /**
     * according toIDgetdataDictionaryentity
     *
     * @param id dataDictionaryentity unique identifier
     * @return returndataDictionaryentity detailedinformation
     */
    SysDictDataVO get(Long id);

    /**
     * savenew dataDictionaryitem
     *
     * @param dto dataDictionaryitem Save datatransfer object
     */
    void save(SysDictDataDTO dto);

    /**
     * updatedataDictionaryitem
     *
     * @param dto dataDictionaryitem updatedatatransfer object
     */
    void update(SysDictDataDTO dto);

    /**
     * Delete dataDictionaryitem
     *
     * @param ids need todelete dataDictionaryitem IDarray
     */
    void delete(Long[] ids);

    /**
     * according toDictionary type IDdeletecorresponding Dictionary data
     *
     * @param dictTypeId Dictionary type ID
     */
    void deleteByTypeId(Long dictTypeId);

    /**
     * according toDictionary typegetDictionary datalist
     *
     * @param dictType Dictionary type
     * @return returnDictionary datalist
     */
    List<SysDictDataItem> getDictDataByType(String dictType);

}