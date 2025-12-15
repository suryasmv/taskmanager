package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ProjectEntity createProject(@RequestBody ProjectEntity project) {
        return projectService.createProject(project);
    }

    @GetMapping
    public List<ProjectEntity> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/id/{id}")
    public ProjectEntity getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/name/{name}")
    public ProjectEntity getProjectByName(@PathVariable String name) {
        return projectService.getProjectByName(name);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }

    @PostMapping("/{projectName}/tasks")
    public TaskEntity createTaskForProject(@PathVariable String projectName,
                                           @RequestBody TaskEntity task) {
        return projectService.createTaskForProject(projectName, task);
    }

    @GetMapping("/{projectName}/tasks")
    public List<TaskEntity> getTasksForProject(@PathVariable String projectName) {
        return projectService.getTasksForProject(projectName);
    }

    @GetMapping("/{projectName}/tasks/{id}")
    public TaskEntity getTaskByIdInProject(@PathVariable String projectName,
                                           @PathVariable Long id) {
        TaskEntity task = projectService.getTaskService().getTaskById(id);
        return task;
    }

    @PutMapping("/{projectName}/tasks/{id}")
    public TaskEntity updateTaskInProject(@PathVariable String projectName,
                                          @PathVariable Long id,
                                          @RequestBody TaskEntity task) {
        return projectService.getTaskService().updateTaskById(id, task);
    }

    @DeleteMapping("/{projectName}/tasks/{id}")
    public void deleteTaskInProject(@PathVariable String projectName,
                                    @PathVariable Long id) {
        projectService.deleteTaskInProject(projectName, id);
    }
}
