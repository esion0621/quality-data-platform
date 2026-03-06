package com.quality.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Data
@Entity
@Table(name = "quality_rule")
public class RuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String ruleName;

    @Column(nullable = false, columnDefinition = "ENUM('HDFS','KAFKA')")
    private String sourceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private String sourceConfig;

    @Column(nullable = false, length = 50)
    private String ruleType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private String ruleParams;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String scheduleConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String windowConfig;

    private Byte status = 1;

    @Column(updatable = false, insertable = false)
    private Instant createdAt;

    @Column(insertable = false)
    private Instant updatedAt;
}
