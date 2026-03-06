package com.quality.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "quality_task_instance")
public class TaskInstanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer ruleId;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(columnDefinition = "ENUM('RUNNING','SUCCESS','FAILED')")
    private String status = "RUNNING";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String resultSummary;

    @Column(columnDefinition = "text")
    private String errorMsg;

    @Column(updatable = false, insertable = false)
    private Instant createdAt;
}
