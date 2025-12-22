package com.taskmanager.taskmanager.TaskTests;

import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceCRUDTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);

        user = new UserEntity();
        user.setId(1L);
        user.setFullName("Test User");
        user.setEmail("test@example.com");
    }

    @Test
    void createTaskForUser_shouldSetUserAndIsImportantFalseAndSave() {
        TaskEntity input = new TaskEntity();
        input.setTitle("Test");
        input.setDescription("Desc");
        input.setStatus("PENDING");
        input.setDueDate(LocalDate.now());

        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity saved = taskService.createTaskForUser(input, user);

        assertThat(saved.getIsImportant()).isFalse();
        assertThat(saved.getUser()).isSameAs(user);
        verify(taskRepository).save(input);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getAllTasksForUser_shouldUseUserScopedRepositoryMethod() {
        TaskEntity t = new TaskEntity();
        t.setId(1L);
        t.setTitle("T1");
        t.setDueDate(LocalDate.now());
        t.setUser(user);

        when(taskRepository.findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L))
                .thenReturn(List.of(t));

        List<TaskEntity> result = taskService.getAllTasksForUser(user);

        assertThat(result).containsExactly(t);
        verify(taskRepository)
                .findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getTaskByIdForUser_foundAndOwned_returnsTask() {
        TaskEntity t = new TaskEntity();
        t.setId(1L);
        t.setUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        TaskEntity result = taskService.getTaskByIdForUser(1L, user);

        assertThat(result.getId()).isEqualTo(1L);
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getTaskByIdForUser_notFound_throws() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskByIdForUser(99L, user))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).findById(99L);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void getTaskByIdForUser_ownedByOtherUser_throws() {
        UserEntity other = new UserEntity();
        other.setId(2L);

        TaskEntity t = new TaskEntity();
        t.setId(1L);
        t.setUser(other);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> taskService.getTaskByIdForUser(1L, user))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void updateTaskByIdForUser_shouldCopyFieldsAndSave() {
        TaskEntity existing = new TaskEntity();
        existing.setId(1L);
        existing.setUser(user);
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

        TaskEntity updated = taskService.updateTaskByIdForUser(1L, incoming, user);

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("New Desc");
        assertThat(updated.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(updated.getDueDate()).isEqualTo(LocalDate.of(2025, 2, 2));
        assertThat(updated.getIsImportant()).isTrue();
        assertThat(updated.getUser()).isSameAs(user);

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(existing);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void deleteTaskByIdForUser_deletesAndReordersUserTasks() {
        TaskEntity tExisting = new TaskEntity();
        tExisting.setId(1L);
        tExisting.setUser(user);

        TaskEntity t2 = new TaskEntity();
        t2.setId(2L);
        t2.setUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(tExisting));
        when(taskRepository.findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L))
                .thenReturn(List.of(t2));

        taskService.deleteTaskByIdForUser(1L, user);

        verify(taskRepository).findById(1L);
        verify(taskRepository).deleteById(1L);
        verify(taskRepository)
                .findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L);
        verifyNoMoreInteractions(taskRepository);
    }
}
