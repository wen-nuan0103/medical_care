package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.HealthTrackSaveDTO;
import com.xuenai.medical.model.vo.HealthTrackRecordVO;
import com.xuenai.medical.model.vo.PatientCareVO;
import com.xuenai.medical.service.HealthTrackService;
import com.xuenai.medical.service.ProfileResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthTrackController {

    private final HealthTrackService healthTrackService;
    private final ProfileResolveService profileResolveService;

    @PostMapping("/patient/health-tracks")
    @RequireRole("PATIENT")
    public BaseResponse<HealthTrackRecordVO> create(@Valid @RequestBody HealthTrackSaveDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(healthTrackService.create(patientProfileId, dto));
    }

    @GetMapping("/patient/health-tracks")
    @RequireRole("PATIENT")
    public BaseResponse<List<HealthTrackRecordVO>> listPatientTracks() {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(healthTrackService.listForPatient(patientProfileId));
    }

    @GetMapping("/doctor/patients")
    @RequireRole("DOCTOR")
    public BaseResponse<List<PatientCareVO>> listDoctorPatients() {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(healthTrackService.listDoctorPatients(doctorProfileId));
    }

    @GetMapping("/doctor/patients/{patientId}/health-tracks")
    @RequireRole("DOCTOR")
    public BaseResponse<List<HealthTrackRecordVO>> listDoctorPatientTracks(@PathVariable Long patientId) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(healthTrackService.listForDoctor(doctorProfileId, patientId));
    }
}
