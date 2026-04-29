package com.xuenai.medical.service;

import com.xuenai.medical.model.entity.DoctorCardOrder;
import com.xuenai.medical.model.entity.DoctorCardPlan;
import com.xuenai.medical.model.entity.PrivateDoctorCard;
import com.xuenai.medical.model.vo.PrivateDoctorCardVO;

import java.util.List;

public interface PrivateDoctorCardService {

    /**
     * 激活服务卡（在支付订单后调用）
     */
    PrivateDoctorCard activateCard(DoctorCardOrder order, DoctorCardPlan plan);

    /**
     * 检查患者是否拥有某医生的有效服务卡
     */
    boolean hasActiveCard(Long patientId, Long doctorId);

    /**
     * 患者查询我的私人医生卡
     */
    List<PrivateDoctorCardVO> getMyCards(Long patientId);
    
    /**
     * 获取患者在指定医生下的有效卡
     */
    PrivateDoctorCard getActiveCard(Long patientId, Long doctorId);
    
    /**
     * 更新服务卡（扣减次数、时长等）
     */
    void updateCard(PrivateDoctorCard card);

    /**
     * 医生查看购买自己服务卡的患者列表
     */
    List<PrivateDoctorCardVO> getCardsByDoctor(Long doctorProfileId);
}
