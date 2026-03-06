-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS quality_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE quality_platform;

-- 规则表
CREATE TABLE IF NOT EXISTS quality_rule (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    source_type ENUM('HDFS', 'KAFKA') NOT NULL COMMENT '数据源类型',
    source_config JSON NOT NULL COMMENT '数据源配置（路径、topic、格式等）',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型（NULL_CHECK等）',
    rule_params JSON NOT NULL COMMENT '规则参数',
    schedule_config JSON COMMENT '离线调度配置（cron等）',
    window_config JSON COMMENT '实时窗口配置（长度、间隔）',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量规则表';

-- 任务实例表
CREATE TABLE IF NOT EXISTS quality_task_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '实例ID',
    rule_id INT NOT NULL COMMENT '规则ID',
    scheduled_time DATETIME NOT NULL COMMENT '计划执行时间',
    start_time DATETIME COMMENT '实际开始时间',
    end_time DATETIME COMMENT '结束时间',
    status ENUM('RUNNING', 'SUCCESS', 'FAILED') DEFAULT 'RUNNING' COMMENT '执行状态',
    result_summary JSON COMMENT '结果摘要（总行数、异常行数、通过率等）',
    error_msg TEXT COMMENT '失败错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rule_id) REFERENCES quality_rule(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_scheduled_time (scheduled_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线任务实例表';

-- 告警记录表
CREATE TABLE IF NOT EXISTS quality_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id INT NOT NULL COMMENT '规则ID',
    trigger_time DATETIME NOT NULL COMMENT '触发时间',
    abnormal_value VARCHAR(100) COMMENT '异常值（如异常率）',
    threshold_value VARCHAR(100) COMMENT '阈值',
    alert_method ENUM('EMAIL', 'DINGTALK') COMMENT '告警方式',
    confirmed TINYINT DEFAULT 0 COMMENT '是否已确认',
    confirmed_by VARCHAR(50) COMMENT '确认人',
    confirmed_time DATETIME COMMENT '确认时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rule_id) REFERENCES quality_rule(id),
    INDEX idx_trigger_time (trigger_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';
