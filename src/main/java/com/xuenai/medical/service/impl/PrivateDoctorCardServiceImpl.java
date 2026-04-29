package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.mapper.DoctorMapper;
import com.xuenai.medical.mapper.PatientProfileMapper;
import com.xuenai.medical.mapper.PrivateDoctorCardMapper;
import com.xuenai.medical.mapper.UserMapper;
import com.xuenai.medical.model.entity.DoctorCardOrder;
import com.xuenai.medical.model.entity.DoctorCardPlan;
import com.xuenai.medical.model.entity.PatientProfile;
import com.xuenai.medical.model.entity.PrivateDoctorCard;
import com.xuenai.medical.model.entity.SysUser;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.model.vo.PrivateDoctorCardVO;
import com.xuenai.medical.service.PrivateDoctorCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateDoctorCardServiceImpl implements PrivateDoctorCardService {

    private final PrivateDoctorCardMapper privateDoctorCardMapper;
    private final DoctorMapper doctorMapper;
    private final PatientProfileMapper patientProfileMapper;
    private final UserMapper userMapper;

    @Override
    public PrivateDoctorCard activateCard(DoctorCardOrder order, DoctorCardPlan plan) {
        PrivateDoctorCard card = new PrivateDoctorCard();
        card.setPatientId(order.getPatientId());
        card.setDoctorId(order.getDoctorId());
        card.setPlanId(order.getPlanId());
        card.setOrderId(order.getId());
        card.setCardType(plan.getCardType());

        LocalDateTime now = LocalDateTime.now();
        card.setStartTime(now);
        card.setExpireTime(now.plusDays(plan.getValidDays()));

        card.setTotalTimes(plan.getConsultationTimes());
        card.setRemainingTimes(plan.getConsultationTimes());
        card.setTotalMinutes(plan.getTotalMinutes());
        card.setRemainingMinutes(plan.getTotalMinutes());
        card.setGiftedMinutes(0);
        card.setStatus("ACTIVE");

        privateDoctorCardMapper.insert(card);
        return card;
    }

    @Override
    public boolean hasActiveCard(Long patientProfileId, Long doctorProfileId) {
        return getActiveCard(patientProfileId, doctorProfileId) != null;
    }

    @Override
    public PrivateDoctorCard getActiveCard(Long patientProfileId, Long doctorProfileId) {
        LambdaQueryWrapper<PrivateDoctorCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateDoctorCard::getPatientId, patientProfileId)
               .eq(PrivateDoctorCard::getDoctorId, doctorProfileId)
               .eq(PrivateDoctorCard::getStatus, "ACTIVE")
               .gt(PrivateDoctorCard::getExpireTime, LocalDateTime.now())
               .gt(PrivateDoctorCard::getRemainingTimes, 0)
               .last("LIMIT 1");
        return privateDoctorCardMapper.selectOne(wrapper);
    }

    @Override
    public List<PrivateDoctorCardVO> getMyCards(Long patientProfileId) {
        LambdaQueryWrapper<PrivateDoctorCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateDoctorCard::getPatientId, patientProfileId)
               .orderByDesc(PrivateDoctorCard::getCreateTime);

        List<PrivateDoctorCard> cards = privateDoctorCardMapper.selectList(wrapper);

        return cards.stream().map(card -> {
            PrivateDoctorCardVO vo = new PrivateDoctorCardVO();
            BeanUtils.copyProperties(card, vo);

            // 获取医生信息
            DoctorDetailVO doctorDetail = doctorMapper.selectDoctorDetailById(card.getDoctorId());
            if (doctorDetail != null) {
                vo.setDoctorName(doctorDetail.realName());
                vo.setDoctorTitle(doctorDetail.title());
                vo.setDoctorAvatar(doctorDetail.avatarUrl());
            }

            // 检查过期
            if ("ACTIVE".equals(card.getStatus()) && card.getExpireTime().isBefore(LocalDateTime.now())) {
                vo.setStatus("EXPIRED");
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateCard(PrivateDoctorCard card) {
        privateDoctorCardMapper.updateById(card);
    }

    @Override
    public List<PrivateDoctorCardVO> getCardsByDoctor(Long doctorProfileId) {
        LambdaQueryWrapper<PrivateDoctorCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateDoctorCard::getDoctorId, doctorProfileId)
               .orderByDesc(PrivateDoctorCard::getCreateTime);

        List<PrivateDoctorCard> cards = privateDoctorCardMapper.selectList(wrapper);

        return cards.stream().map(card -> {
            PrivateDoctorCardVO vo = new PrivateDoctorCardVO();
            BeanUtils.copyProperties(card, vo);

            // 获取患者信息
            PatientProfile patientProfile = patientProfileMapper.selectById(card.getPatientId());
            if (patientProfile != null) {
                SysUser patientUser = userMapper.selectById(patientProfile.getUserId());
                if (patientUser != null) {
                    vo.setDoctorName(patientUser.getRealName()); // 复用字段显示患者名
                    vo.setDoctorAvatar(patientUser.getAvatarUrl());
                }
            }

            // 检查过期
            if ("ACTIVE".equals(card.getStatus()) && card.getExpireTime().isBefore(LocalDateTime.now())) {
                vo.setStatus("EXPIRED");
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
