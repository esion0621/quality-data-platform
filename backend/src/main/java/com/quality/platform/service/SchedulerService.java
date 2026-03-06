package com.quality.platform.service;

import com.quality.platform.entity.RuleEntity;
import com.quality.platform.job.RuleTriggerJob;
import com.quality.platform.util.CronUtils;
import com.quality.platform.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final Scheduler scheduler;

    /**
     * 如果规则启用且为HDFS类型，则调度
     */
    public void scheduleRuleIfEnabled(RuleEntity rule) {
        if (rule.getStatus() == 1 && "HDFS".equals(rule.getSourceType())) {
            scheduleRule(rule);
        }
    }

    /**
     * 调度规则
     */
    public void scheduleRule(RuleEntity rule) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(RuleTriggerJob.class)
                    .withIdentity("rule-" + rule.getId())
                    .usingJobData("ruleId", rule.getId())
                    .build();

            Map<String, Object> scheduleConfig = JsonUtils.parseMap(rule.getScheduleConfig());
            String cron = (String) scheduleConfig.get("cron");
            if (!CronUtils.isValid(cron)) {
                log.error("Invalid cron expression: {} for ruleId: {}", cron, rule.getId());
                return;
            }

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger-" + rule.getId())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled rule: {} with cron: {}", rule.getId(), cron);
        } catch (SchedulerException e) {
            log.error("Failed to schedule rule: " + rule.getId(), e);
        }
    }

    /**
     * 重新调度规则（更新cron）
     */
    public void rescheduleRule(RuleEntity rule) {
        unscheduleRule(rule);
        scheduleRuleIfEnabled(rule);
    }

    /**
     * 取消调度规则
     */
    public void unscheduleRule(RuleEntity rule) {
        try {
            JobKey jobKey = JobKey.jobKey("rule-" + rule.getId());
            scheduler.deleteJob(jobKey);
            log.info("Unscheduled rule: {}", rule.getId());
        } catch (SchedulerException e) {
            log.error("Failed to unschedule rule: " + rule.getId(), e);
        }
    }
}
