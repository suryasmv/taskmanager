package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.enums.ImportanceFilter;
import com.taskmanager.taskmanager.enums.SortDirection;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.taskmanager.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    // ---------- CREATE ----------

    public TaskEntity createTaskForUser(TaskEntity task, UserEntity user) {
        task.setUser(user);
        task.setIsImportant(false); // default false
        return taskRepository.save(task);
    }

    // ---------- READ ----------

    // Global tasks (no project) for current user
    public List<TaskEntity> getAllTasksForUser(UserEntity user) {
        return taskRepository
                .findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(user.getId());
    }

    // Helper: load by id and verify ownership
    public TaskEntity getTaskByIdForUser(Long id, UserEntity user) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        return task;
    }

    // ---------- UPDATE ----------

    @Transactional
    public TaskEntity updateTaskByIdForUser(Long id, TaskEntity task, UserEntity user) {
        TaskEntity existing = getTaskByIdForUser(id, user);

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDueDate(task.getDueDate());
        existing.setStatus(task.getStatus());
        existing.setIsImportant(task.getIsImportant());

        return taskRepository.save(existing);
    }

    // ---------- DELETE ----------

    @Transactional
    public void deleteTaskByIdForUser(Long id, UserEntity user) {
        TaskEntity existing = getTaskByIdForUser(id, user);
        taskRepository.deleteById(existing.getId());

        // Reorder remaining global tasks for this user (projectId = null)
        List<TaskEntity> tasks = taskRepository
                .findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(user.getId());

        TaskReorderRequest request = new TaskReorderRequest();
        request.setOrderedIds(tasks.stream().map(TaskEntity::getId).toList());
        request.setSortByDueDate(false);
    }

    // ---------- FILTER (per user) ----------

    public List<TaskEntity> getTasksWithFiltersForUser(
            UserEntity user,
            ImportanceFilter importance,
            SortDirection sortDirection
    ) {
        // Load tasks for this user (global only)
        List<TaskEntity> tasks = getAllTasksForUser(user);

        // Filter by importance
        tasks = tasks.stream()
                .filter(task -> {
                    boolean imp = Boolean.TRUE.equals(task.getIsImportant());
                    return switch (importance) {
                        case IMPORTANT_ONLY -> imp;
                        case NORMAL_ONLY   -> !imp;
                        case ALL           -> true;
                    };
                })
                .toList();

        // Sort by dueDate ASC / DESC
        Comparator<TaskEntity> cmp = Comparator.comparing(TaskEntity::getDueDate);
        if (sortDirection == SortDirection.DESC) {
            cmp = cmp.reversed();
        }

        return tasks.stream()
                .sorted(cmp)
                .collect(Collectors.toList());
    }
}
