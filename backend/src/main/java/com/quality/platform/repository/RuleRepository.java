package com.quality.platform.repository;

import com.quality.platform.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Integer> {
    List<RuleEntity> findByStatus(Byte status);
    List<RuleEntity> findBySourceTypeAndStatus(String sourceType, Byte status);
}
