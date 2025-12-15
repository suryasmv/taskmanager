package com.taskmanager.taskmanager.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

        // New project tasks start as normal (not important)
        task.setIsImportant(false);

        return taskRepository.save(task);
    }

    // 2) Get all tasks for a project
    public List<TaskEntity> getTasksForProject(String projectName) {
        ProjectEntity project = getProjectByName(projectName);
        Long projectId = project.getId();

        // Important tasks first, then normal, both by dueDate DESC
        return taskRepository.findAllByProjectIdOrderByIsImportantDescDueDateDesc(projectId);
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
    public void deleteTaskInProject(String projectName, Long taskId) {  // no maxPriority

        // 1) Delete the task
        taskRepository.deleteById(taskId);

        // 2) Get project ID
        Long projectId = projectRepository.findByName(projectName)
                .map(ProjectEntity::getId)
                .orElseThrow(() -> new ProjectNotFoundException(projectName));

        // 3) Get remaining project tasks
        List<TaskEntity> remainingTasks = taskRepository
                .findAllByProjectIdOrderByIsImportantDescDueDateDesc(projectId);  // new query order

        // 4) Build reorder request from current DB order
        TaskReorderRequest request = new TaskReorderRequest();
        request.setOrderedIds(remainingTasks.stream().map(TaskEntity::getId).toList());
        request.setSortByDueDate(false);  // preserve natural order after delete
    }
}
