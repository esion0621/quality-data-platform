package com.quality.platform.controller;

import com.quality.platform.dto.RuleDTO;
import com.quality.platform.dto.TaskInstanceDTO;
import com.quality.platform.service.RuleService;
import com.quality.platform.service.SparkSubmitService;
import com.quality.platform.service.TaskInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {
    private final RuleService ruleService;
    private final TaskInstanceService taskInstanceService;
    private final SparkSubmitService sparkSubmitService;

    @PostMapping
    public RuleDTO createRule(@RequestBody RuleDTO dto) {
        return ruleService.createRule(dto);
    }

    @PutMapping("/{id}")
    public RuleDTO updateRule(@PathVariable Integer id, @RequestBody RuleDTO dto) {
        return ruleService.updateRule(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteRule(@PathVariable Integer id) {
        ruleService.deleteRule(id);
    }

    @GetMapping("/{id}")
    public RuleDTO getRule(@PathVariable Integer id) {
        return ruleService.getRule(id);
    }

    @GetMapping
    public List<RuleDTO> listRules() {
        return ruleService.listRules();
    }

    /**
     * 手动触发规则校验（仅用于测试）
     */
    @PostMapping("/trigger/{id}")
    public ResponseEntity<String> trigger(@PathVariable Integer id) {
        try {
            // 创建任务实例（状态为 RUNNING）
            TaskInstanceDTO instance = taskInstanceService.createInstance(id, LocalDateTime.now());
            // 提交 Spark 作业
            sparkSubmitService.submitRuleJob(id, instance.getId());
            return ResponseEntity.ok("触发成功，实例ID: " + instance.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("触发失败: " + e.getMessage());
        }
    }
    @GetMapping("/realtime")
    public List<RuleDTO> getRealtimeRules() {
        return ruleService.getRealtimeRules();
    }
}
