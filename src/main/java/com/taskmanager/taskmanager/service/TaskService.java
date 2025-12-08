package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    //Create a Task
    public TaskEntity createTask(TaskEntity task) {
        int maxPriority = taskRepository.findMaxPriority().orElse(0);

        Integer nextPriority;
        if (maxPriority == 0) {
            nextPriority = 1;
        } else if (maxPriority == 1) {
            nextPriority = 2;
        } else if (maxPriority == 2) {
            nextPriority = 3;
        } else {
            nextPriority = null;
        }

        task.setPriority(nextPriority);
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
    public void deleteTaskById(Long id) {
        taskRepository.deleteById(id);

        List<TaskEntity> tasks = taskRepository.findAllByOrderByPriorityAsc();
        TaskReorderRequest request = new TaskReorderRequest();
        request.setOrderedIds(tasks.stream().map(TaskEntity::getId).toList());

        reorderTasks(request);
    }

    //Reorder Tasks
    @Transactional
    public void reorderTasks(TaskReorderRequest request){
        int priority = 1;

        for(Long id : request.getOrderedIds()){
            TaskEntity task = taskRepository.findById(id).orElseThrow();

            if(priority <= 3) {
                task.setPriority(priority);
            }else {
                task.setPriority(null);
            }
            priority++;
        }
    }
}
