package com.taskmanager.taskmanager.ProjectTests;

import com.taskmanager.taskmanager.entity.ProjectEntity;
import com.taskmanager.taskmanager.entity.UserEntity;
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
import static org.mockito.ArgumentMatchers.*;
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

    // ---------- createProject ----------

    @Test
    void createProject_savesAndReturnsProject_whenNameNotExistsForUser() {
        ProjectEntity input = project(0L);

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.empty());
        when(projectRepository.save(input)).thenReturn(input);

        ProjectEntity result = projectService.createProject(input, user);

        assertThat(result).isSameAs(input);
        assertThat(result.getUser()).isSameAs(user);
        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verify(projectRepository).save(input);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void createProject_duplicateNameForSameUser_throws() {
        ProjectEntity existing = project(1L);
        ProjectEntity input = project(0L);

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> projectService.createProject(input, user))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("already exists");

        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    // ---------- getAllProjectsForUser ----------

    @Test
    void getAllProjectsForUser_returnsList() {
        ProjectEntity p = project(1L);

        when(projectRepository.findAllByUserId(1L)).thenReturn(List.of(p));

        List<ProjectEntity> result = projectService.getAllProjectsForUser(user);

        assertThat(result).containsExactly(p);
        verify(projectRepository).findAllByUserId(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    // ---------- getProjectByIdForUser ----------

    @Test
    void getProjectByIdForUser_existingAndOwned_returnsProject() {
        ProjectEntity p = project(1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        ProjectEntity result = projectService.getProjectByIdForUser(1L, user);

        assertThat(result).isSameAs(p);
        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getProjectByIdForUser_missing_throws() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectByIdForUser(1L, user))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getProjectByIdForUser_ownedByOtherUser_throws() {
        ProjectEntity p = project(1L);
        UserEntity other = new UserEntity();
        other.setId(99L);
        p.setUser(other);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> projectService.getProjectByIdForUser(1L, user))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    // ---------- getProjectByNameForUser ----------

    @Test
    void getProjectByNameForUser_existing_returnsProject() {
        ProjectEntity p = project(1L);

        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.of(p));

        ProjectEntity result = projectService.getProjectByNameForUser("Home Related", user);

        assertThat(result).isSameAs(p);
        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    @Test
    void getProjectByNameForUser_missing_throws() {
        when(projectRepository.findByNameAndUserId("Home Related", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectByNameForUser("Home Related", user))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository).findByNameAndUserId("Home Related", 1L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    // ---------- updateProjectForUser ----------

    @Test
    void updateProjectForUser_updatesNameAndSaves() {
        ProjectEntity existing = project(5L);
        ProjectEntity input = new ProjectEntity();
        input.setName("Updated");

        when(projectRepository.findById(5L)).thenReturn(Optional.of(existing));

        projectService.updateProjectForUser(5L, input, user);

        assertThat(existing.getName()).isEqualTo("Updated");
        verify(projectRepository).findById(5L);
        verify(projectRepository).save(existing);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }

    // ---------- deleteProjectForUser ----------

    @Test
    void deleteProjectForUser_deletesTasksThenProject() {
        ProjectEntity existing = project(5L);

        when(projectRepository.findById(5L)).thenReturn(Optional.of(existing));

        projectService.deleteProjectForUser(5L, user);

        verify(projectRepository).findById(5L);
        verify(taskRepository).deleteByProjectId(5L);
        verify(projectRepository).deleteById(5L);
        verifyNoMoreInteractions(projectRepository, taskRepository, taskService);
    }
}
