package com.taskmanager.taskmanager.TaskServiceTests;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;

import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceCRUDTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
    }

    @Test
    void createTask_shouldSetIsImportantFalseAndSave() {
        TaskEntity input = new TaskEntity();
        input.setTitle("Test");
        input.setDescription("Desc");
        input.setStatus("PENDING");
        input.setDueDate(LocalDate.now());

        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity saved = taskService.createTask(input);

        assertThat(saved.getIsImportant()).isFalse();
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void getAllTasks_shouldReturnTasksInCorrectOrder() {
        // Arrange
        TaskEntity t1 = new TaskEntity(); // important, due tomorrow
        t1.setIsImportant(true);
        t1.setDueDate(LocalDate.now().plusDays(1)); //14

        TaskEntity t2 = new TaskEntity(); // important, due today
        t2.setIsImportant(true);
        t2.setDueDate(LocalDate.now()); //13

        TaskEntity t3 = new TaskEntity(); // not important, due in 3 days
        t3.setIsImportant(false);
        t3.setDueDate(LocalDate.now().plusDays(3)); //16

        TaskEntity t4 = new TaskEntity(); // not important, due yesterday
        t4.setIsImportant(false);
        t4.setDueDate(LocalDate.now().minusDays(1)); //12

        List<TaskEntity> tasksFromRepo = List.of(t2,t1,t4,t3);

        when(taskRepository.findAllByProjectIdIsNullOrderByIsImportantAscDueDateAsc())
                .thenReturn(tasksFromRepo);

        // Act
        List<TaskEntity> result = taskService.getAllTasks();

        // Assert: repository already returns ordered list, so service must keep that order
        assertThat(result).containsExactly(t2,t1,t4,t3);
        verify(taskRepository)
                .findAllByProjectIdIsNullOrderByIsImportantAscDueDateAsc();
    }

    @Test
    void getTaskById_found() {
        TaskEntity t = new TaskEntity();
        t.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        TaskEntity result = taskService.getTaskById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskById_notFound_throws() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void updateTaskById_shouldCopyFieldsAndSave() {
        TaskEntity existing = new TaskEntity();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setDescription("Old");
        existing.setStatus("PENDING");
        existing.setDueDate(LocalDate.of(2025, 1, 1));
        existing.setIsImportant(false);

        TaskEntity incoming = new TaskEntity();
        incoming.setTitle("New");
        incoming.setDescription("New Desc");
        incoming.setStatus("IN_PROGRESS");
        incoming.setDueDate(LocalDate.of(2025, 2, 2));
        incoming.setIsImportant(true);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity updated = taskService.updateTaskById(1L, incoming);

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("New Desc");
        assertThat(updated.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(updated.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 2));
        assertThat(updated.getIsImportant()).isTrue();

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(existing);
    }

    @Test
    void deleteTaskById_shouldReturnCorrectReorderRequest() {
        TaskEntity t2 = new TaskEntity();
        t2.setId(2L);

        when(taskRepository.findAllByProjectIdIsNullOrderByIsImportantAscDueDateAsc())
                .thenReturn(List.of(t2));
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTaskById(1L);

        verify(taskRepository).deleteById(1L);
        verify(taskRepository).findAllByProjectIdIsNullOrderByIsImportantAscDueDateAsc();
    }
}
