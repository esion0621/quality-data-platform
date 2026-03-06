package com.quality.platform.controller;

import com.quality.platform.dto.RulePassRateDTO;
import com.quality.platform.dto.StatsTrendDTO;
import com.quality.platform.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping("/trend")
    public List<StatsTrendDTO> getTrend(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        return statsService.getTrend(start, end);
    }

    @GetMapping("/pass-rates")
    public List<RulePassRateDTO> getRulePassRates() {
        return statsService.getRulePassRates();
    }
    @GetMapping("/realtime/latest")
    public Map<String, Object> getRealtimeLatest() {
        return statsService.getRealtimeLatest();
    }
}
