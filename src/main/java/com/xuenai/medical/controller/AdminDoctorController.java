package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.StatusUpdateDTO;
import com.xuenai.medical.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/doctors")
@RequireRole("ADMIN")
public class AdminDoctorController {

    private final DoctorService doctorService;

    public AdminDoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PutMapping("/{id}/status")
    public BaseResponse<Boolean> updateServiceStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO request) {
        return ResultUtils.success(doctorService.updateServiceStatus(id, request.status()));
    }
}
