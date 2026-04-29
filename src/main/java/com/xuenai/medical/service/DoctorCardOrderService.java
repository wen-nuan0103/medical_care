package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.PurchaseCardDTO;
import com.xuenai.medical.model.entity.DoctorCardOrder;

public interface DoctorCardOrderService {

    /**
     * 患者发起购买服务卡，生成订单并模拟支付
     */
    DoctorCardOrder purchaseCard(Long patientId, PurchaseCardDTO dto);
    
}
