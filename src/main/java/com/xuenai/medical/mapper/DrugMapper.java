package com.xuenai.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuenai.medical.model.entity.Drug;
import com.xuenai.medical.model.vo.DrugDetailVO;
import com.xuenai.medical.model.vo.DrugVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 药品 Mapper
 */
@Mapper
public interface DrugMapper extends BaseMapper<Drug> {

    /**
     * 分页查询药品列表（含分类名称）
     */
    List<DrugVO> selectDrugVOPage(@Param("offset") long offset,
                                  @Param("limit") int limit,
                                  @Param("keyword") String keyword,
                                  @Param("categoryId") Long categoryId,
                                  @Param("prescriptionRequired") Integer prescriptionRequired,
                                  @Param("insuranceCovered") Integer insuranceCovered,
                                  @Param("status") Integer status);

    /**
     * 统计药品数量
     */
    long countDrugs(@Param("keyword") String keyword,
                    @Param("categoryId") Long categoryId,
                    @Param("prescriptionRequired") Integer prescriptionRequired,
                    @Param("insuranceCovered") Integer insuranceCovered,
                    @Param("status") Integer status);

    /**
     * 查询药品详情（含分类名称）
     */
    DrugDetailVO selectDrugDetailById(@Param("id") Long id);
}
