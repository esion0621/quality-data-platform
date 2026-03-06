package com.quality.platform.repository;

import com.quality.platform.entity.TaskInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskInstanceRepository extends JpaRepository<TaskInstanceEntity, Long> {
    List<TaskInstanceEntity> findByRuleIdOrderByScheduledTimeDesc(Integer ruleId);
    List<TaskInstanceEntity> findByScheduledTimeBetween(LocalDateTime start, LocalDateTime end);
}
