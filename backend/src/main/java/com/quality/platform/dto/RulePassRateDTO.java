package com.quality.platform.dto;

import lombok.Data;

@Data
public class RulePassRateDTO {
    private Integer ruleId;
    private String ruleName;
    private Double passRate;  // 通过率（0-1）
}
