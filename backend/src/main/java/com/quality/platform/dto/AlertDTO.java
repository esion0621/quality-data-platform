package com.quality.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlertDTO {
    private Long id;
    private Integer ruleId;
    private LocalDateTime triggerTime;
    private String abnormalValue;
    private String thresholdValue;
    private String alertMethod;
    private Byte confirmed;
    private String confirmedBy;
    private LocalDateTime confirmedTime;
}
