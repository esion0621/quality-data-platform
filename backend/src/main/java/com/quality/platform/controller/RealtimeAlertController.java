package com.quality.platform.controller;

import com.quality.platform.service.HBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/realtime-alerts")
@RequiredArgsConstructor
public class RealtimeAlertController {
    private final HBaseService hBaseService;

    @GetMapping
    public List<Map<String, String>> getRealtimeAlerts(
            @RequestParam(required = false) Integer ruleId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) throws IOException {
        return hBaseService.getRealtimeAlerts(ruleId, startTime, endTime);
    }
}
