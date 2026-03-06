package com.quality.platform.repository;

import com.quality.platform.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
    List<AlertEntity> findByRuleIdOrderByTriggerTimeDesc(Integer ruleId);
    List<AlertEntity> findByTriggerTimeBetween(LocalDateTime start, LocalDateTime end);

    void deleteByRuleId(Integer ruleId);
}
