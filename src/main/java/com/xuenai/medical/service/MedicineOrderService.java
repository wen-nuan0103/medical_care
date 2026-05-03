package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.CreateMedicineOrderDTO;
import com.xuenai.medical.model.dto.PayMedicineOrderDTO;
import com.xuenai.medical.model.vo.MedicineOrderVO;
import com.xuenai.medical.model.vo.PrescriptionVO;

import java.util.List;

public interface MedicineOrderService {

    List<PrescriptionVO> listPurchasablePrescriptions(Long patientProfileId);

    MedicineOrderVO createFromPrescription(Long patientProfileId, CreateMedicineOrderDTO dto);

    List<MedicineOrderVO> listOrders(Long patientProfileId, String status);

    MedicineOrderVO detail(Long patientProfileId, Long orderId);

    MedicineOrderVO payWithInsurance(Long patientProfileId, Long orderId, PayMedicineOrderDTO dto);

    MedicineOrderVO cancel(Long patientProfileId, Long orderId);
}
