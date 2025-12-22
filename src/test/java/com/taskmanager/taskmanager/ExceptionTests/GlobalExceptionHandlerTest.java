package com.taskmanager.taskmanager.ExceptionTests;

import com.taskmanager.taskmanager.exception.GlobalExceptionHandler;
import com.taskmanager.taskmanager.exception.ProjectNotFoundException;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleTaskNotFound_returns404AndBody() {
        TaskNotFoundException ex = new TaskNotFoundException("Task not found: 1");

        ResponseEntity<Map<String, Object>> response = handler.handleTaskNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Not Found");
        assertThat(body.get("message")).isEqualTo("Task not found: 1");
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    void handleProjectNotFound_returns404AndBody() {
        ProjectNotFoundException ex = new ProjectNotFoundException("Project not found: 99");

        ResponseEntity<Map<String, Object>> response = handler.handleProjectNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Not Found");
        assertThat(body.get("message")).isEqualTo("Project not found: 99");
        assertThat(body.get("timestamp")).isNotNull();
    }
}
