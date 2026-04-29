package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.DrugInteractionRuleMapper;
import com.xuenai.medical.mapper.DrugMapper;
import com.xuenai.medical.mapper.PharmacistProfileMapper;
import com.xuenai.medical.mapper.PrescriptionAuditMapper;
import com.xuenai.medical.mapper.PrescriptionItemMapper;
import com.xuenai.medical.mapper.PrescriptionMapper;
import com.xuenai.medical.model.dto.PrescriptionAuditDTO;
import com.xuenai.medical.model.entity.Drug;
import com.xuenai.medical.model.entity.DrugInteractionRule;
import com.xuenai.medical.model.entity.PharmacistProfile;
import com.xuenai.medical.model.entity.Prescription;
import com.xuenai.medical.model.entity.PrescriptionAudit;
import com.xuenai.medical.model.entity.PrescriptionItem;
import com.xuenai.medical.model.vo.DrugInteractionRiskVO;
import com.xuenai.medical.model.vo.PrescriptionAuditCheckVO;
import com.xuenai.medical.model.vo.PrescriptionStockWarningVO;
import com.xuenai.medical.model.vo.PrescriptionVO;
import com.xuenai.medical.service.PrescriptionAuditService;
import com.xuenai.medical.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionAuditServiceImpl implements PrescriptionAuditService {

    private static final String STATUS_PENDING_AUDIT = "PENDING_AUDIT";
    private static final String RESULT_APPROVED = "APPROVED";
    private static final String RESULT_REJECTED = "REJECTED";
    private static final String RESULT_NEED_MODIFY = "NEED_MODIFY";

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final PrescriptionAuditMapper prescriptionAuditMapper;
    private final DrugMapper drugMapper;
    private final DrugInteractionRuleMapper drugInteractionRuleMapper;
    private final PharmacistProfileMapper pharmacistProfileMapper;
    private final PrescriptionService prescriptionService;

    @Override
    @Transactional(readOnly = true)
    public PrescriptionAuditCheckVO check(Long prescriptionId) {
        Prescription prescription = getPrescription(prescriptionId);
        List<PrescriptionItem> items = listItems(prescription.getId());
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处方明细为空");
        }

        Map<Long, Drug> drugMap = loadDrugMap(items);
        List<PrescriptionStockWarningVO> stockWarnings = buildStockWarnings(items, drugMap);
        List<DrugInteractionRiskVO> interactionRisks = buildInteractionRisks(items, drugMap);

        String riskLevel = resolveRiskLevel(stockWarnings, interactionRisks);
        PrescriptionAuditCheckVO vo = new PrescriptionAuditCheckVO();
        vo.setPrescriptionId(prescription.getId());
        vo.setRiskLevel(riskLevel);
        vo.setStockWarnings(stockWarnings);
        vo.setInteractionRisks(interactionRisks);
        vo.setStockResult(buildStockResult(stockWarnings));
        vo.setInteractionResult(buildInteractionResult(interactionRisks));
        vo.setDosageResult("系统已提取剂量、频次和疗程，仍需药剂师结合患者病史、过敏史和肝肾功能人工复核。");
        vo.setSummary(buildSummary(riskLevel, stockWarnings, interactionRisks));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionVO audit(Long pharmacistProfileId, Long prescriptionId, PrescriptionAuditDTO dto) {
        PharmacistProfile pharmacist = pharmacistProfileMapper.selectById(pharmacistProfileId);
        if (pharmacist == null || !Integer.valueOf(1).equals(pharmacist.getStatus())
                || !"APPROVED".equals(pharmacist.getAuditStatus())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "药剂师档案未启用或未审核通过");
        }

        Prescription prescription = getPrescription(prescriptionId);
        if (!STATUS_PENDING_AUDIT.equals(prescription.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前处方不在待审核状态");
        }
        String auditResult = normalizeAuditResult(dto.getAuditResult());
        PrescriptionAuditCheckVO check = check(prescriptionId);
        if (RESULT_APPROVED.equals(auditResult) && hasBlocker(check)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "存在禁用级风险或库存不足，不能审核通过");
        }

        PrescriptionAudit audit = new PrescriptionAudit();
        audit.setPrescriptionId(prescriptionId);
        audit.setPharmacistId(pharmacistProfileId);
        audit.setAuditResult(auditResult);
        audit.setRiskLevel(hasText(dto.getRiskLevel()) ? dto.getRiskLevel() : check.getRiskLevel());
        audit.setInteractionResult(check.getInteractionResult());
        audit.setStockResult(check.getStockResult());
        audit.setDosageResult(hasText(dto.getDosageResult()) ? dto.getDosageResult() : check.getDosageResult());
        audit.setAdvice(dto.getAdvice());
        audit.setAuditTime(LocalDateTime.now());
        prescriptionAuditMapper.insert(audit);

        prescription.setStatus(auditResult);
        if (RESULT_APPROVED.equals(auditResult)) {
            prescription.setApproveTime(LocalDateTime.now());
            prescription.setValidUntil(LocalDateTime.now().plusDays(3));
        }
        prescriptionMapper.updateById(prescription);
        return prescriptionService.detail(prescriptionId, UserContext.get());
    }

    private Prescription getPrescription(Long prescriptionId) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "处方不存在");
        }
        return prescription;
    }

    private List<PrescriptionItem> listItems(Long prescriptionId) {
        LambdaQueryWrapper<PrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionItem::getPrescriptionId, prescriptionId)
                .orderByAsc(PrescriptionItem::getId);
        return prescriptionItemMapper.selectList(wrapper);
    }

    private Map<Long, Drug> loadDrugMap(List<PrescriptionItem> items) {
        return drugMapper.selectBatchIds(items.stream()
                        .map(PrescriptionItem::getDrugId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Drug::getId, Function.identity()));
    }

    private List<PrescriptionStockWarningVO> buildStockWarnings(List<PrescriptionItem> items, Map<Long, Drug> drugMap) {
        return items.stream().map(item -> {
            Drug drug = drugMap.get(item.getDrugId());
            PrescriptionStockWarningVO vo = new PrescriptionStockWarningVO();
            vo.setDrugId(item.getDrugId());
            vo.setDrugName(item.getDrugName());
            vo.setRequestedQuantity(item.getQuantity());
            vo.setCurrentStock(drug == null ? 0 : drug.getStockQuantity());
            vo.setWarningThreshold(drug == null ? 0 : drug.getWarningThreshold());
            boolean enough = drug != null && drug.getStockQuantity() != null && drug.getStockQuantity() >= item.getQuantity();
            vo.setStockEnough(enough);
            if (!enough) {
                vo.setWarningMessage("库存不足，需补货或调整处方");
            } else if (drug.getWarningThreshold() != null && drug.getStockQuantity() <= drug.getWarningThreshold()) {
                vo.setWarningMessage("库存接近预警阈值");
            } else {
                vo.setWarningMessage("库存充足");
            }
            return vo;
        }).toList();
    }

    private List<DrugInteractionRiskVO> buildInteractionRisks(List<PrescriptionItem> items, Map<Long, Drug> drugMap) {
        List<Long> drugIds = items.stream().map(PrescriptionItem::getDrugId).distinct().toList();
        if (drugIds.size() < 2) {
            return List.of();
        }
        LambdaQueryWrapper<DrugInteractionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugInteractionRule::getStatus, 1)
                .in(DrugInteractionRule::getDrugAId, drugIds)
                .in(DrugInteractionRule::getDrugBId, drugIds);
        return drugInteractionRuleMapper.selectList(wrapper).stream()
                .map(rule -> toRiskVO(rule, drugMap))
                .toList();
    }

    private DrugInteractionRiskVO toRiskVO(DrugInteractionRule rule, Map<Long, Drug> drugMap) {
        DrugInteractionRiskVO vo = new DrugInteractionRiskVO();
        vo.setRuleId(rule.getId());
        vo.setDrugAId(rule.getDrugAId());
        vo.setDrugBId(rule.getDrugBId());
        vo.setDrugAName(resolveDrugName(rule.getDrugAId(), drugMap));
        vo.setDrugBName(resolveDrugName(rule.getDrugBId(), drugMap));
        vo.setRiskLevel(rule.getRiskLevel());
        vo.setDescription(rule.getDescription());
        vo.setSuggestion(rule.getSuggestion());
        return vo;
    }

    private String resolveDrugName(Long drugId, Map<Long, Drug> drugMap) {
        Drug drug = drugMap.get(drugId);
        return drug == null ? "未知药品" : drug.getDrugName();
    }

    private String resolveRiskLevel(List<PrescriptionStockWarningVO> stockWarnings,
                                    List<DrugInteractionRiskVO> interactionRisks) {
        int rank = 0;
        for (PrescriptionStockWarningVO warning : stockWarnings) {
            if (Boolean.FALSE.equals(warning.getStockEnough())) {
                rank = Math.max(rank, 3);
            } else if (!"库存充足".equals(warning.getWarningMessage())) {
                rank = Math.max(rank, 1);
            }
        }
        for (DrugInteractionRiskVO risk : interactionRisks) {
            rank = Math.max(rank, riskRank(risk.getRiskLevel()));
        }
        return switch (rank) {
            case 4 -> "FORBIDDEN";
            case 3 -> "HIGH";
            case 2 -> "MEDIUM";
            case 1 -> "LOW";
            default -> "OK";
        };
    }

    private int riskRank(String riskLevel) {
        if ("FORBIDDEN".equals(riskLevel)) {
            return 4;
        }
        if ("HIGH".equals(riskLevel)) {
            return 3;
        }
        if ("MEDIUM".equals(riskLevel)) {
            return 2;
        }
        if ("LOW".equals(riskLevel)) {
            return 1;
        }
        return 0;
    }

    private String buildStockResult(List<PrescriptionStockWarningVO> stockWarnings) {
        return stockWarnings.stream()
                .map(warning -> warning.getDrugName() + "：需 " + warning.getRequestedQuantity()
                        + "，库存 " + warning.getCurrentStock() + "，" + warning.getWarningMessage())
                .collect(Collectors.joining("；"));
    }

    private String buildInteractionResult(List<DrugInteractionRiskVO> interactionRisks) {
        if (interactionRisks.isEmpty()) {
            return "未发现处方内药物相互作用规则命中";
        }
        return interactionRisks.stream()
                .map(risk -> risk.getDrugAName() + " + " + risk.getDrugBName() + "："
                        + risk.getRiskLevel() + "，" + risk.getDescription())
                .collect(Collectors.joining("；"));
    }

    private String buildSummary(String riskLevel,
                                List<PrescriptionStockWarningVO> stockWarnings,
                                List<DrugInteractionRiskVO> interactionRisks) {
        long insufficientCount = stockWarnings.stream()
                .filter(warning -> Boolean.FALSE.equals(warning.getStockEnough()))
                .count();
        if ("OK".equals(riskLevel)) {
            return "处方库存充足，未命中相互作用规则，可进入人工复核。";
        }
        return "综合风险：" + riskLevel + "；库存异常 " + insufficientCount
                + " 项，相互作用风险 " + interactionRisks.size() + " 项，请药剂师重点复核。";
    }

    private boolean hasBlocker(PrescriptionAuditCheckVO check) {
        boolean insufficientStock = check.getStockWarnings().stream()
                .anyMatch(warning -> Boolean.FALSE.equals(warning.getStockEnough()));
        return insufficientStock || "FORBIDDEN".equals(check.getRiskLevel());
    }

    private String normalizeAuditResult(String auditResult) {
        if (RESULT_APPROVED.equals(auditResult) || RESULT_REJECTED.equals(auditResult)
                || RESULT_NEED_MODIFY.equals(auditResult)) {
            return auditResult;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "审核结果只能为 APPROVED、REJECTED 或 NEED_MODIFY");
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}
