package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.PrescriptionSaveDTO;
import com.xuenai.medical.model.vo.PrescriptionVO;
import com.xuenai.medical.service.PrescriptionService;
import com.xuenai.medical.service.ProfileResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final ProfileResolveService profileResolveService;

    @PostMapping("/doctor/prescriptions")
    @RequireRole("DOCTOR")
    public BaseResponse<PrescriptionVO> create(@Valid @RequestBody PrescriptionSaveDTO dto) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(prescriptionService.create(doctorProfileId, dto));
    }

    @PutMapping("/doctor/prescriptions/{id}")
    @RequireRole("DOCTOR")
    public BaseResponse<PrescriptionVO> update(@PathVariable Long id, @Valid @RequestBody PrescriptionSaveDTO dto) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(prescriptionService.update(doctorProfileId, id, dto));
    }

    @PostMapping("/doctor/prescriptions/{id}/submit")
    @RequireRole("DOCTOR")
    public BaseResponse<PrescriptionVO> submit(@PathVariable Long id) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(prescriptionService.submit(doctorProfileId, id));
    }

    @GetMapping("/doctor/prescriptions")
    @RequireRole("DOCTOR")
    public BaseResponse<List<PrescriptionVO>> listDoctorPrescriptions(@RequestParam(required = false) String status) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(prescriptionService.listForDoctor(doctorProfileId, status));
    }

    @GetMapping("/patient/prescriptions")
    @RequireRole("PATIENT")
    public BaseResponse<List<PrescriptionVO>> listPatientPrescriptions(@RequestParam(required = false) String status) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(prescriptionService.listForPatient(patientProfileId, status));
    }

    @GetMapping("/prescriptions/{id}")
    public BaseResponse<PrescriptionVO> detail(@PathVariable Long id) {
        return ResultUtils.success(prescriptionService.detail(id, UserContext.get()));
    }
}
