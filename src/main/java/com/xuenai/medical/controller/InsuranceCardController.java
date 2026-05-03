package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.InsuranceCardBindDTO;
import com.xuenai.medical.model.vo.InsuranceCardVO;
import com.xuenai.medical.service.InsuranceCardService;
import com.xuenai.medical.service.ProfileResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient/insurance-cards")
@RequireRole("PATIENT")
@RequiredArgsConstructor
public class InsuranceCardController {

    private final InsuranceCardService insuranceCardService;
    private final ProfileResolveService profileResolveService;

    @GetMapping
    public BaseResponse<List<InsuranceCardVO>> list() {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(insuranceCardService.listByPatient(patientProfileId));
    }

    @PostMapping
    public BaseResponse<InsuranceCardVO> bind(@Valid @RequestBody InsuranceCardBindDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(insuranceCardService.bind(patientProfileId, dto));
    }
}
