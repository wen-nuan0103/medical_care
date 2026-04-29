package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.DoctorCardPlanMapper;
import com.xuenai.medical.model.dto.DoctorCardPlanSaveDTO;
import com.xuenai.medical.model.entity.DoctorCardPlan;
import com.xuenai.medical.service.DoctorCardPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorCardPlanServiceImpl implements DoctorCardPlanService {

    private final DoctorCardPlanMapper doctorCardPlanMapper;

    @Override
    public List<DoctorCardPlan> getDoctorPlans(Long doctorId) {
        LambdaQueryWrapper<DoctorCardPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorCardPlan::getDoctorId, doctorId)
               .orderByAsc(DoctorCardPlan::getPrice);
        return doctorCardPlanMapper.selectList(wrapper);
    }

    @Override
    public boolean saveOrUpdatePlan(Long doctorId, Long planId, DoctorCardPlanSaveDTO dto) {
        DoctorCardPlan plan;
        if (planId == null) {
            plan = new DoctorCardPlan();
            BeanUtils.copyProperties(dto, plan);
            plan.setDoctorId(doctorId);
            plan.setStatus(1); // 默认上架
            doctorCardPlanMapper.insert(plan);
        } else {
            plan = doctorCardPlanMapper.selectById(planId);
            if (plan == null || !plan.getDoctorId().equals(doctorId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无权操作此服务卡");
            }
            BeanUtils.copyProperties(dto, plan);
            doctorCardPlanMapper.updateById(plan);
        }
        return true;
    }

    @Override
    public boolean updateStatus(Long doctorId, Long planId, Integer status) {
        DoctorCardPlan plan = doctorCardPlanMapper.selectById(planId);
        if (plan == null || !plan.getDoctorId().equals(doctorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "无权操作此服务卡");
        }
        plan.setStatus(status);
        doctorCardPlanMapper.updateById(plan);
        return true;
    }
}
