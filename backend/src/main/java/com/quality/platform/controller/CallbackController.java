package com.quality.platform.controller;

import com.quality.platform.dto.TaskInstanceDTO;
import com.quality.platform.service.AlertService;
import com.quality.platform.service.TaskInstanceService;
import com.quality.platform.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/callback")
@RequiredArgsConstructor
public class CallbackController {
    private final TaskInstanceService taskInstanceService;
    private final AlertService alertService;

    @PostMapping("/spark-job")
    public void handleSparkJobCallback(@RequestBody Map<String, Object> callbackData) {
        Long instanceId = Long.valueOf(callbackData.get("instanceId").toString());
        String status = callbackData.get("status").toString();
        String resultSummary = callbackData.get("resultSummary").toString();

        // 更新任务状态
        taskInstanceService.updateTaskStatus(instanceId, status, resultSummary);

        try {
            // 解析结果摘要
            Map<String, Object> summary = JsonUtils.parseMap(resultSummary);
            Boolean passed = (Boolean) summary.get("passed");
            if (passed != null && !passed) {
                // 获取规则ID
                TaskInstanceDTO instance = taskInstanceService.getById(instanceId);
                Integer ruleId = instance.getRuleId();

                Double rate = (Double) summary.get("rate");
                Double threshold = (Double) summary.get("threshold");

                alertService.createAlert(ruleId, String.valueOf(rate), String.valueOf(threshold), "DINGTALK");
                log.info("Alert created for ruleId: {}, rate: {}, threshold: {}", ruleId, rate, threshold);
            }
        } catch (Exception e) {
            log.error("Failed to create alert from callback", e);
        }
    }
}
