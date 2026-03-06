package com.quality.platform.service;

import com.quality.platform.dto.RulePassRateDTO;
import com.quality.platform.dto.StatsTrendDTO;
import com.quality.platform.entity.TaskInstanceEntity;
import com.quality.platform.repository.RuleRepository;
import com.quality.platform.repository.TaskInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.quality.platform.util.JsonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final TaskInstanceRepository taskInstanceRepository;
    private final RuleRepository ruleRepository;
    private final RedisService redisService;
    private final RuleService ruleService;  

    public List<StatsTrendDTO> getTrend(LocalDate start, LocalDate end) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);
        List<TaskInstanceEntity> tasks = taskInstanceRepository.findByScheduledTimeBetween(startTime, endTime);
        Map<LocalDate, List<TaskInstanceEntity>> groupByDate = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getScheduledTime().toLocalDate()));
        return groupByDate.entrySet().stream()
                .map(e -> {
                    StatsTrendDTO dto = new StatsTrendDTO();
                    dto.setDate(e.getKey());
                    long total = e.getValue().size();
                    long success = e.getValue().stream().filter(t -> "SUCCESS".equals(t.getStatus())).count();
                    dto.setTotalCount(total);
                    dto.setSuccessCount(success);
                    dto.setFailCount(total - success);
                    return dto;
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    public List<RulePassRateDTO> getRulePassRates() {
        List<TaskInstanceEntity> allTasks = taskInstanceRepository.findAll();
        Map<Integer, List<TaskInstanceEntity>> byRule = allTasks.stream()
                .collect(Collectors.groupingBy(TaskInstanceEntity::getRuleId));

        List<RulePassRateDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<TaskInstanceEntity>> entry : byRule.entrySet()) {
            RulePassRateDTO dto = new RulePassRateDTO();
            dto.setRuleId(entry.getKey());
            ruleRepository.findById(entry.getKey()).ifPresent(r -> dto.setRuleName(r.getRuleName()));
            long total = entry.getValue().size();
            long success = entry.getValue().stream().filter(t -> "SUCCESS".equals(t.getStatus())).count();
            double rate = total == 0 ? 0 : (double) success / total;
            dto.setPassRate(rate);
            result.add(dto);
        }
        return result;
    }

    
    public Map<String, Object> getRealtimeLatest() {
        Map<String, Object> result = new HashMap<>();
        List<Integer> ruleIds = ruleService.getRealtimeRuleIds();
        for (Integer ruleId : ruleIds) {
            String key = "realtime:rule:" + ruleId + ":latest";
            String value = redisService.hget(key, "latest");
            if (value != null) {
                Map<String, Object> metric = JsonUtils.parseMap(value);
                result.put(String.valueOf(ruleId), metric);
            }
        }
        return result;
    }
}
