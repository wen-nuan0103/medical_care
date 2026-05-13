package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.FollowUpPlanSaveDTO;
import com.xuenai.medical.model.dto.FollowUpStatusDTO;
import com.xuenai.medical.model.vo.FollowUpPlanVO;
import com.xuenai.medical.service.FollowUpPlanService;
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
public class FollowUpPlanController {

    private final FollowUpPlanService followUpPlanService;
    private final ProfileResolveService profileResolveService;

    @GetMapping("/patient/follow-ups")
    @RequireRole("PATIENT")
    public BaseResponse<List<FollowUpPlanVO>> listPatientFollowUps(@RequestParam(required = false) String status) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(followUpPlanService.listForPatient(patientProfileId, status));
    }

    @GetMapping("/doctor/follow-ups")
    @RequireRole("DOCTOR")
    public BaseResponse<List<FollowUpPlanVO>> listDoctorFollowUps(@RequestParam(required = false) Long patientId,
                                                                  @RequestParam(required = false) String status) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(followUpPlanService.listForDoctor(doctorProfileId, patientId, status));
    }

    @PostMapping("/doctor/follow-ups")
    @RequireRole("DOCTOR")
    public BaseResponse<FollowUpPlanVO> create(@Valid @RequestBody FollowUpPlanSaveDTO dto) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(followUpPlanService.create(doctorProfileId, dto));
    }

    @PutMapping("/doctor/follow-ups/{id}/status")
    @RequireRole("DOCTOR")
    public BaseResponse<FollowUpPlanVO> updateStatus(@PathVariable Long id,
                                                     @Valid @RequestBody FollowUpStatusDTO dto) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        return ResultUtils.success(followUpPlanService.updateStatus(doctorProfileId, id, dto));
    }
}
