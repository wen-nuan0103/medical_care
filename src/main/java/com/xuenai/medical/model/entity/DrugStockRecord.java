package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("drug_stock_record")
public class DrugStockRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long drugId;
    private String changeType;
    private Integer changeQuantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private Long relatedOrderId;
    private Long operatorId;
    private String reason;
    private LocalDateTime createTime;
}
