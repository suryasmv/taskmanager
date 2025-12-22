package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.enums.ImportanceFilter;
import com.taskmanager.taskmanager.enums.SortDirection;
import com.taskmanager.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskService taskService;

    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserEntity) auth.getPrincipal();
    }

    // ---------- CREATE ----------

    @PostMapping("/tasks")
    public TaskEntity createTask(@RequestBody TaskEntity task) {
        UserEntity user = getCurrentUser();
        return taskService.createTaskForUser(task, user);
    }

    // ---------- READ ----------

    @GetMapping("/tasks")
    public List<TaskEntity> getAllTasks() {
        UserEntity user = getCurrentUser();
        return taskService.getAllTasksForUser(user);
    }

    @GetMapping("/tasks/{id}")
    public TaskEntity getTaskById(@PathVariable Long id) {
        UserEntity user = getCurrentUser();
        return taskService.getTaskByIdForUser(id, user);
    }

    // ---------- UPDATE ----------

    @PutMapping("/tasks/{id}")
    public TaskEntity updateTask(@PathVariable Long id, @RequestBody TaskEntity task) {
        UserEntity user = getCurrentUser();
        return taskService.updateTaskByIdForUser(id, task, user);
    }

    // ---------- DELETE ----------

    @DeleteMapping("/tasks/{id}")
    public void deleteTask(@PathVariable Long id) {
        UserEntity user = getCurrentUser();
        taskService.deleteTaskByIdForUser(id, user);
    }

    // ---------- FILTER (per user) ----------

    @GetMapping("/tasks/filter")
    public List<TaskEntity> filterTasks(
            @RequestParam(defaultValue = "ALL") ImportanceFilter importance,
            @RequestParam(defaultValue = "ASC") SortDirection sortDir
    ) {
        UserEntity user = getCurrentUser();
        return taskService.getTasksWithFiltersForUser(user, importance, sortDir);
    }
}
