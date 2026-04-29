package com.xuenai.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuenai.medical.model.entity.ConsultationSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问诊会话 Mapper
 */
@Mapper
public interface ConsultationSessionMapper extends BaseMapper<ConsultationSession> {
}
