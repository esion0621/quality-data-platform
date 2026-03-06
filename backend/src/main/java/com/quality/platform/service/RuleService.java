package com.quality.platform.service;

import com.quality.platform.dto.RuleDTO;
import com.quality.platform.entity.RuleEntity;
import com.quality.platform.exception.BusinessException;
import com.quality.platform.repository.AlertRepository;  
import com.quality.platform.repository.RuleRepository;
import com.quality.platform.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleService {
    private final RuleRepository ruleRepository;
    private final SchedulerService schedulerService;
    private final AlertRepository alertRepository;  

    @Transactional
    public RuleDTO createRule(RuleDTO dto) {
        RuleEntity entity = new RuleEntity();
        copyProperties(dto, entity);
        entity = ruleRepository.save(entity);
        schedulerService.scheduleRuleIfEnabled(entity);
        return toDTO(entity);
    }

    @Transactional
    public RuleDTO updateRule(Integer id, RuleDTO dto) {
        RuleEntity entity = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));
        copyProperties(dto, entity);
        entity = ruleRepository.save(entity);
        schedulerService.rescheduleRule(entity);
        return toDTO(entity);
    }

    @Transactional
    public void deleteRule(Integer id) {
        RuleEntity entity = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));

        // 1. 先删除该规则下的所有告警记录
        alertRepository.deleteByRuleId(id);

        // 2. 取消Quartz调度
        schedulerService.unscheduleRule(entity);

        // 3. 删除规则（关联的任务实例因外键 ON DELETE CASCADE 会自动删除）
        ruleRepository.delete(entity);
    }

    public RuleDTO getRule(Integer id) {
        RuleEntity entity = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));
        return toDTO(entity);
    }

    public List<RuleDTO> listRules() {
        return ruleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void copyProperties(RuleDTO dto, RuleEntity entity) {
        entity.setRuleName(dto.getRuleName());
        entity.setSourceType(dto.getSourceType());
        entity.setSourceConfig(dto.getSourceConfig());
        entity.setRuleType(dto.getRuleType());
        entity.setRuleParams(dto.getRuleParams());
        entity.setScheduleConfig(dto.getScheduleConfig());
        entity.setWindowConfig(dto.getWindowConfig());
        entity.setStatus(dto.getStatus());
    }

    private RuleDTO toDTO(RuleEntity entity) {
        RuleDTO dto = new RuleDTO();
        dto.setId(entity.getId());
        dto.setRuleName(entity.getRuleName());
        dto.setSourceType(entity.getSourceType());
        dto.setSourceConfig(entity.getSourceConfig());
        dto.setRuleType(entity.getRuleType());
        dto.setRuleParams(entity.getRuleParams());
        dto.setScheduleConfig(entity.getScheduleConfig());
        dto.setWindowConfig(entity.getWindowConfig());
        dto.setStatus(entity.getStatus());
        return dto;
    }
    public List<RuleDTO> getRealtimeRules() {
        return ruleRepository.findBySourceTypeAndStatus("KAFKA", (byte)1)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
    public List<Integer> getRealtimeRuleIds() {
        return ruleRepository.findBySourceTypeAndStatus("KAFKA", (byte)1)
                .stream()
                .map(RuleEntity::getId)
                .collect(Collectors.toList());
    }
}
