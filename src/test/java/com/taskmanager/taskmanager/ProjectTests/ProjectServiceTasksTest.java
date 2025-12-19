package com.taskmanager.taskmanager.ProjectTests;

import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.TaskEntity;
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
import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, taskRepository, taskService);
    }

    private ProjectEntity project(long id) {
        ProjectEntity p = new ProjectEntity();
        p.setId(id);
        p.setName("Home Related");
        return p;
    }

    private TaskEntity task(long id, long projectId, boolean important, LocalDate dueDate) {
        TaskEntity t = new TaskEntity();
        t.setId(id);
        t.setProjectId(projectId);
        t.setIsImportant(important);
        t.setDueDate(dueDate);
        return t;
    }

    @Test
    void createTaskForProject_setsProjectIdAndIsImportantFalse() {
        ProjectEntity p = project(10L);
        TaskEntity input = new TaskEntity();
        input.setTitle("Task1");

        when(projectRepository.findByName("Home Related")).thenReturn(Optional.of(p));
        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity saved = projectService.createTaskForProject("Home Related", input);

        assertThat(saved.getProjectId()).isEqualTo(10L);
        assertThat(saved.getIsImportant()).isFalse();
        verify(projectRepository).findByName("Home Related");
        verify(taskRepository).save(input);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void createTaskForProject_projectNotFound_throws() {
        when(projectRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.createTaskForProject("Missing", new TaskEntity())
        ).isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository).findByName("Missing");
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getTasksForProject_usesRepositoryMethod() {
        ProjectEntity p = project(5L);
        TaskEntity t1 = task(1L, 5L, true, LocalDate.now());

        when(projectRepository.findByName("Home Related")).thenReturn(Optional.of(p));
        when(taskRepository.findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L))
                .thenReturn(List.of(t1));

        List<TaskEntity> result = projectService.getTasksForProject("Home Related");

        assertThat(result).containsExactly(t1);
        verify(projectRepository).findByName("Home Related");
        verify(taskRepository).findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getTaskInProject_taskBelongsToProject_returnsTask() {
        ProjectEntity p = project(5L);
        TaskEntity t = task(1L, 5L, false, LocalDate.now());

        when(projectRepository.findByName("Home Related")).thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        TaskEntity result = projectService.getTaskInProject("Home Related", 1L);

        assertThat(result).isSameAs(t);
        verify(projectRepository).findByName("Home Related");
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getTaskInProject_taskNotFound_throws() {
        ProjectEntity p = project(5L);

        when(projectRepository.findByName("Home Related")).thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.getTaskInProject("Home Related", 1L)
        ).isInstanceOf(TaskNotFoundException.class);

        verify(projectRepository).findByName("Home Related");
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getTaskInProject_taskFromOtherProject_throws() {
        ProjectEntity p = project(5L);
        TaskEntity t = task(1L, 99L, false, LocalDate.now());

        when(projectRepository.findByName("Home Related")).thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() ->
                projectService.getTaskInProject("Home Related", 1L)
        ).isInstanceOf(TaskNotFoundException.class);

        verify(projectRepository).findByName("Home Related");
        verify(taskRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void deleteTaskInProject_deletesAndBuildsRequest() {
        ProjectEntity p = project(5L);
        TaskEntity remaining = task(2L, 5L, false, LocalDate.now());

        when(projectRepository.findByName("Home Related")).thenReturn(Optional.of(p));
        when(taskRepository.findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L))
                .thenReturn(List.of(remaining));

        projectService.deleteTaskInProject("Home Related", 1L);

        verify(taskRepository).deleteById(1L);
        verify(projectRepository).findByName("Home Related");
        verify(taskRepository).findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void deleteTaskInProject_projectNotFound_throws() {
        when(projectRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.deleteTaskInProject("Missing", 1L)
        ).isInstanceOf(ProjectNotFoundException.class);

        verify(taskRepository).deleteById(1L);
        verify(projectRepository).findByName("Missing");
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }
}
