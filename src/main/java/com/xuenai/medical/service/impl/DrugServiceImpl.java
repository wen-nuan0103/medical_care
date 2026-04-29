package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.DrugMapper;
import com.xuenai.medical.model.dto.DrugSaveDTO;
import com.xuenai.medical.model.dto.StockUpdateDTO;
import com.xuenai.medical.model.entity.Drug;
import com.xuenai.medical.model.vo.DrugDetailVO;
import com.xuenai.medical.model.vo.DrugVO;
import com.xuenai.medical.service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 药品 Service 实现
 */
@Service
@RequiredArgsConstructor
public class DrugServiceImpl implements DrugService {

    private final DrugMapper drugMapper;

    @Override
    public PageResult<DrugVO> pageDrugs(int current, int pageSize, String keyword, Long categoryId,
                                        Integer prescriptionRequired, Integer insuranceCovered, Integer status) {
        int safePageSize = Math.min(100, Math.max(1, pageSize));
        int safeCurrent = Math.max(1, current);
        long offset = (long) (safeCurrent - 1) * safePageSize;

        long total = drugMapper.countDrugs(keyword, categoryId, prescriptionRequired, insuranceCovered, status);
        List<DrugVO> records = drugMapper.selectDrugVOPage(offset, safePageSize, keyword, categoryId, prescriptionRequired, insuranceCovered, status);
        return PageResult.of(records, total, safeCurrent, safePageSize);
    }

    @Override
    public DrugDetailVO detail(Long id) {
        DrugDetailVO detail = drugMapper.selectDrugDetailById(id);
        if (detail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "药品不存在");
        }
        return detail;
    }

    @Override
    @Transactional
    public DrugDetailVO create(DrugSaveDTO request) {
        Long operatorId = UserContext.getUserId();
        Drug drug = toEntity(request);
        drug.setCreateBy(operatorId);
        drug.setUpdateBy(operatorId);
        drugMapper.insert(drug);
        return detail(drug.getId());
    }

    @Override
    @Transactional
    public DrugDetailVO update(Long id, DrugSaveDTO request) {
        Long operatorId = UserContext.getUserId();
        Drug drug = toEntity(request);
        drug.setUpdateBy(operatorId);

        LambdaUpdateWrapper<Drug> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Drug::getId, id).eq(Drug::getDeleted, 0);
        int updated = drugMapper.update(drug, wrapper);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "药品不存在");
        }
        return detail(id);
    }

    @Override
    @Transactional
    public DrugDetailVO updateStock(Long id, StockUpdateDTO request) {
        Long operatorId = UserContext.getUserId();
        LambdaUpdateWrapper<Drug> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Drug::getId, id)
               .eq(Drug::getDeleted, 0)
               .set(Drug::getStockQuantity, request.stockQuantity())
               .set(Drug::getUpdateBy, operatorId);
        int updated = drugMapper.update(null, wrapper);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "药品不存在");
        }
        return detail(id);
    }

    private Drug toEntity(DrugSaveDTO dto) {
        Drug drug = new Drug();
        drug.setDrugCode(dto.drugCode());
        drug.setDrugName(dto.drugName());
        drug.setGenericName(dto.genericName());
        drug.setCategoryId(dto.categoryId());
        drug.setSpecification(dto.specification());
        drug.setDosageForm(dto.dosageForm());
        drug.setManufacturer(dto.manufacturer());
        drug.setApprovalNo(dto.approvalNo());
        drug.setPrice(dto.price());
        drug.setStockQuantity(dto.stockQuantity());
        drug.setWarningThreshold(dto.warningThreshold());
        drug.setPrescriptionRequired(dto.prescriptionRequired());
        drug.setInsuranceCovered(dto.insuranceCovered());
        drug.setUsageInstruction(dto.usageInstruction());
        drug.setContraindication(dto.contraindication());
        drug.setAdverseReaction(dto.adverseReaction());
        drug.setStatus(dto.status() == null ? 1 : dto.status());
        return drug;
    }
}
