package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.InsuranceCardMapper;
import com.xuenai.medical.model.dto.InsuranceCardBindDTO;
import com.xuenai.medical.model.entity.InsuranceCard;
import com.xuenai.medical.model.vo.InsuranceCardVO;
import com.xuenai.medical.service.InsuranceCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceCardServiceImpl implements InsuranceCardService {

    private static final BigDecimal DEFAULT_REIMBURSEMENT_RATE = new BigDecimal("0.70");

    private final InsuranceCardMapper insuranceCardMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceCardVO bind(Long patientProfileId, InsuranceCardBindDTO dto) {
        LambdaQueryWrapper<InsuranceCard> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(InsuranceCard::getCardNo, dto.getCardNo()).last("LIMIT 1");
        if (insuranceCardMapper.selectOne(duplicateWrapper) != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "insurance card already exists");
        }

        InsuranceCard card = new InsuranceCard();
        card.setPatientId(patientProfileId);
        card.setCardNo(dto.getCardNo());
        card.setHolderName(dto.getHolderName());
        card.setHolderIdCard(dto.getHolderIdCard());
        card.setBalance(dto.getBalance());
        card.setReimbursementRate(dto.getReimbursementRate() == null
                ? DEFAULT_REIMBURSEMENT_RATE
                : dto.getReimbursementRate());
        card.setStatus("ACTIVE");
        card.setBindTime(LocalDateTime.now());
        card.setCreateBy(UserContext.getUserId());
        card.setUpdateBy(UserContext.getUserId());
        insuranceCardMapper.insert(card);
        return toVO(card);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceCardVO> listByPatient(Long patientProfileId) {
        LambdaQueryWrapper<InsuranceCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InsuranceCard::getPatientId, patientProfileId)
                .orderByDesc(InsuranceCard::getCreateTime);
        return insuranceCardMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .toList();
    }

    private InsuranceCardVO toVO(InsuranceCard card) {
        InsuranceCardVO vo = new InsuranceCardVO();
        BeanUtils.copyProperties(card, vo);
        return vo;
    }
}
