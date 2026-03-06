package com.quality.platform.job;

import com.quality.platform.service.SparkSubmitService;
import com.quality.platform.service.TaskInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class RuleTriggerJob implements Job {

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Autowired
    private SparkSubmitService sparkSubmitService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        Integer ruleId = data.getInt("ruleId");
        log.info("Quartz triggered rule job, ruleId: {}", ruleId);

        try {
            // 创建任务实例
            var instance = taskInstanceService.createInstance(ruleId, LocalDateTime.now());
            // 提交Spark作业
            sparkSubmitService.submitRuleJob(ruleId, instance.getId());
            log.info("Spark job submitted for ruleId: {}, instanceId: {}", ruleId, instance.getId());
        } catch (Exception e) {
            log.error("Failed to execute rule job for ruleId: " + ruleId, e);
        }
    }
}
