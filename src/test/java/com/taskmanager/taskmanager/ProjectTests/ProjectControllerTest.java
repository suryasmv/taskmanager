package com.taskmanager.taskmanager.ProjectTests;

import com.taskmanager.taskmanager.controller.ProjectController;
import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.service.ProjectService;
import com.taskmanager.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProject() throws Exception {
        ProjectEntity project = new ProjectEntity();
        project.setName("Test Project");
        when(projectService.createProject(any(ProjectEntity.class))).thenReturn(project);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk());

        verify(projectService).createProject(any(ProjectEntity.class));
    }

    @Test
    void getAllProjects() throws Exception {
        ProjectEntity project = new ProjectEntity();
        project.setName("Test");
        when(projectService.getAllProjects()).thenReturn(List.of(project));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk());

        verify(projectService).getAllProjects();
    }

    @Test
    void getProjectById() throws Exception {
        ProjectEntity project = new ProjectEntity();
        project.setName("Test");
        when(projectService.getProjectById(1L)).thenReturn(project);

        mockMvc.perform(get("/projects/id/1"))
                .andExpect(status().isOk());

        verify(projectService).getProjectById(1L);
    }

    @Test
    void getProjectByName() throws Exception {
        ProjectEntity project = new ProjectEntity();
        project.setName("Test");
        when(projectService.getProjectByName("Test")).thenReturn(project);

        mockMvc.perform(get("/projects/name/Test"))
                .andExpect(status().isOk());

        verify(projectService).getProjectByName("Test");
    }

    @Test
    void updateProject() throws Exception {
        ProjectEntity project = new ProjectEntity();
        project.setName("Updated");

        mockMvc.perform(put("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isNoContent());

        verify(projectService).UpdateProject(eq(1L), any(ProjectEntity.class));
    }

    @Test
    void deleteProject() throws Exception {
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isOk());

        verify(projectService).deleteProject(1L);
    }

    @Test
    void createTaskForProject() throws Exception {
        TaskEntity task = new TaskEntity();
        task.setTitle("Test Task");
        when(projectService.createTaskForProject(eq("Test Project"), any(TaskEntity.class))).thenReturn(task);

        mockMvc.perform(post("/projects/Test Project/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk());

        verify(projectService).createTaskForProject(eq("Test Project"), any(TaskEntity.class));
    }

    @Test
    void getTasksForProject() throws Exception {
        TaskEntity task = new TaskEntity();
        task.setTitle("Test Task");
        when(projectService.getTasksForProject("Test Project")).thenReturn(List.of(task));

        mockMvc.perform(get("/projects/Test Project/tasks"))
                .andExpect(status().isOk());

        verify(projectService).getTasksForProject("Test Project");
    }

    @Test
    void getTaskByIdInProject() throws Exception {
        TaskEntity task = new TaskEntity();
        task.setTitle("Test Task");

        // FIX 2: Mock projectService.getTaskService() → returns TaskService mock
        TaskService mockTaskService = mock(TaskService.class);
        when(projectService.getTaskService()).thenReturn(mockTaskService);
        when(mockTaskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/projects/Test Project/tasks/1"))
                .andExpect(status().isOk());

        verify(projectService.getTaskService()).getTaskById(1L);
    }

    @Test
    void updateTaskInProject() throws Exception {
        TaskEntity task = new TaskEntity();
        task.setTitle("Updated Task");

        // FIX 3: Mock projectService.getTaskService() → returns TaskService mock
        TaskService mockTaskService = mock(TaskService.class);
        when(projectService.getTaskService()).thenReturn(mockTaskService);
        when(mockTaskService.updateTaskById(eq(1L), any(TaskEntity.class))).thenReturn(task);

        mockMvc.perform(put("/projects/Test Project/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk());

        verify(projectService.getTaskService()).updateTaskById(eq(1L), any(TaskEntity.class));
    }

    @Test
    void deleteTaskInProject() throws Exception {
        mockMvc.perform(delete("/projects/Test Project/tasks/1"))
                .andExpect(status().isOk());

        verify(projectService).deleteTaskInProject("Test Project", 1L);
    }
}
