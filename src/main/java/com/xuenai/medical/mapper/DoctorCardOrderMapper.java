package com.xuenai.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuenai.medical.model.entity.DoctorCardOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 私人医生卡购买订单 Mapper
 */
@Mapper
public interface DoctorCardOrderMapper extends BaseMapper<DoctorCardOrder> {
}
