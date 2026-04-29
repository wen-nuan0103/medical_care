package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.DrugSaveDTO;
import com.xuenai.medical.model.dto.StockUpdateDTO;
import com.xuenai.medical.model.vo.DrugDetailVO;
import com.xuenai.medical.service.DrugService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireRole({"PHARMACIST", "ADMIN"})
public class DrugManageController {

    private final DrugService drugService;

    public DrugManageController(DrugService drugService) {
        this.drugService = drugService;
    }

    @PostMapping({"/api/pharmacist/drugs", "/api/admin/drugs"})
    public BaseResponse<DrugDetailVO> create(@Valid @RequestBody DrugSaveDTO request) {
        return ResultUtils.success(drugService.create(request));
    }

    @PutMapping({"/api/pharmacist/drugs/{id}", "/api/admin/drugs/{id}"})
    public BaseResponse<DrugDetailVO> update(@PathVariable Long id, @Valid @RequestBody DrugSaveDTO request) {
        return ResultUtils.success(drugService.update(id, request));
    }

    @PutMapping({"/api/pharmacist/drugs/{id}/stock", "/api/admin/drugs/{id}/stock"})
    public BaseResponse<DrugDetailVO> updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateDTO request) {
        return ResultUtils.success(drugService.updateStock(id, request));
    }
}
