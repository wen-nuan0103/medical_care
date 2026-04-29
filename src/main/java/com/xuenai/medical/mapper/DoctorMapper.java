package com.xuenai.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuenai.medical.model.entity.DoctorProfile;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.model.vo.DoctorVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医生档案 Mapper
 */
@Mapper
public interface DoctorMapper extends BaseMapper<DoctorProfile> {

    /**
     * 分页查询医生列表（含用户信息）
     */
    List<DoctorVO> selectDoctorVOPage(@Param("offset") long offset,
                                      @Param("limit") int limit,
                                      @Param("department") String department,
                                      @Param("title") String title,
                                      @Param("serviceStatus") Integer serviceStatus,
                                      @Param("keyword") String keyword);

    /**
     * 统计医生数量
     */
    long countDoctors(@Param("department") String department,
                      @Param("title") String title,
                      @Param("serviceStatus") Integer serviceStatus,
                      @Param("keyword") String keyword);

    /**
     * 查询医生详情（含用户信息）
     */
    DoctorDetailVO selectDoctorDetailById(@Param("doctorId") Long doctorId);
}
