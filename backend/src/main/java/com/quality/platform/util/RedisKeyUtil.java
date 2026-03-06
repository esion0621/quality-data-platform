package com.quality.platform.util;

public class RedisKeyUtil {
    public static String realtimeRuleKey(Integer ruleId) {
        return "realtime:rule:" + ruleId;
    }

    public static String globalAnomalyRateKey() {
        return "realtime:global:anomaly_rate";
    }
}
