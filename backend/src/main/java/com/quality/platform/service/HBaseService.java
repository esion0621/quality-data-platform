package com.quality.platform.service;

import com.quality.platform.util.HBaseClientUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HBaseService {
    private final HBaseClientUtil hBaseClientUtil;

    private static final String REALTIME_ALERT_TABLE = "realtime_alert";

    public void putAnomalyDetail(String rowKey, Map<String, String> data) throws IOException {
        hBaseClientUtil.putData("quality_anomaly_detail", rowKey, "cf", data);
    }

    // 新增：查询实时告警
    public List<Map<String, String>> getRealtimeAlerts(Integer ruleId, Long startTime, Long endTime) throws IOException {
        String startRow = null;
        String stopRow = null;
        if (ruleId != null) {
            // Rowkey 格式：ruleId_timestamp_suffix
            startRow = ruleId + "_" + (startTime != null ? startTime : 0);
            stopRow = ruleId + "_" + (endTime != null ? endTime : Long.MAX_VALUE) + "_z";
        }
        return hBaseClientUtil.scan(REALTIME_ALERT_TABLE, startRow, stopRow);
    }
}
