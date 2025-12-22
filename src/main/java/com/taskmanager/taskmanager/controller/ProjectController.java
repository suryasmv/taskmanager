package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    private final ProjectService projectService;

    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserEntity) auth.getPrincipal();
    }

    // ---------- PROJECT CRUD ----------

    @PostMapping
    public ProjectEntity createProject(@RequestBody ProjectEntity project) {
        UserEntity user = getCurrentUser();
        return projectService.createProject(project, user);
    }

    @GetMapping
    public List<ProjectEntity> getAllProjects() {
        UserEntity user = getCurrentUser();
        return projectService.getAllProjectsForUser(user);
    }

    @GetMapping("/id/{id}")
    public ProjectEntity getProjectById(@PathVariable Long id) {
        UserEntity user = getCurrentUser();
        return projectService.getProjectByIdForUser(id, user);
    }

    @GetMapping("/name/{name}")
    public ProjectEntity getProjectByName(@PathVariable String name) {
        UserEntity user = getCurrentUser();
        return projectService.getProjectByNameForUser(name, user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectEntity project
    ) {
        UserEntity user = getCurrentUser();
        projectService.updateProjectForUser(id, project, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        UserEntity user = getCurrentUser();
        projectService.deleteProjectForUser(id, user);
    }

    // ---------- PROJECT TASKS ----------

    @PostMapping("/{projectName}/tasks")
    public TaskEntity createTaskForProject(@PathVariable String projectName,
                                           @RequestBody TaskEntity task) {
        UserEntity user = getCurrentUser();
        return projectService.createTaskForProjectForUser(projectName, task, user);
    }

    @GetMapping("/{projectName}/tasks")
    public List<TaskEntity> getTasksForProject(@PathVariable String projectName) {
        UserEntity user = getCurrentUser();
        return projectService.getTasksForProjectForUser(projectName, user);
    }

    @GetMapping("/{projectName}/tasks/{id}")
    public TaskEntity getTaskByIdInProject(@PathVariable String projectName,
                                           @PathVariable Long id) {
        UserEntity user = getCurrentUser();
        return projectService.getTaskInProjectForUser(projectName, id, user);
    }

    @PutMapping("/{projectName}/tasks/{id}")
    public TaskEntity updateTaskInProject(@PathVariable String projectName,
                                          @PathVariable Long id,
                                          @RequestBody TaskEntity task) {
        // reuse TaskService user‑scoped update if you prefer; here we just ensure user owns that task
        UserEntity user = getCurrentUser();
        TaskEntity existing = projectService.getTaskInProjectForUser(projectName, id, user);

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDueDate(task.getDueDate());
        existing.setStatus(task.getStatus());
        existing.setIsImportant(task.getIsImportant());

        return projectService.getTaskService().updateTaskByIdForUser(existing.getId(), existing, user);
    }

    @DeleteMapping("/{projectName}/tasks/{id}")
    public void deleteTaskInProject(@PathVariable String projectName,
                                    @PathVariable Long id) {
        UserEntity user = getCurrentUser();
        projectService.deleteTaskInProjectForUser(projectName, id, user);
    }
}
