package com.quality.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskInstanceDTO {
    private Long id;
    private Integer ruleId;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String resultSummary;
    private String errorMsg;
}
