package com.xuenai.medical.controller;

import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.vo.DrugDetailVO;
import com.xuenai.medical.model.vo.DrugVO;
import com.xuenai.medical.service.DrugService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    private final DrugService drugService;

    public DrugController(DrugService drugService) {
        this.drugService = drugService;
    }

    @GetMapping
    public BaseResponse<PageResult<DrugVO>> pageDrugs(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer prescriptionRequired,
            @RequestParam(required = false) Integer insuranceCovered,
            @RequestParam(required = false) Integer status
    ) {
        return ResultUtils.success(drugService.pageDrugs(current, pageSize, keyword, categoryId, prescriptionRequired, insuranceCovered, status));
    }

    @GetMapping("/{id}")
    public BaseResponse<DrugDetailVO> detail(@PathVariable Long id) {
        return ResultUtils.success(drugService.detail(id));
    }
}
