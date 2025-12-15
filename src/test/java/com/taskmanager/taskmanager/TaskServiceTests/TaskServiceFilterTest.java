package com.taskmanager.taskmanager.TaskServiceTests;

import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.enums.ImportanceFilter;
import com.taskmanager.taskmanager.enums.SortDirection;
import com.taskmanager.taskmanager.repository.TaskRepository;
import com.taskmanager.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceFilterTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
    }

    // helper to build tasks
    private TaskEntity task(Long id, Long projectId, boolean important, LocalDate dueDate) {
        TaskEntity t = new TaskEntity();
        t.setId(id);
        t.setProjectId(projectId);
        t.setIsImportant(important);
        t.setDueDate(dueDate);
        return t;
    }

    private List<TaskEntity> sampleTasks() {
        TaskEntity t1 = task(1L, null,  true,  LocalDate.of(2025, 11, 10)); // imp
        TaskEntity t2 = task(2L, 100L, false, LocalDate.of(2025, 11, 11)); // normal
        TaskEntity t3 = task(3L, 200L, true,  LocalDate.of(2025, 11,  9)); // imp
        TaskEntity t4 = task(4L, null,  false, LocalDate.of(2025, 11, 12)); // normal
        return List.of(t1, t2, t3, t4);
    }

    // 1) IMPORTANT_ONLY + ASC
    @Test
    void importantOnly_sortedAsc_allProjectsAndNormal() {
        when(taskRepository.findAll()).thenReturn(sampleTasks());

        var result = taskService.getTasksWithFilters(
                ImportanceFilter.IMPORTANT_ONLY,
                SortDirection.ASC
        );

        System.out.println("Important+ASC IDs = " +
                result.stream().map(TaskEntity::getId).toList());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> Boolean.TRUE.equals(t.getIsImportant()));
        // important tasks: id=3 (2025-11-09), id=1 (2025-11-10)
        assertThat(result.get(0).getId()).isEqualTo(3L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }

    // 2) IMPORTANT_ONLY + DESC
    @Test
    void importantOnly_sortedDesc_allProjectsAndNormal() {
        when(taskRepository.findAll()).thenReturn(sampleTasks());

        var result = taskService.getTasksWithFilters(
                ImportanceFilter.IMPORTANT_ONLY,
                SortDirection.DESC
        );

        System.out.println("Important+DESC IDs = " +
                result.stream().map(TaskEntity::getId).toList());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> Boolean.TRUE.equals(t.getIsImportant()));
        // DESC by date: 10 -> 9
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(3L);
    }

    // 3) NORMAL_ONLY + ASC
    @Test
    void normalOnly_sortedAsc_allProjectsAndNormal() {
        when(taskRepository.findAll()).thenReturn(sampleTasks());

        var result = taskService.getTasksWithFilters(
                ImportanceFilter.NORMAL_ONLY,
                SortDirection.ASC
        );

        System.out.println("Normal+ASC IDs = " +
                result.stream().map(TaskEntity::getId).toList());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> !Boolean.TRUE.equals(t.getIsImportant()));
        // normal tasks: id=2 (11th), id=4 (12th) => ASC: 11 -> 12
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(4L);
    }

    // 4) NORMAL_ONLY + DESC
    @Test
    void normalOnly_sortedDesc_allProjectsAndNormal() {
        when(taskRepository.findAll()).thenReturn(sampleTasks());

        var result = taskService.getTasksWithFilters(
                ImportanceFilter.NORMAL_ONLY,
                SortDirection.DESC
        );

        System.out.println("Normal+DESC IDs = " +
                result.stream().map(TaskEntity::getId).toList());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> !Boolean.TRUE.equals(t.getIsImportant()));
        // DESC: 12 -> 11
        assertThat(result.get(0).getId()).isEqualTo(4L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    // 5) ALL + ASC
    @Test
    void allImportance_sortedAsc_allTasks() {
        when(taskRepository.findAll()).thenReturn(sampleTasks());

        var result = taskService.getTasksWithFilters(
                ImportanceFilter.ALL,
                SortDirection.ASC
        );

        System.out.println("ALL+ASC IDs = " +
                result.stream().map(TaskEntity::getId).toList());

        // all 4 tasks, sorted by due date asc: 9,10,11,12
        assertThat(result).hasSize(4);
        assertThat(result.get(0).getId()).isEqualTo(3L); // 9
        assertThat(result.get(1).getId()).isEqualTo(1L); // 10
        assertThat(result.get(2).getId()).isEqualTo(2L); // 11
        assertThat(result.get(3).getId()).isEqualTo(4L); // 12
    }

    // 6) ALL + DESC
    @Test
    void allImportance_sortedDesc_allTasks() {
        when(taskRepository.findAll()).thenReturn(sampleTasks());

        var result = taskService.getTasksWithFilters(
                ImportanceFilter.ALL,
                SortDirection.DESC
        );

        System.out.println("ALL+DESC IDs = " +
                result.stream().map(TaskEntity::getId).toList());

        // all 4 tasks, sorted by due date desc: 12,11,10,9
        assertThat(result).hasSize(4);
        assertThat(result.get(0).getId()).isEqualTo(4L); // 12
        assertThat(result.get(1).getId()).isEqualTo(2L); // 11
        assertThat(result.get(2).getId()).isEqualTo(1L); // 10
        assertThat(result.get(3).getId()).isEqualTo(3L); // 9
    }
}
