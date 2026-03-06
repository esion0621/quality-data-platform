package com.quality.platform.controller;

import com.quality.platform.dto.TaskInstanceDTO;
import com.quality.platform.service.TaskInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskInstanceController {
    private final TaskInstanceService taskInstanceService;

    @GetMapping("/rule/{ruleId}")
    public List<TaskInstanceDTO> getByRuleId(@PathVariable Integer ruleId) {
        return taskInstanceService.getByRuleId(ruleId);
    }

    @GetMapping("/{id}")
    public TaskInstanceDTO getById(@PathVariable Long id) {
        return taskInstanceService.getById(id);
    }
    @GetMapping
    public List<TaskInstanceDTO> listAllTasks() {
        return taskInstanceService.listAll();
    }
}
