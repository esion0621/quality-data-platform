package com.quality.platform.service;

import com.quality.platform.dto.AlertDTO;
import com.quality.platform.entity.AlertEntity;
import com.quality.platform.repository.AlertRepository;
import com.quality.platform.util.AlertSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;
    private final AlertSender alertSender;

    public void createAlert(Integer ruleId, String abnormalValue, String thresholdValue, String method) {
        AlertEntity entity = new AlertEntity();
        entity.setRuleId(ruleId);
        entity.setTriggerTime(LocalDateTime.now());
        entity.setAbnormalValue(abnormalValue);
        entity.setThresholdValue(thresholdValue);
        entity.setAlertMethod(method);
        alertRepository.save(entity);

        // 发送钉钉告警
        if ("DINGTALK".equals(method)) {
            alertSender.sendDingTalk(ruleId, abnormalValue, thresholdValue);
        }
    }

    public void confirmAlert(Long id, String confirmBy) {
        AlertEntity entity = alertRepository.findById(id).orElseThrow();
        entity.setConfirmed((byte)1);
        entity.setConfirmedBy(confirmBy);
        entity.setConfirmedTime(LocalDateTime.now());
        alertRepository.save(entity);
    }

    public List<AlertDTO> getByRuleId(Integer ruleId) {
        return alertRepository.findByRuleIdOrderByTriggerTimeDesc(ruleId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AlertDTO toDTO(AlertEntity entity) {
        AlertDTO dto = new AlertDTO();
        dto.setId(entity.getId());
        dto.setRuleId(entity.getRuleId());
        dto.setTriggerTime(entity.getTriggerTime());
        dto.setAbnormalValue(entity.getAbnormalValue());
        dto.setThresholdValue(entity.getThresholdValue());
        dto.setAlertMethod(entity.getAlertMethod());
        dto.setConfirmed(entity.getConfirmed());
        dto.setConfirmedBy(entity.getConfirmedBy());
        dto.setConfirmedTime(entity.getConfirmedTime());
        return dto;
    }
    public List<AlertDTO> listAll() {
        return alertRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
