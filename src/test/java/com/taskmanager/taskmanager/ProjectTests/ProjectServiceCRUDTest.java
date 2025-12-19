package com.taskmanager.taskmanager.ProjectTests;

import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.exception.ProjectNotFoundException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.service.ProjectService;
import com.taskmanager.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceCRUDTest {

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

    @Test
    void createProject_savesAndReturnsProject() {
        ProjectEntity input = project(1L);
        when(projectRepository.save(input)).thenReturn(input);

        ProjectEntity result = projectService.createProject(input);

        assertThat(result).isSameAs(input);
        verify(projectRepository).save(input);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getAllProjects_returnsList() {
        ProjectEntity p = project(1L);
        when(projectRepository.findAll()).thenReturn(List.of(p));

        List<ProjectEntity> result = projectService.getAllProjects();

        assertThat(result).containsExactly(p);
        verify(projectRepository).findAll();
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getProjectById_existing_returnsProject() {
        ProjectEntity p = project(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        ProjectEntity result = projectService.getProjectById(1L);

        assertThat(result).isSameAs(p);
        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getProjectById_missing_throws() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(1L))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getProjectByName_existing_returnsProject() {
        ProjectEntity p = project(1L);
        when(projectRepository.findByName("Home Related")).thenReturn(Optional.of(p));

        ProjectEntity result = projectService.getProjectByName("Home Related");

        assertThat(result).isSameAs(p);
        verify(projectRepository).findByName("Home Related");
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getProjectByName_missing_throws() {
        when(projectRepository.findByName("Home Related")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectByName("Home Related"))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository).findByName("Home Related");
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void updateProject_setsIdAndSaves() {
        ProjectEntity p = project(0L);

        projectService.UpdateProject(5L, p);

        assertThat(p.getId()).isEqualTo(5L);
        verify(projectRepository).save(p);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void deleteProject_deletesTasksThenProject() {
        projectService.deleteProject(5L);

        verify(taskRepository).deleteByProjectId(5L);
        verify(projectRepository).deleteById(5L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }
}
