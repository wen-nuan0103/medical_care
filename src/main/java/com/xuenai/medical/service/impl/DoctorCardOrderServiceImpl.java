package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.DoctorCardOrderMapper;
import com.xuenai.medical.mapper.DoctorCardPlanMapper;
import com.xuenai.medical.mapper.DoctorMapper;
import com.xuenai.medical.model.dto.PurchaseCardDTO;
import com.xuenai.medical.model.entity.DoctorCardOrder;
import com.xuenai.medical.model.entity.DoctorCardPlan;
import com.xuenai.medical.model.entity.DoctorProfile;
import com.xuenai.medical.model.entity.PrivateDoctorCard;
import com.xuenai.medical.service.DoctorCardOrderService;
import com.xuenai.medical.service.PrivateDoctorCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorCardOrderServiceImpl implements DoctorCardOrderService {

    private final DoctorCardOrderMapper doctorCardOrderMapper;
    private final DoctorCardPlanMapper doctorCardPlanMapper;
    private final DoctorMapper doctorMapper;
    private final PrivateDoctorCardService privateDoctorCardService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DoctorCardOrder purchaseCard(Long patientId, PurchaseCardDTO dto) {
        // 1. 校验医生和服务卡
        DoctorProfile doctor = doctorMapper.selectById(dto.getDoctorId());
        if (doctor == null || doctor.getServiceStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生不存在或未开通服务");
        }

        DoctorCardPlan plan = doctorCardPlanMapper.selectById(dto.getPlanId());
        if (plan == null || !plan.getDoctorId().equals(dto.getDoctorId()) || plan.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "服务卡不存在或已下架");
        }

        // 2. 检查是否已经有该医生有效的服务卡 (简单起见，这里假设同一医生只能有一张有效卡)
        boolean hasActiveCard = privateDoctorCardService.hasActiveCard(patientId, dto.getDoctorId());
        if (hasActiveCard) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您已拥有该医生的有效服务卡，无需重复购买");
        }

        // 3. 生成订单
        DoctorCardOrder order = new DoctorCardOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setPatientId(patientId);
        order.setDoctorId(dto.getDoctorId());
        order.setPlanId(dto.getPlanId());
        order.setCardType(plan.getCardType());
        order.setPlanName(plan.getPlanName());
        order.setTotalAmount(plan.getPrice());
        
        // 模拟支付逻辑，直接设置为已支付
        order.setPayAmount(plan.getPrice());
        order.setPayType("MOCK");
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        
        doctorCardOrderMapper.insert(order);

        // 4. 生成服务卡记录
        privateDoctorCardService.activateCard(order, plan);

        return order;
    }
}
