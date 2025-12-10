package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/tasks")
    public TaskEntity createTask(@RequestBody TaskEntity task) {
        return taskService.createTask(task);
    }

    @GetMapping("/tasks")
    public List<TaskEntity> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/tasks/{id}")
    public TaskEntity getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/tasks/{id}")
    public TaskEntity updateTask(@PathVariable Long id, @RequestBody TaskEntity task) {
        return taskService.updateTaskById(id, task);
    }

    @DeleteMapping("/tasks/{id}")
    public void deleteTask(@PathVariable Long id, @RequestParam(required = false) Integer maxPriority) {
        taskService.deleteTaskById(id, maxPriority);
    }

    @PostMapping("/tasks/reorder")
    public void reorderTasks(@RequestBody TaskReorderRequest request) {
        taskService.reorderTasks(request, request.getMaxPriority());
    }
}
