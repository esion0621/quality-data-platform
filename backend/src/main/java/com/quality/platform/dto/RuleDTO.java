package com.quality.platform.dto;

import lombok.Data;

@Data
public class RuleDTO {
    private Integer id;
    private String ruleName;
    private String sourceType;
    private String sourceConfig;
    private String ruleType;
    private String ruleParams;
    private String scheduleConfig;
    private String windowConfig;
    private Byte status;
}
