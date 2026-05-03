package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.CreateMedicineOrderDTO;
import com.xuenai.medical.model.dto.PayMedicineOrderDTO;
import com.xuenai.medical.model.vo.MedicineOrderVO;
import com.xuenai.medical.model.vo.PrescriptionVO;
import com.xuenai.medical.service.MedicineOrderService;
import com.xuenai.medical.service.ProfileResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequireRole("PATIENT")
@RequiredArgsConstructor
public class MedicineOrderController {

    private final MedicineOrderService medicineOrderService;
    private final ProfileResolveService profileResolveService;

    @GetMapping("/prescriptions/purchasable")
    public BaseResponse<List<PrescriptionVO>> purchasablePrescriptions() {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicineOrderService.listPurchasablePrescriptions(patientProfileId));
    }

    @PostMapping("/medicine-orders/from-prescription")
    public BaseResponse<MedicineOrderVO> createFromPrescription(@Valid @RequestBody CreateMedicineOrderDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicineOrderService.createFromPrescription(patientProfileId, dto));
    }

    @GetMapping("/medicine-orders")
    public BaseResponse<List<MedicineOrderVO>> list(@RequestParam(required = false) String status) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicineOrderService.listOrders(patientProfileId, status));
    }

    @GetMapping("/medicine-orders/{id}")
    public BaseResponse<MedicineOrderVO> detail(@PathVariable Long id) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicineOrderService.detail(patientProfileId, id));
    }

    @PostMapping("/medicine-orders/{id}/pay-insurance")
    public BaseResponse<MedicineOrderVO> payInsurance(@PathVariable Long id,
                                                      @Valid @RequestBody PayMedicineOrderDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicineOrderService.payWithInsurance(patientProfileId, id, dto));
    }

    @PostMapping("/medicine-orders/{id}/cancel")
    public BaseResponse<MedicineOrderVO> cancel(@PathVariable Long id) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicineOrderService.cancel(patientProfileId, id));
    }
}
