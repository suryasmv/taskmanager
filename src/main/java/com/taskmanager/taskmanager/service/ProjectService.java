package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.entity.UserEntity;
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

// ---------- PROJECT CRUD (per user) ----------

    public ProjectEntity createProject(ProjectEntity project, UserEntity user) {
        boolean exists = projectRepository
                .findByNameAndUserId(project.getName(), user.getId())
                .isPresent();
        if (exists) {
            throw new ProjectNotFoundException(
                    "Project with name '" + project.getName() + "' already exists for this user"
            );
        }

        project.setUser(user);
        return projectRepository.save(project);
    }

    public List<ProjectEntity> getAllProjectsForUser(UserEntity user) {
        return projectRepository.findAllByUserId(user.getId());
    }

    public ProjectEntity getProjectByIdForUser(Long id, UserEntity user) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID " + id));

        if (!project.getUser().getId().equals(user.getId())) {
            throw new ProjectNotFoundException("Project not found with ID " + id);
        }
        return project;
    }

    public ProjectEntity getProjectByNameForUser(String name, UserEntity user) {
        return projectRepository.findByNameAndUserId(name, user.getId())
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Project not found with name " + name + " for current user"
                ));
    }

    public void updateProjectForUser(Long id, ProjectEntity project, UserEntity user) {
        ProjectEntity existing = getProjectByIdForUser(id, user);
        existing.setName(project.getName());
        projectRepository.save(existing);
    }

    @Transactional
    public void deleteProjectForUser(Long id, UserEntity user) {
        ProjectEntity existing = getProjectByIdForUser(id, user);

// delete tasks under this project
        taskRepository.deleteByProjectId(existing.getId());
        projectRepository.deleteById(existing.getId());
    }

// ---------- PROJECT TASKS (per user) ----------

    @Transactional
    public TaskEntity createTaskForProjectForUser(String projectName, TaskEntity task, UserEntity user) {
        ProjectEntity project = getProjectByNameForUser(projectName, user);

        Long projectId = project.getId();
        task.setProjectId(projectId);
        task.setUser(user); // important: same user
        task.setIsImportant(false);

        return taskRepository.save(task);
    }

    public List<TaskEntity> getTasksForProjectForUser(String projectName, UserEntity user) {
        ProjectEntity project = getProjectByNameForUser(projectName, user);
        Long projectId = project.getId();

        return taskRepository.findAllByProjectIdOrderByIsImportantDescDueDateDesc(projectId);
    }

    public TaskEntity getTaskInProjectForUser(String projectName, Long taskId, UserEntity user) {
        ProjectEntity project = getProjectByNameForUser(projectName, user);

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));

        if (!task.getProjectId().equals(project.getId()) ||
                !task.getUser().getId().equals(user.getId())) {
            throw new TaskNotFoundException("Task " + taskId + " is not inside project " + projectName);
        }

        return task;
    }

    @Transactional
    public void deleteTaskInProjectForUser(String projectName, Long taskId, UserEntity user) {
// validate ownership & membership in project
        TaskEntity task = getTaskInProjectForUser(projectName, taskId, user);

// 1) Delete the task
        taskRepository.deleteById(task.getId());

// 2) Get project ID
        Long projectId = task.getProjectId();

// 3) Get remaining project tasks
        List<TaskEntity> remainingTasks = taskRepository
                .findAllByProjectIdOrderByIsImportantDescDueDateDesc(projectId);

// 4) Build reorder request from current DB order
        TaskReorderRequest request = new TaskReorderRequest();
        request.setOrderedIds(remainingTasks.stream().map(TaskEntity::getId).toList());
        request.setSortByDueDate(false);
    }
}