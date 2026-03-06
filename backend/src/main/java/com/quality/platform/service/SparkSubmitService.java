package com.quality.platform.service;

import com.quality.platform.util.SparkLauncherUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SparkSubmitService {

    private final TaskInstanceService taskInstanceService;

    public void submitRuleJob(Integer ruleId, Long instanceId) {
        log.info("Submitting Spark job for ruleId: {}, instanceId: {}", ruleId, instanceId);
        // 实际调用SparkLauncherUtil提交作业
        SparkLauncherUtil.submitJob(ruleId, instanceId);
    }
}
