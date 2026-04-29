package com.xuenai.medical.service;

import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.model.vo.DoctorVO;

/**
 * 医生 Service 接口
 */
public interface DoctorService {

    /**
     * 分页查询医生列表
     */
    PageResult<DoctorVO> pageDoctors(int current, int pageSize, String department, String title, Integer serviceStatus, String keyword);

    /**
     * 查询医生详情
     */
    DoctorDetailVO detail(Long doctorId);

    /**
     * 更新医生接诊状态
     */
    boolean updateServiceStatus(Long doctorId, Integer status);
}
