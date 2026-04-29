package com.xuenai.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuenai.medical.model.entity.DoctorCardPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生服务卡配置 Mapper
 */
@Mapper
public interface DoctorCardPlanMapper extends BaseMapper<DoctorCardPlan> {
}
