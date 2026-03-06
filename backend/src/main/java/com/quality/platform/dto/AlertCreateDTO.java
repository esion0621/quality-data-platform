package com.quality.platform.dto;

import lombok.Data;

@Data
public class AlertCreateDTO {
    private Integer ruleId;
    private String abnormalValue;
    private String thresholdValue;
    private String alertMethod;
}
