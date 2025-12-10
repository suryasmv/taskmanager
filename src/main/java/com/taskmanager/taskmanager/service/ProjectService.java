package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.exception.ProjectNotFoundException;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Getter
    private final TaskService taskService;

    //Create a Project
    public ProjectEntity createProject(ProjectEntity project) {
        return projectRepository.save(project);
    }

    //Read All Projects
    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    //Read Project by ID
    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID " + id));
    }

    //Read Project by Name
    public ProjectEntity getProjectByName(String name) {
        return projectRepository.findByName(name)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with name " + name));
    }

    //Update Project
    public void UpdateProject(Long id, ProjectEntity project) {
        project.setId(id);
        projectRepository.save(project);
    }

    //Delete Project
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    // 1) Create task inside project
    @Transactional
    public TaskEntity createTaskForProject(String projectName, TaskEntity task) {
        var project = projectRepository.findByName(projectName)
                .orElseThrow(() -> new ProjectNotFoundException(projectName));

        Long projectId = project.getId();
        task.setProjectId(projectId);

        int maxPriority = projectRepository.findMaxPriorityByProjectId(projectId)
                .orElse(0);   // Optional<Integer> -> int

        Integer next = switch (maxPriority) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            default -> null;
        };

        task.setPriority(next);
        return taskRepository.save(task);
    }


    // 2) Get all tasks for a project
    public List<TaskEntity> getTasksForProject(String projectName) {
        ProjectEntity project = getProjectByName(projectName);
        return taskRepository.findAllByProjectIdOrderByPriorityAsc(project.getId());
    }

    // 3) Get one specific task inside a project
    public TaskEntity getTaskInProject(String projectName, Long taskId) {
        ProjectEntity project = getProjectByName(projectName);

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));

        if (!task.getProjectId().equals(project.getId())) {
            throw new TaskNotFoundException("Task " + taskId + " is not inside project " + projectName);
        }

        return task;
    }

    @Transactional
    public void deleteTaskInProject(String projectName, Long taskId, Integer maxPriority) {

        // 1) Delete the task
        taskRepository.deleteById(taskId);

        // 2) Fetch the project
        ProjectEntity project = projectRepository.findByName(projectName)
                .orElseThrow(() -> new ProjectNotFoundException(projectName));

        Long projectId = project.getId();

        List<TaskEntity> tasks =
                taskRepository.findAllByProjectIdOrderByPriorityAsc(projectId);

        // 4) Build reorder request
        TaskReorderRequest request = new TaskReorderRequest();
        request.setOrderedIds(
                tasks.stream().map(TaskEntity::getId).toList()
        );

        // 5) Reorder using existing method
        reorderTasksInProject(projectName, request, maxPriority);
    }

    // 6) Reorder tasks inside project
    @Transactional
    public void reorderTasksInProject(String projectName, TaskReorderRequest request, Integer maxPriority) {
        if (maxPriority != null) {
            taskService.setMaxPriority(projectName, maxPriority);
        }
        int limit = taskService.getMaxPriority(projectName);

        int pos = 1;
        for (Long taskId : request.getOrderedIds()) {
            TaskEntity task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
            // validate project id as before
            task.setPosition(pos);
            task.setPriority(pos <= limit ? pos : null);
            taskRepository.save(task);
            pos++;
        }
    }
}
