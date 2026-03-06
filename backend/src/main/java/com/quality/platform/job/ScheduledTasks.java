package com.quality.platform.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledTasks {
    // 可以放一些定时清理或检查任务
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldData() {
        log.info("Cleaning old data...");
    }
}
