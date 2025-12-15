package com.taskmanager.taskmanager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.enums.ImportanceFilter;
import com.taskmanager.taskmanager.enums.SortDirection;
import com.taskmanager.taskmanager.service.TaskService;

import lombok.RequiredArgsConstructor;

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
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTaskById(id);
    }

    @GetMapping("/tasks/filter")
    public List<TaskEntity> filterTasks(
            @RequestParam(defaultValue = "ALL") ImportanceFilter importance,
            @RequestParam(defaultValue = "ASC") SortDirection sortDir
    ) {
        return taskService.getTasksWithFilters(importance, sortDir);
    }
}
