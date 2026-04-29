package com.xuenai.medical.controller;

import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.model.vo.DoctorVO;
import com.xuenai.medical.service.DoctorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public BaseResponse<PageResult<DoctorVO>> pageDoctors(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer serviceStatus,
            @RequestParam(required = false) String keyword
    ) {
        return ResultUtils.success(doctorService.pageDoctors(current, pageSize, department, title, serviceStatus, keyword));
    }

    @GetMapping("/{id}")
    public BaseResponse<DoctorDetailVO> detail(@PathVariable Long id) {
        return ResultUtils.success(doctorService.detail(id));
    }
}
