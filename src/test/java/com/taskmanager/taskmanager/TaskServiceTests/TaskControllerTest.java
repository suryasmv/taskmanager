package com.taskmanager.taskmanager.TaskServiceTests;

import com.taskmanager.taskmanager.controller.TaskController;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.enums.ImportanceFilter;
import com.taskmanager.taskmanager.enums.SortDirection;
import com.taskmanager.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createTask_returnsCreatedTask() throws Exception {
        TaskEntity request = new TaskEntity();
        request.setTitle("T1");

        TaskEntity response = new TaskEntity();
        response.setId(1L);
        response.setTitle("T1");

        when(taskService.createTask(any(TaskEntity.class))).thenReturn(response);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("T1")));

        verify(taskService).createTask(any(TaskEntity.class));
    }

    @Test
    void getAllTasks_returnsList() throws Exception {
        TaskEntity t = new TaskEntity();
        t.setId(1L);
        t.setTitle("T1");
        t.setDueDate(LocalDate.now());

        when(taskService.getAllTasks()).thenReturn(List.of(t));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(taskService).getAllTasks();
    }

    @Test
    void getTaskById_returnsTask() throws Exception {
        TaskEntity t = new TaskEntity();
        t.setId(5L);
        t.setTitle("T5");

        when(taskService.getTaskById(5L)).thenReturn(t);

        mockMvc.perform(get("/tasks/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)));

        verify(taskService).getTaskById(5L);
    }

    @Test
    void updateTask_returnsUpdatedTask() throws Exception {
        TaskEntity body = new TaskEntity();
        body.setTitle("Updated");

        TaskEntity resp = new TaskEntity();
        resp.setId(5L);
        resp.setTitle("Updated");

        when(taskService.updateTaskById(eq(5L), any(TaskEntity.class))).thenReturn(resp);

        mockMvc.perform(put("/tasks/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.title", is("Updated")));

        verify(taskService).updateTaskById(eq(5L), any(TaskEntity.class));
    }

    @Test
    void deleteTask_callsService() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 3L))
                .andExpect(status().isOk());

        verify(taskService).deleteTaskById(3L);
    }

    @Test
    void filterTasks_passesEnumsAndReturnsList() throws Exception {
        TaskEntity t1 = new TaskEntity();
        t1.setId(1L);
        t1.setTitle("Imp");

        when(taskService.getTasksWithFilters(
                ImportanceFilter.IMPORTANT_ONLY,
                SortDirection.DESC))
                .thenReturn(List.of(t1));

        mockMvc.perform(get("/tasks/filter")
                        .param("importance", "IMPORTANT_ONLY")
                        .param("sortDir", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(taskService).getTasksWithFilters(
                ImportanceFilter.IMPORTANT_ONLY,
                SortDirection.DESC);
    }

    @Test
    void filterTasks_usesDefaultParamsWhenNotProvided() throws Exception {
        TaskEntity t1 = new TaskEntity();
        t1.setId(1L);

        when(taskService.getTasksWithFilters(
                ImportanceFilter.ALL,
                SortDirection.ASC))
                .thenReturn(List.of(t1));

        mockMvc.perform(get("/tasks/filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(taskService).getTasksWithFilters(
                ImportanceFilter.ALL,
                SortDirection.ASC);
    }
}
