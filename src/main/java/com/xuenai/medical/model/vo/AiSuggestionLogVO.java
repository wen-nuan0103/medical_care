package com.xuenai.medical.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiSuggestionLogVO {

    private Long id;
    private Long userId;
    private String roleCode;
    private String scenario;
    private Long targetId;
    private String modelName;
    private String promptSummary;
    private String aiOutput;
    private String confirmStatus;
    private String errorMessage;
    private LocalDateTime createTime;
}
