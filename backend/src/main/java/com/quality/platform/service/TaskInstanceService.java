package com.quality.platform.service;

import com.quality.platform.dto.TaskInstanceDTO;
import com.quality.platform.entity.TaskInstanceEntity;
import com.quality.platform.repository.TaskInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskInstanceService {
    private final TaskInstanceRepository taskInstanceRepository;

    public TaskInstanceDTO createInstance(Integer ruleId, LocalDateTime scheduledTime) {
        TaskInstanceEntity entity = new TaskInstanceEntity();
        entity.setRuleId(ruleId);
        entity.setScheduledTime(scheduledTime);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus("RUNNING");
        entity = taskInstanceRepository.save(entity);
        return toDTO(entity);
    }

    public void updateTaskStatus(Long instanceId, String status, String resultSummary) {
        TaskInstanceEntity entity = taskInstanceRepository.findById(instanceId).orElseThrow();
        entity.setStatus(status);
        entity.setEndTime(LocalDateTime.now());
        entity.setResultSummary(resultSummary);
        taskInstanceRepository.save(entity);
    }

    public List<TaskInstanceDTO> getByRuleId(Integer ruleId) {
        return taskInstanceRepository.findByRuleIdOrderByScheduledTimeDesc(ruleId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TaskInstanceDTO getById(Long id) {
        return toDTO(taskInstanceRepository.findById(id).orElseThrow());
    }

    private TaskInstanceDTO toDTO(TaskInstanceEntity entity) {
        TaskInstanceDTO dto = new TaskInstanceDTO();
        dto.setId(entity.getId());
        dto.setRuleId(entity.getRuleId());
        dto.setScheduledTime(entity.getScheduledTime());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStatus(entity.getStatus());
        dto.setResultSummary(entity.getResultSummary());
        dto.setErrorMsg(entity.getErrorMsg());
        return dto;
    }
    public List<TaskInstanceDTO> listAll() {
        return taskInstanceRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
}
