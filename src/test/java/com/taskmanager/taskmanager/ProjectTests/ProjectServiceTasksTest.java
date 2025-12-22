package com.taskmanager.taskmanager.ProjectTests;

import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.exception.ProjectNotFoundException;
import com.taskmanager.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.service.ProjectService;
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
class ProjectServiceTasksTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskService taskService;

    private ProjectService projectService;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, taskRepository, taskService);

        user = new UserEntity();
        user.setId(1L);
        user.setFullName("Test User");
        user.setEmail("test@example.com");
    }

    private ProjectEntity project(long id) {
        ProjectEntity p = new ProjectEntity();
        p.setId(id);
        p.setName("Home Related");
        p.setUser(user);
        return p;
    }

    private TaskEntity task(long id, long projectId, boolean important, LocalDate dueDate) {
        TaskEntity t = new TaskEntity();
        t.setId(id);
        t.setProjectId(projectId);
        t.setIsImportant(important);
        t.setDueDate(dueDate);
        t.setUser(user);
        return t;
    }

    // ---------- createTaskForProjectForUser ----------

    @Test
    void createTaskForProjectForUser_setsProjectIdAndIsImportantFalse() {
        ProjectEntity p = project(10L);
        TaskEntity input = new TaskEntity();
        input.setTitle("Task1");

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(p));
        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity saved = projectService.createTaskForProjectForUser("Home Related", input, user);

        assertThat(saved.getProjectId()).isEqualTo(10L);
        assertThat(saved.getIsImportant()).isFalse();
        assertThat(saved.getUser()).isSameAs(user);
        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verify(taskRepository).save(input);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void createTaskForProjectForUser_projectNotFound_throws() {
        when(projectRepository.findByNameAndUserId("Missing", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.createTaskForProjectForUser("Missing", new TaskEntity(), user)
        ).isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository).findByNameAndUserId("Missing", 1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    // ---------- getTasksForProjectForUser ----------

    @Test
    void getTasksForProjectForUser_usesRepositoryMethod() {
        ProjectEntity p = project(5L);
        TaskEntity t1 = task(1L, 5L, true, LocalDate.now());

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(p));
        when(taskRepository.findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L))
                .thenReturn(List.of(t1));

        List<TaskEntity> result = projectService.getTasksForProjectForUser("Home Related", user);

        assertThat(result).containsExactly(t1);
        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verify(taskRepository).findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    // ---------- getTaskInProjectForUser ----------

    @Test
    void getTaskInProjectForUser_taskBelongsToProjectAndUser_returnsTask() {
        ProjectEntity p = project(5L);
        TaskEntity t = task(1L, 5L, false, LocalDate.now());

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        TaskEntity result = projectService.getTaskInProjectForUser("Home Related", 1L, user);

        assertThat(result).isSameAs(t);
        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getTaskInProjectForUser_taskNotFound_throws() {
        ProjectEntity p = project(5L);

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.getTaskInProjectForUser("Home Related", 1L, user)
        ).isInstanceOf(TaskNotFoundException.class);

        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getTaskInProjectForUser_taskFromOtherProject_throws() {
        ProjectEntity p = project(5L);
        TaskEntity t = task(1L, 99L, false, LocalDate.now());

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() ->
                projectService.getTaskInProjectForUser("Home Related", 1L, user)
        ).isInstanceOf(TaskNotFoundException.class);

        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    // ---------- deleteTaskInProjectForUser ----------

    @Test
    void deleteTaskInProjectForUser_deletesAndBuildsRequest() {
        ProjectEntity p = project(5L);
        TaskEntity t1 = task(1L, 5L, false, LocalDate.now());
        TaskEntity remaining = task(2L, 5L, false, LocalDate.now());

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t1));
        when(taskRepository.findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L))
                .thenReturn(List.of(remaining));

        projectService.deleteTaskInProjectForUser("Home Related", 1L, user);

        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verify(taskRepository).findById(1L);
        verify(taskRepository).deleteById(1L);
        verify(taskRepository).findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void deleteTaskInProjectForUser_taskNotFound_throws() {
        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(project(5L)));
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.deleteTaskInProjectForUser("Home Related", 1L, user)
        ).isInstanceOf(TaskNotFoundException.class);

        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void deleteTaskInProjectForUser_projectNotFound_throws() {
        when(projectRepository.findByNameAndUserId("Missing", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.deleteTaskInProjectForUser("Missing", 1L, user)
        ).isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository).findByNameAndUserId("Missing", 1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

}
