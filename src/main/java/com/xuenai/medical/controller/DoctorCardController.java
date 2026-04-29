package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.DoctorCardPlanSaveDTO;
import com.xuenai.medical.model.dto.PurchaseCardDTO;
import com.xuenai.medical.model.entity.DoctorCardOrder;
import com.xuenai.medical.model.entity.DoctorCardPlan;
import com.xuenai.medical.model.vo.PrivateDoctorCardVO;
import com.xuenai.medical.service.DoctorCardOrderService;
import com.xuenai.medical.service.DoctorCardPlanService;
import com.xuenai.medical.service.PrivateDoctorCardService;
import com.xuenai.medical.service.ProfileResolveService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DoctorCardController {

    private final DoctorCardPlanService doctorCardPlanService;
    private final DoctorCardOrderService doctorCardOrderService;
    private final PrivateDoctorCardService privateDoctorCardService;
    private final ProfileResolveService profileResolveService;

    public DoctorCardController(DoctorCardPlanService doctorCardPlanService,
                                DoctorCardOrderService doctorCardOrderService,
                                PrivateDoctorCardService privateDoctorCardService,
                                ProfileResolveService profileResolveService) {
        this.doctorCardPlanService = doctorCardPlanService;
        this.doctorCardOrderService = doctorCardOrderService;
        this.privateDoctorCardService = privateDoctorCardService;
        this.profileResolveService = profileResolveService;
    }

    // --- 医生端接口 ---

    @GetMapping("/doctor/card-plans")
    @RequireRole("DOCTOR")
    public BaseResponse<List<DoctorCardPlan>> getDoctorPlans() {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(doctorCardPlanService.getDoctorPlans(doctorProfileId));
    }

    @PostMapping("/doctor/card-plans")
    @RequireRole("DOCTOR")
    public BaseResponse<Boolean> addPlan(@Valid @RequestBody DoctorCardPlanSaveDTO dto) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(doctorCardPlanService.saveOrUpdatePlan(doctorProfileId, null, dto));
    }

    @PutMapping("/doctor/card-plans/{id}")
    @RequireRole("DOCTOR")
    public BaseResponse<Boolean> updatePlan(@PathVariable Long id, @Valid @RequestBody DoctorCardPlanSaveDTO dto) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(doctorCardPlanService.saveOrUpdatePlan(doctorProfileId, id, dto));
    }

    // --- 患者端接口 ---

    @GetMapping("/doctors/{doctorId}/card-plans")
    public BaseResponse<List<DoctorCardPlan>> getDoctorPlansForPatient(@PathVariable Long doctorId) {
        return ResultUtils.success(doctorCardPlanService.getDoctorPlans(doctorId));
    }

    @PostMapping("/patient/private-cards/purchase")
    @RequireRole("PATIENT")
    public BaseResponse<DoctorCardOrder> purchaseCard(@Valid @RequestBody PurchaseCardDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(doctorCardOrderService.purchaseCard(patientProfileId, dto));
    }

    @GetMapping("/patient/private-cards")
    @RequireRole("PATIENT")
    public BaseResponse<List<PrivateDoctorCardVO>> getMyCards() {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(privateDoctorCardService.getMyCards(patientProfileId));
    }

    @GetMapping("/doctor/private-card-patients")
    @RequireRole("DOCTOR")
    public BaseResponse<List<PrivateDoctorCardVO>> getMyPatients() {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(privateDoctorCardService.getCardsByDoctor(doctorProfileId));
    }
}
