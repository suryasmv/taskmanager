package com.taskmanager.taskmanager.ProjectServiceTests;

import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.exception.ProjectNotFoundException;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceCRUDTest {

    @Mock
    private ProjectRepository projectRepository;

    // TaskRepository is needed for ProjectService constructor, but not used here
    @Mock
    private com.taskmanager.taskmanager.repository.TaskRepository taskRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, taskRepository, null);
    }

    @Test
    void createProject_shouldSaveAndReturn() {
        ProjectEntity p = new ProjectEntity();
        p.setName("Alpha");

        when(projectRepository.save(any(ProjectEntity.class)))
                .thenAnswer(invocation -> {
                    ProjectEntity arg = invocation.getArgument(0);
                    arg.setId(1L);
                    return arg;
                });

        ProjectEntity saved = projectService.createProject(p);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("Alpha");
        verify(projectRepository).save(p);
    }

    @Test
    void getAllProjects_shouldReturnList() {
        ProjectEntity p1 = new ProjectEntity();
        p1.setId(1L);
        ProjectEntity p2 = new ProjectEntity();
        p2.setId(2L);

        when(projectRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProjectEntity> result = projectService.getAllProjects();

        assertThat(result).hasSize(2);
        verify(projectRepository).findAll();
    }

    @Test
    void getProjectById_found() {
        ProjectEntity p = new ProjectEntity();
        p.setId(5L);

        when(projectRepository.findById(5L)).thenReturn(Optional.of(p));

        ProjectEntity result = projectService.getProjectById(5L);

        assertThat(result.getId()).isEqualTo(5L);
        verify(projectRepository).findById(5L);
    }

    @Test
    void getProjectById_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void getProjectByName_found() {
        ProjectEntity p = new ProjectEntity();
        p.setId(1L);
        p.setName("Alpha");

        when(projectRepository.findByName("Alpha")).thenReturn(Optional.of(p));

        ProjectEntity result = projectService.getProjectByName("Alpha");

        assertThat(result.getName()).isEqualTo("Alpha");
        verify(projectRepository).findByName("Alpha");
    }

    @Test
    void getProjectByName_notFound_throws() {
        when(projectRepository.findByName("Missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectByName("Missing"))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void updateProject_shouldSetIdAndSave() {
        ProjectEntity p = new ProjectEntity();
        p.setName("Updated");

        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);

        projectService.UpdateProject(10L, p);

        verify(projectRepository).save(captor.capture());
        ProjectEntity saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getName()).isEqualTo("Updated");
    }

    @Test
    void deleteProject_shouldCallRepositoryDelete() {
        projectService.deleteProject(7L);

        verify(projectRepository).deleteById(7L);
    }
}
