package com.quality.platform.util;

import org.quartz.CronExpression;

public class CronUtils {
    public static boolean isValid(String cron) {
        return CronExpression.isValidExpression(cron);
    }
}
