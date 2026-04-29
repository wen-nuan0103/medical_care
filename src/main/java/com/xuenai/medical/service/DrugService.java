package com.xuenai.medical.service;

import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.model.dto.DrugSaveDTO;
import com.xuenai.medical.model.dto.StockUpdateDTO;
import com.xuenai.medical.model.vo.DrugDetailVO;
import com.xuenai.medical.model.vo.DrugVO;

/**
 * 药品 Service 接口
 */
public interface DrugService {

    /**
     * 分页查询药品列表
     */
    PageResult<DrugVO> pageDrugs(int current, int pageSize, String keyword, Long categoryId,
                                 Integer prescriptionRequired, Integer insuranceCovered, Integer status);

    /**
     * 查询药品详情
     */
    DrugDetailVO detail(Long id);

    /**
     * 新增药品
     */
    DrugDetailVO create(DrugSaveDTO request);

    /**
     * 修改药品
     */
    DrugDetailVO update(Long id, DrugSaveDTO request);

    /**
     * 更新药品库存
     */
    DrugDetailVO updateStock(Long id, StockUpdateDTO request);
}
