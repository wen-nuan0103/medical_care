package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.DoctorCardPlanSaveDTO;
import com.xuenai.medical.model.entity.DoctorCardPlan;

import java.util.List;

public interface DoctorCardPlanService {
    
    /**
     * 医生查询自己配置的服务卡类型
     */
    List<DoctorCardPlan> getDoctorPlans(Long doctorId);

    /**
     * 医生新增或修改服务卡配置
     */
    boolean saveOrUpdatePlan(Long doctorId, Long planId, DoctorCardPlanSaveDTO dto);

    /**
     * 医生修改服务卡上下架状态
     */
    boolean updateStatus(Long doctorId, Long planId, Integer status);
}
