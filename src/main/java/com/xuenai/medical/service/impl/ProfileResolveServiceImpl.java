package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.DoctorMapper;
import com.xuenai.medical.mapper.PatientProfileMapper;
import com.xuenai.medical.mapper.PharmacistProfileMapper;
import com.xuenai.medical.model.entity.DoctorProfile;
import com.xuenai.medical.model.entity.PatientProfile;
import com.xuenai.medical.model.entity.PharmacistProfile;
import com.xuenai.medical.service.ProfileResolveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileResolveServiceImpl implements ProfileResolveService {

    private final DoctorMapper doctorMapper;
    private final PatientProfileMapper patientProfileMapper;
    private final PharmacistProfileMapper pharmacistProfileMapper;

    @Override
    public Long getDoctorProfileId(Long userId) {
        LambdaQueryWrapper<DoctorProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorProfile::getUserId, userId).last("LIMIT 1");
        DoctorProfile profile = doctorMapper.selectOne(wrapper);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到该用户的医生档案");
        }
        return profile.getId();
    }

    @Override
    public Long getPatientProfileId(Long userId) {
        LambdaQueryWrapper<PatientProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientProfile::getUserId, userId).last("LIMIT 1");
        PatientProfile profile = patientProfileMapper.selectOne(wrapper);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到该用户的患者档案");
        }
        return profile.getId();
    }

    @Override
    public Long getPharmacistProfileId(Long userId) {
        LambdaQueryWrapper<PharmacistProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PharmacistProfile::getUserId, userId).last("LIMIT 1");
        PharmacistProfile profile = pharmacistProfileMapper.selectOne(wrapper);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到该用户的药剂师档案");
        }
        return profile.getId();
    }

    @Override
    public Long getUserIdByDoctorProfileId(Long doctorProfileId) {
        DoctorProfile profile = doctorMapper.selectById(doctorProfileId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "医生档案不存在");
        }
        return profile.getUserId();
    }

    @Override
    public Long getUserIdByPatientProfileId(Long patientProfileId) {
        PatientProfile profile = patientProfileMapper.selectById(patientProfileId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "患者档案不存在");
        }
        return profile.getUserId();
    }
}
