package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.enums.ImportanceFilter;
import com.taskmanager.taskmanager.enums.SortDirection;
import com.taskmanager.taskmanager.exception.ProjectNotFoundException;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskEntity createTask(TaskEntity task) {
        task.setIsImportant(false); // default false
        return taskRepository.save(task);
    }

    // Read global tasks (no project)
    public List<TaskEntity> getAllTasks() {
        return taskRepository.findAllByProjectIdIsNullOrderByIsImportantAscDueDateAsc();
    }

    //Read Task by ID
    public TaskEntity getTaskById(Long id) {
        return taskRepository.findById(id).
                orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    //update task by ID
    @Transactional
    public TaskEntity updateTaskById(Long id, TaskEntity task) {
        TaskEntity existing = getTaskById(id);

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDueDate(task.getDueDate());
        existing.setStatus(task.getStatus());
        existing.setIsImportant(task.getIsImportant()); // ← add this

        return taskRepository.save(existing);
    }

    @Transactional
    public void deleteTaskById(Long id) {  // no maxPriority
        taskRepository.deleteById(id);

        // Get remaining global tasks (projectId = null) ordered by isImportant + dueDate
        List<TaskEntity> tasks = taskRepository
                .findAllByProjectIdIsNullOrderByIsImportantAscDueDateAsc();

        TaskReorderRequest request = new TaskReorderRequest();
        request.setOrderedIds(tasks.stream().map(TaskEntity::getId).toList());
        request.setSortByDueDate(false);  // preserve natural order
    }

    public List<TaskEntity> getTasksWithFilters(
            ImportanceFilter importance,
            SortDirection sortDirection
    ) {
        // 1) Load ALL tasks
        List<TaskEntity> tasks = taskRepository.findAll();

        // 2) Filter by importance
        tasks = tasks.stream()
                .filter(task -> {
                    boolean imp = Boolean.TRUE.equals(task.getIsImportant());
                    return switch (importance) {
                        case IMPORTANT_ONLY -> imp;
                        case NORMAL_ONLY   -> !imp;
                        case ALL           -> true;
                    };
                })
                .collect(Collectors.toList());

        // 3) Sort by dueDate ASC / DESC
        Comparator<TaskEntity> cmp = Comparator.comparing(TaskEntity::getDueDate);
        if (sortDirection == SortDirection.DESC) {
            cmp = cmp.reversed();
        }

        return tasks.stream()
                .sorted(cmp)
                .collect(Collectors.toList());
    }
}
