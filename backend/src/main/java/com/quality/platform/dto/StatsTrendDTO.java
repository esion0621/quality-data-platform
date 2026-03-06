package com.quality.platform.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StatsTrendDTO {
    private LocalDate date;
    private Long totalCount;
    private Long successCount;
    private Long failCount;
}
