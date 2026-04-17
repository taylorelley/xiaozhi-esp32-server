package xiaozhi.common.service;

import java.io.Serializable;
import java.util.Collection;

import com.baomidou.mybatisplus.core.conditions.Wrapper;

/**
 * baseserviceinterface，allServiceinterfaceallneed to继承
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
public interface BaseService<T> {
    Class<T> currentModelClass();

    /**
     * <p>
     * insertoneitemsrecord（selectfield，策略insert）
     * </p>
     *
     * @param entity entity object
     */
    boolean insert(T entity);

    /**
     * <p>
     * insert（batch），thismethodnot support Oracle、SQL Server
     * </p>
     *
     * @param entityList entity object collection
     */
    boolean insertBatch(Collection<T> entityList);

    /**
     * <p>
     * insert（batch），thismethodnot support Oracle、SQL Server
     * </p>
     *
     * @param entityList entity object collection
     * @param batchSize  insert批timescount
     */
    boolean insertBatch(Collection<T> entityList, int batchSize);

    /**
     * <p>
     * according to ID selectupdate
     * </p>
     *
     * @param entity entity object
     */
    boolean updateById(T entity);

    /**
     * <p>
     * according to whereEntity itemsitem，updaterecord
     * </p>
     *
     * @param entity        entity object
     * @param updateWrapper entity object封装operationclass
     *                      {@link com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper}
     */
    boolean update(T entity, Wrapper<T> updateWrapper);

    /**
     * <p>
     * according toID batchupdate
     * </p>
     *
     * @param entityList entity object collection
     */
    boolean updateBatchById(Collection<T> entityList);

    /**
     * <p>
     * according toID batchupdate
     * </p>
     *
     * @param entityList entity object collection
     * @param batchSize  update批timescount
     */
    boolean updateBatchById(Collection<T> entityList, int batchSize);

    /**
     * <p>
     * according to ID query
     * </p>
     *
     * @param id Primary keyID
     */
    T selectById(Serializable id);

    /**
     * <p>
     * according to ID delete
     * </p>
     *
     * @param id Primary keyID
     */
    boolean deleteById(Serializable id);

    /**
     * <p>
     * delete（according toID batchdelete）
     * </p>
     *
     * @param idList Primary keyIDlist
     */
    boolean deleteBatchIds(Collection<? extends Serializable> idList);
}