package xiaozhi.modules.device.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import xiaozhi.modules.device.entity.OtaEntity;

/**
 * OTAfirmwaremanagement
 */
@Mapper
public interface OtaDao extends BaseMapper<OtaEntity> {
    
}