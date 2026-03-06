package com.quality.platform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "quality_alert")
public class AlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ruleId;

    @Column(nullable = false)
    private LocalDateTime triggerTime;

    private String abnormalValue;
    private String thresholdValue;

    @Column(columnDefinition = "ENUM('EMAIL','DINGTALK')")
    private String alertMethod;

    private Byte confirmed = 0;
    private String confirmedBy;
    private LocalDateTime confirmedTime;

    @Column(updatable = false, insertable = false)
    private Instant createdAt;
}
