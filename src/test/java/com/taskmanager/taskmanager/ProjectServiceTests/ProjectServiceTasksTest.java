package com.taskmanager.taskmanager.ProjectServiceTests;

import com.taskmanager.taskmanager.dto.TaskReorderRequest;
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
class ProjectServiceTasksTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    // TaskService needed for constructor, but not used here
    @Mock
    private TaskService taskService;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, taskRepository, taskService);
    }

    private ProjectEntity project(long id, String name) {
        ProjectEntity p = new ProjectEntity();
        p.setId(id);
        p.setName(name);
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

    // 1) createTaskForProject

    @Test
    void createTaskForProject_setsProjectIdAndIsImportantFalse() {
        ProjectEntity p = project(10L, "Alpha");
        TaskEntity input = new TaskEntity();
        input.setTitle("Task1");

        when(projectRepository.findByName("Alpha")).thenReturn(Optional.of(p));
        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity saved = projectService.createTaskForProject("Alpha", input);

        assertThat(saved.getProjectId()).isEqualTo(10L);
        assertThat(saved.getIsImportant()).isFalse();
        verify(taskRepository).save(input);
    }

    @Test
    void createTaskForProject_projectNotFound_throws() {
        when(projectRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.createTaskForProject("Missing", new TaskEntity())
        ).isInstanceOf(ProjectNotFoundException.class);
    }

    // 2) getTasksForProject

    @Test
    void getTasksForProject_usesRepositoryMethod() {
        ProjectEntity p = project(5L, "Alpha");
        TaskEntity t1 = task(1L, 5L, true, LocalDate.now());

        when(projectRepository.findByName("Alpha")).thenReturn(Optional.of(p));
        when(taskRepository.findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L))
                .thenReturn(List.of(t1));

        List<TaskEntity> result = projectService.getTasksForProject("Alpha");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(taskRepository)
                .findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L);
    }

    // 3) getTaskInProject

    @Test
    void getTaskInProject_taskBelongsToProject_returnsTask() {
        ProjectEntity p = project(5L, "Alpha");
        TaskEntity t = task(1L, 5L, false, LocalDate.now());

        when(projectRepository.findByName("Alpha")).thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        TaskEntity result = projectService.getTaskInProject("Alpha", 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getTaskInProject_taskNotFound_throws() {
        ProjectEntity p = project(5L, "Alpha");

        when(projectRepository.findByName("Alpha")).thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.getTaskInProject("Alpha", 1L)
        ).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void getTaskInProject_taskFromOtherProject_throws() {
        ProjectEntity p = project(5L, "Alpha");
        TaskEntity t = task(1L, 99L, false, LocalDate.now()); // different projectId

        when(projectRepository.findByName("Alpha")).thenReturn(Optional.of(p));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() ->
                projectService.getTaskInProject("Alpha", 1L)
        ).isInstanceOf(TaskNotFoundException.class);
    }

    // 4) deleteTaskInProject

    @Test
    void deleteTaskInProject_deletesAndReorders() {
        ProjectEntity p = project(5L, "Alpha");
        TaskEntity remaining = task(2L, 5L, false, LocalDate.now());

        when(projectRepository.findByName("Alpha")).thenReturn(Optional.of(p));
        when(taskRepository
                .findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L))
                .thenReturn(List.of(remaining));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(remaining));

        // capture saveAll from reorderTasksInProject
        ArgumentCaptor<List<TaskEntity>> captor = ArgumentCaptor.forClass(List.class);

        projectService.deleteTaskInProject("Alpha", 1L);

        verify(taskRepository).deleteById(1L);
        verify(taskRepository)
                .findAllByProjectIdOrderByIsImportantDescDueDateDesc(5L);
        verify(taskRepository).saveAll(captor.capture());

        List<TaskEntity> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void deleteTaskInProject_projectNotFound_throws() {
        when(projectRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.deleteTaskInProject("Missing", 1L)
        ).isInstanceOf(ProjectNotFoundException.class);
    }
}
