package com.xuenai.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuenai.medical.model.entity.PatientProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者资料 Mapper
 */
@Mapper
public interface PatientProfileMapper extends BaseMapper<PatientProfile> {
}
