package com.quality.platform.controller;

import com.quality.platform.dto.AlertCreateDTO;
import com.quality.platform.dto.AlertDTO;
import com.quality.platform.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;

    // 获取所有离线告警
    @GetMapping
    public List<AlertDTO> getAllAlerts() {
        return alertService.listAll();
    }

    @GetMapping("/rule/{ruleId}")
    public List<AlertDTO> getByRuleId(@PathVariable Integer ruleId) {
        return alertService.getByRuleId(ruleId);
    }

    @PutMapping("/{id}/confirm")
    public void confirmAlert(@PathVariable Long id, @RequestParam String confirmBy) {
        alertService.confirmAlert(id, confirmBy);
    }

    @PostMapping
    public void createAlert(@RequestBody AlertCreateDTO dto) {
        alertService.createAlert(
            dto.getRuleId(),
            dto.getAbnormalValue(),
            dto.getThresholdValue(),
            dto.getAlertMethod()
        );
    }
}
