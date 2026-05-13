package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.DrugMapper;
import com.xuenai.medical.mapper.DrugStockRecordMapper;
import com.xuenai.medical.mapper.InsuranceCardMapper;
import com.xuenai.medical.mapper.InsuranceDrugCatalogMapper;
import com.xuenai.medical.mapper.InsurancePaymentRecordMapper;
import com.xuenai.medical.mapper.MedicineOrderItemMapper;
import com.xuenai.medical.mapper.MedicineOrderMapper;
import com.xuenai.medical.mapper.PrescriptionItemMapper;
import com.xuenai.medical.mapper.PrescriptionMapper;
import com.xuenai.medical.model.dto.CreateMedicineOrderDTO;
import com.xuenai.medical.model.dto.PayMedicineOrderDTO;
import com.xuenai.medical.model.entity.Drug;
import com.xuenai.medical.model.entity.DrugStockRecord;
import com.xuenai.medical.model.entity.InsuranceCard;
import com.xuenai.medical.model.entity.InsuranceDrugCatalog;
import com.xuenai.medical.model.entity.InsurancePaymentRecord;
import com.xuenai.medical.model.entity.MedicineOrder;
import com.xuenai.medical.model.entity.MedicineOrderItem;
import com.xuenai.medical.model.entity.Prescription;
import com.xuenai.medical.model.entity.PrescriptionItem;
import com.xuenai.medical.model.vo.InsurancePaymentRecordVO;
import com.xuenai.medical.model.vo.MedicineOrderItemVO;
import com.xuenai.medical.model.vo.MedicineOrderVO;
import com.xuenai.medical.model.vo.PrescriptionVO;
import com.xuenai.medical.service.MedicineOrderService;
import com.xuenai.medical.service.MedicationReminderService;
import com.xuenai.medical.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineOrderServiceImpl implements MedicineOrderService {

    private static final String PRESCRIPTION_APPROVED = "APPROVED";
    private static final String PRESCRIPTION_PAID = "PAID";
    private static final String ORDER_UNPAID = "UNPAID";
    private static final String ORDER_PAID = "PAID";
    private static final String ORDER_CANCELED = "CANCELED";
    private static final String ORDER_COMPLETED = "COMPLETED";

    private final MedicineOrderMapper medicineOrderMapper;
    private final MedicineOrderItemMapper medicineOrderItemMapper;
    private final InsuranceCardMapper insuranceCardMapper;
    private final InsuranceDrugCatalogMapper insuranceDrugCatalogMapper;
    private final InsurancePaymentRecordMapper insurancePaymentRecordMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final DrugMapper drugMapper;
    private final DrugStockRecordMapper drugStockRecordMapper;
    private final PrescriptionService prescriptionService;
    private final MedicationReminderService medicationReminderService;

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionVO> listPurchasablePrescriptions(Long patientProfileId) {
        return prescriptionService.listForPatient(patientProfileId, PRESCRIPTION_APPROVED).stream()
                .filter(prescription -> prescription.getValidUntil() == null
                        || prescription.getValidUntil().isAfter(LocalDateTime.now()))
                .filter(prescription -> !hasActiveOrder(prescription.getId()))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedicineOrderVO createFromPrescription(Long patientProfileId, CreateMedicineOrderDTO dto) {
        Prescription prescription = validatePrescriptionPurchasable(patientProfileId, dto.getPrescriptionId());
        InsuranceCard card = getActiveCard(patientProfileId, dto.getInsuranceCardId());
        if (hasActiveOrder(prescription.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "prescription already has an active medicine order");
        }

        List<PrescriptionItem> prescriptionItems = listPrescriptionItems(prescription.getId());
        Map<Long, Drug> drugMap = loadDrugMap(prescriptionItems.stream().map(PrescriptionItem::getDrugId).toList());
        validateStockEnough(prescriptionItems, drugMap);
        Map<Long, InsuranceDrugCatalog> catalogMap = loadCatalogMap(prescriptionItems);

        OrderAmount amount = calculateAmount(prescriptionItems, drugMap, catalogMap, card);
        if (card.getBalance().compareTo(amount.totalAmount()) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "insurance card balance is insufficient");
        }
        MedicineOrder order = new MedicineOrder();
        order.setOrderNo(generateOrderNo());
        order.setPatientId(patientProfileId);
        order.setPrescriptionId(prescription.getId());
        order.setInsuranceCardId(card.getId());
        order.setTotalAmount(amount.totalAmount());
        order.setInsuranceAmount(amount.insuranceAmount());
        order.setSelfAmount(amount.selfAmount());
        order.setStatus(ORDER_UNPAID);
        order.setCreateBy(UserContext.getUserId());
        order.setUpdateBy(UserContext.getUserId());
        medicineOrderMapper.insert(order);

        for (MedicineOrderItem item : amount.items()) {
            item.setOrderId(order.getId());
            medicineOrderItemMapper.insert(item);
        }
        return buildVO(order.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineOrderVO> listOrders(Long patientProfileId, String status) {
        LambdaQueryWrapper<MedicineOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicineOrder::getPatientId, patientProfileId);
        if (status != null && !status.isBlank()) {
            wrapper.eq(MedicineOrder::getStatus, status);
        }
        wrapper.orderByDesc(MedicineOrder::getCreateTime);
        return medicineOrderMapper.selectList(wrapper).stream()
                .map(order -> buildVO(order.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineOrderVO detail(Long patientProfileId, Long orderId) {
        MedicineOrder order = getPatientOrder(patientProfileId, orderId);
        return buildVO(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedicineOrderVO payWithInsurance(Long patientProfileId, Long orderId, PayMedicineOrderDTO dto) {
        MedicineOrder order = getPatientOrder(patientProfileId, orderId);
        if (!ORDER_UNPAID.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "only unpaid order can be paid");
        }
        if (!order.getInsuranceCardId().equals(dto.getInsuranceCardId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "insurance card does not match order");
        }

        Prescription prescription = validatePrescriptionPurchasable(patientProfileId, order.getPrescriptionId());
        InsuranceCard card = getActiveCard(patientProfileId, dto.getInsuranceCardId());
        if (card.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "insurance card balance is insufficient");
        }

        List<MedicineOrderItem> orderItems = listOrderItems(order.getId());
        Map<Long, Drug> drugMap = loadDrugMap(orderItems.stream().map(MedicineOrderItem::getDrugId).toList());
        validateOrderStockEnough(orderItems, drugMap);

        for (MedicineOrderItem item : orderItems) {
            Drug drug = drugMap.get(item.getDrugId());
            int before = drug.getStockQuantity();
            int after = before - item.getQuantity();
            LambdaUpdateWrapper<Drug> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Drug::getId, drug.getId())
                    .ge(Drug::getStockQuantity, item.getQuantity())
                    .set(Drug::getStockQuantity, after)
                    .set(Drug::getUpdateBy, UserContext.getUserId());
            int updated = drugMapper.update(null, updateWrapper);
            if (updated == 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "drug stock changed, please retry payment");
            }
            insertStockRecord(drug.getId(), order.getId(), item.getQuantity(), before, after);
        }

        BigDecimal beforeBalance = card.getBalance();
        BigDecimal afterBalance = beforeBalance.subtract(order.getTotalAmount()).setScale(2, RoundingMode.HALF_UP);
        card.setBalance(afterBalance);
        card.setUpdateBy(UserContext.getUserId());
        insuranceCardMapper.updateById(card);

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(ORDER_PAID);
        order.setPayTime(now);
        order.setUpdateBy(UserContext.getUserId());
        medicineOrderMapper.updateById(order);

        prescription.setStatus(PRESCRIPTION_PAID);
        prescriptionMapper.updateById(prescription);

        InsurancePaymentRecord payment = new InsurancePaymentRecord();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(order.getId());
        payment.setPatientId(patientProfileId);
        payment.setInsuranceCardId(card.getId());
        payment.setBeforeBalance(beforeBalance);
        payment.setPaidAmount(order.getTotalAmount());
        payment.setAfterBalance(afterBalance);
        payment.setStatus("SUCCESS");
        payment.setPayTime(now);
        payment.setCreateBy(UserContext.getUserId());
        payment.setUpdateBy(UserContext.getUserId());
        insurancePaymentRecordMapper.insert(payment);
        medicationReminderService.generateFromPaidOrder(order);
        return buildVO(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedicineOrderVO cancel(Long patientProfileId, Long orderId) {
        MedicineOrder order = getPatientOrder(patientProfileId, orderId);
        if (!ORDER_UNPAID.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "only unpaid order can be canceled");
        }
        order.setStatus(ORDER_CANCELED);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateBy(UserContext.getUserId());
        medicineOrderMapper.updateById(order);
        return buildVO(order.getId());
    }

    private Prescription validatePrescriptionPurchasable(Long patientProfileId, Long prescriptionId) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "prescription not found");
        }
        if (!patientProfileId.equals(prescription.getPatientId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission for this prescription");
        }
        if (!PRESCRIPTION_APPROVED.equals(prescription.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "only approved prescription can be purchased");
        }
        if (prescription.getValidUntil() != null && prescription.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "prescription has expired");
        }
        return prescription;
    }

    private InsuranceCard getActiveCard(Long patientProfileId, Long cardId) {
        InsuranceCard card = insuranceCardMapper.selectById(cardId);
        if (card == null || !patientProfileId.equals(card.getPatientId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "insurance card not found");
        }
        if (!"ACTIVE".equals(card.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "insurance card is not active");
        }
        return card;
    }

    private MedicineOrder getPatientOrder(Long patientProfileId, Long orderId) {
        MedicineOrder order = medicineOrderMapper.selectById(orderId);
        if (order == null || !patientProfileId.equals(order.getPatientId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "medicine order not found");
        }
        return order;
    }

    private boolean hasActiveOrder(Long prescriptionId) {
        LambdaQueryWrapper<MedicineOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicineOrder::getPrescriptionId, prescriptionId)
                .in(MedicineOrder::getStatus, List.of(ORDER_UNPAID, ORDER_PAID, ORDER_COMPLETED))
                .last("LIMIT 1");
        return medicineOrderMapper.selectOne(wrapper) != null;
    }

    private List<PrescriptionItem> listPrescriptionItems(Long prescriptionId) {
        LambdaQueryWrapper<PrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionItem::getPrescriptionId, prescriptionId)
                .orderByAsc(PrescriptionItem::getId);
        List<PrescriptionItem> items = prescriptionItemMapper.selectList(wrapper);
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "prescription has no medicines");
        }
        return items;
    }

    private List<MedicineOrderItem> listOrderItems(Long orderId) {
        LambdaQueryWrapper<MedicineOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicineOrderItem::getOrderId, orderId)
                .orderByAsc(MedicineOrderItem::getId);
        return medicineOrderItemMapper.selectList(wrapper);
    }

    private Map<Long, Drug> loadDrugMap(Collection<Long> drugIds) {
        if (drugIds == null || drugIds.isEmpty()) {
            return Map.of();
        }
        return drugMapper.selectBatchIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, Function.identity()));
    }

    private Map<Long, InsuranceDrugCatalog> loadCatalogMap(List<PrescriptionItem> items) {
        List<Long> drugIds = items.stream().map(PrescriptionItem::getDrugId).distinct().toList();
        if (drugIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<InsuranceDrugCatalog> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(InsuranceDrugCatalog::getDrugId, drugIds)
                .eq(InsuranceDrugCatalog::getStatus, 1);
        return insuranceDrugCatalogMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(InsuranceDrugCatalog::getDrugId, Function.identity()));
    }

    private void validateStockEnough(List<PrescriptionItem> items, Map<Long, Drug> drugMap) {
        for (PrescriptionItem item : items) {
            Drug drug = drugMap.get(item.getDrugId());
            if (drug == null || drug.getStockQuantity() == null || drug.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, item.getDrugName() + " stock is insufficient");
            }
        }
    }

    private void validateOrderStockEnough(List<MedicineOrderItem> items, Map<Long, Drug> drugMap) {
        for (MedicineOrderItem item : items) {
            Drug drug = drugMap.get(item.getDrugId());
            if (drug == null || drug.getStockQuantity() == null || drug.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, item.getDrugName() + " stock is insufficient");
            }
        }
    }

    private OrderAmount calculateAmount(List<PrescriptionItem> prescriptionItems,
                                        Map<Long, Drug> drugMap,
                                        Map<Long, InsuranceDrugCatalog> catalogMap,
                                        InsuranceCard card) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal insurance = BigDecimal.ZERO;
        List<MedicineOrderItem> orderItems = new java.util.ArrayList<>();
        for (PrescriptionItem prescriptionItem : prescriptionItems) {
            Drug drug = drugMap.get(prescriptionItem.getDrugId());
            BigDecimal amount = prescriptionItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(prescriptionItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal itemInsurance = calculateInsuranceAmount(amount, drug, catalogMap.get(prescriptionItem.getDrugId()), card);

            MedicineOrderItem orderItem = new MedicineOrderItem();
            orderItem.setDrugId(prescriptionItem.getDrugId());
            orderItem.setDrugName(prescriptionItem.getDrugName());
            orderItem.setSpecification(prescriptionItem.getSpecification());
            orderItem.setQuantity(prescriptionItem.getQuantity());
            orderItem.setUnitPrice(prescriptionItem.getUnitPrice());
            orderItem.setAmount(amount);
            orderItem.setInsuranceCovered(isInsuranceCovered(drug, catalogMap.get(prescriptionItem.getDrugId())) ? 1 : 0);
            orderItem.setInsuranceAmount(itemInsurance);
            orderItems.add(orderItem);

            total = total.add(amount);
            insurance = insurance.add(itemInsurance);
        }
        total = total.setScale(2, RoundingMode.HALF_UP);
        insurance = insurance.setScale(2, RoundingMode.HALF_UP);
        return new OrderAmount(total, insurance, total.subtract(insurance).setScale(2, RoundingMode.HALF_UP), orderItems);
    }

    private BigDecimal calculateInsuranceAmount(BigDecimal amount,
                                                Drug drug,
                                                InsuranceDrugCatalog catalog,
                                                InsuranceCard card) {
        if (!isInsuranceCovered(drug, catalog)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal rate = catalog.getReimbursementRate() == null ? card.getReimbursementRate() : catalog.getReimbursementRate();
        BigDecimal insuranceAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        if (catalog.getMaxReimbursementAmount() != null
                && insuranceAmount.compareTo(catalog.getMaxReimbursementAmount()) > 0) {
            insuranceAmount = catalog.getMaxReimbursementAmount();
        }
        return insuranceAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isInsuranceCovered(Drug drug, InsuranceDrugCatalog catalog) {
        return drug != null && Integer.valueOf(1).equals(drug.getInsuranceCovered()) && catalog != null;
    }

    private void insertStockRecord(Long drugId, Long orderId, Integer quantity, Integer before, Integer after) {
        DrugStockRecord record = new DrugStockRecord();
        record.setDrugId(drugId);
        record.setChangeType("OUT");
        record.setChangeQuantity(-quantity);
        record.setBeforeQuantity(before);
        record.setAfterQuantity(after);
        record.setRelatedOrderId(orderId);
        record.setOperatorId(UserContext.getUserId());
        record.setReason("medicine order insurance payment");
        record.setCreateTime(LocalDateTime.now());
        drugStockRecordMapper.insert(record);
    }

    private MedicineOrderVO buildVO(Long orderId) {
        MedicineOrder order = medicineOrderMapper.selectById(orderId);
        MedicineOrderVO vo = new MedicineOrderVO();
        BeanUtils.copyProperties(order, vo);

        Prescription prescription = prescriptionMapper.selectById(order.getPrescriptionId());
        if (prescription != null) {
            vo.setPrescriptionNo(prescription.getPrescriptionNo());
            vo.setDiagnosis(prescription.getDiagnosis());
        }
        InsuranceCard card = insuranceCardMapper.selectById(order.getInsuranceCardId());
        if (card != null) {
            vo.setInsuranceCardNo(card.getCardNo());
        }
        vo.setItems(listOrderItems(orderId).stream().map(this::toItemVO).toList());
        vo.setPaymentRecord(buildLatestPayment(orderId));
        return vo;
    }

    private MedicineOrderItemVO toItemVO(MedicineOrderItem item) {
        MedicineOrderItemVO vo = new MedicineOrderItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    private InsurancePaymentRecordVO buildLatestPayment(Long orderId) {
        LambdaQueryWrapper<InsurancePaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InsurancePaymentRecord::getOrderId, orderId)
                .orderByDesc(InsurancePaymentRecord::getPayTime)
                .last("LIMIT 1");
        InsurancePaymentRecord record = insurancePaymentRecordMapper.selectOne(wrapper);
        if (record == null) {
            return null;
        }
        InsurancePaymentRecordVO vo = new InsurancePaymentRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    private String generateOrderNo() {
        return "MO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private record OrderAmount(
            BigDecimal totalAmount,
            BigDecimal insuranceAmount,
            BigDecimal selfAmount,
            List<MedicineOrderItem> items
    ) {
    }
}
