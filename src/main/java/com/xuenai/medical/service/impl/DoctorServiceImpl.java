package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.DoctorMapper;
import com.xuenai.medical.model.entity.DoctorProfile;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.model.vo.DoctorVO;
import com.xuenai.medical.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医生 Service 实现
 */
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;

    @Override
    public PageResult<DoctorVO> pageDoctors(int current, int pageSize, String department, String title, Integer serviceStatus, String keyword) {
        int safePageSize = Math.min(100, Math.max(1, pageSize));
        int safeCurrent = Math.max(1, current);
        long offset = (long) (safeCurrent - 1) * safePageSize;

        long total = doctorMapper.countDoctors(department, title, serviceStatus, keyword);
        List<DoctorVO> records = doctorMapper.selectDoctorVOPage(offset, safePageSize, department, title, serviceStatus, keyword);
        return PageResult.of(records, total, safeCurrent, safePageSize);
    }

    @Override
    public DoctorDetailVO detail(Long doctorId) {
        DoctorDetailVO detail = doctorMapper.selectDoctorDetailById(doctorId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "医生不存在");
        }
        return detail;
    }

    @Override
    public boolean updateServiceStatus(Long doctorId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "服务状态只能是 0 或 1");
        }
        LambdaUpdateWrapper<DoctorProfile> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DoctorProfile::getId, doctorId)
               .eq(DoctorProfile::getDeleted, 0)
               .set(DoctorProfile::getServiceStatus, status);
        int updated = doctorMapper.update(null, wrapper);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "医生不存在");
        }
        return true;
    }
}
