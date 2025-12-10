package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.taskmanager.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final Map<String, Integer> maxPriorityMap = new ConcurrentHashMap<>();

    {
        maxPriorityMap.put("default", 3); // default global max priority
    }

    public void setMaxPriority(String key, int priority) {
        maxPriorityMap.put(key, priority);
    }

    public int getMaxPriority(String key) {
        return maxPriorityMap.getOrDefault(key, 3);
    }

    //Create a Task
    public TaskEntity createTask(TaskEntity task) {
        int maxPriority = taskRepository.findMaxPriority().orElse(0);

        Integer next = switch (maxPriority) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            default -> null;
        };

        task.setPriority(next);
        return taskRepository.save(task);
    }


    //Read Tasks
    public List<TaskEntity> getAllTasks() {
        return taskRepository.findAllByOrderByPriorityAsc();
    }

    //Read Task by ID
    public TaskEntity getTaskById(Long id) {
        return taskRepository.findById(id).
                orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    //Update Task by ID
    @Transactional
    public TaskEntity updateTaskById(Long id, TaskEntity task) {
        TaskEntity existing = getTaskById(id);

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDueDate(task.getDueDate());
        existing.setStatus(task.getStatus());

        return taskRepository.save(existing);
    }

    //Delete Task by ID
    @Transactional
    public void deleteTaskById(Long id, Integer maxPriority) {
        taskRepository.deleteById(id);

        List<TaskEntity> tasks = taskRepository.findAllByOrderByPriorityAsc();
        TaskReorderRequest request = new TaskReorderRequest();
        request.setOrderedIds(tasks.stream().map(TaskEntity::getId).toList());

        reorderTasks(request, maxPriority);
    }

    @Transactional
    public void reorderTasks(TaskReorderRequest request, Integer maxPriority) {
        if (maxPriority != null) {
            setMaxPriority("default", maxPriority);
        }
        int limit = getMaxPriority("default");

        int pos = 1;
        for (Long id : request.getOrderedIds()) {
            TaskEntity task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            task.setPosition(pos);
            task.setPriority(pos <= limit ? pos : null);
            taskRepository.save(task);
            pos++;
        }
    }
}
