package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pharmacist_profile")
public class PharmacistProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String certificateNo;
    private String hospitalOrOrg;
    private Integer workingYears;
    private String auditStatus;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
