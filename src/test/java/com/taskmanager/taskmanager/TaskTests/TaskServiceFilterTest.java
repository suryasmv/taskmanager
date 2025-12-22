package com.taskmanager.taskmanager.TaskTests;

import com.taskmanager.taskmanager.entity.TaskEntity;
import com.taskmanager.taskmanager.entity.UserEntity;
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
    private UserEntity user;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);

        user = new UserEntity();
        user.setId(1L);
        user.setFullName("Test User");
        user.setEmail("test@example.com");
    }

    private TaskEntity task(Long id, boolean important, LocalDate dueDate) {
        TaskEntity t = new TaskEntity();
        t.setId(id);
        t.setIsImportant(important);
        t.setDueDate(dueDate);
        t.setUser(user);
        return t;
    }

    private List<TaskEntity> sampleTasks() {
        TaskEntity t1 = task(1L, true,  LocalDate.of(2025, 11, 10)); // imp
        TaskEntity t2 = task(2L, false, LocalDate.of(2025, 11, 11)); // normal
        TaskEntity t3 = task(3L, true,  LocalDate.of(2025, 11,  9)); // imp
        TaskEntity t4 = task(4L, false, LocalDate.of(2025, 11, 12)); // normal
        return List.of(t1, t2, t3, t4);
    }

    @Test
    void importantOnly_sortedAsc() {
        when(taskRepository.findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L))
                .thenReturn(sampleTasks());

        var result = taskService.getTasksWithFiltersForUser(
                user,
                ImportanceFilter.IMPORTANT_ONLY,
                SortDirection.ASC
        );

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> Boolean.TRUE.equals(t.getIsImportant()));
        // important tasks: id=3 (9th), id=1 (10th)
        assertThat(result.get(0).getId()).isEqualTo(3L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }

    @Test
    void importantOnly_sortedDesc() {
        when(taskRepository.findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L))
                .thenReturn(sampleTasks());

        var result = taskService.getTasksWithFiltersForUser(
                user,
                ImportanceFilter.IMPORTANT_ONLY,
                SortDirection.DESC
        );

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> Boolean.TRUE.equals(t.getIsImportant()));
        // DESC: 10 -> 9
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(3L);
    }

    @Test
    void normalOnly_sortedAsc() {
        when(taskRepository.findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L))
                .thenReturn(sampleTasks());

        var result = taskService.getTasksWithFiltersForUser(
                user,
                ImportanceFilter.NORMAL_ONLY,
                SortDirection.ASC
        );

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> !Boolean.TRUE.equals(t.getIsImportant()));
        // normal tasks: id=2 (11th), id=4 (12th) => ASC: 11 -> 12
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(4L);
    }

    @Test
    void normalOnly_sortedDesc() {
        when(taskRepository.findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L))
                .thenReturn(sampleTasks());

        var result = taskService.getTasksWithFiltersForUser(
                user,
                ImportanceFilter.NORMAL_ONLY,
                SortDirection.DESC
        );

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> !Boolean.TRUE.equals(t.getIsImportant()));
        // DESC: 12 -> 11
        assertThat(result.get(0).getId()).isEqualTo(4L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void allImportance_sortedAsc() {
        when(taskRepository.findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L))
                .thenReturn(sampleTasks());

        var result = taskService.getTasksWithFiltersForUser(
                user,
                ImportanceFilter.ALL,
                SortDirection.ASC
        );

        // all 4 tasks, sorted by due date asc: 9,10,11,12
        assertThat(result).hasSize(4);
        assertThat(result.get(0).getId()).isEqualTo(3L); // 9
        assertThat(result.get(1).getId()).isEqualTo(1L); // 10
        assertThat(result.get(2).getId()).isEqualTo(2L); // 11
        assertThat(result.get(3).getId()).isEqualTo(4L); // 12
    }

    @Test
    void allImportance_sortedDesc() {
        when(taskRepository.findAllByUserIdAndProjectIdIsNullOrderByIsImportantAscDueDateAsc(1L))
                .thenReturn(sampleTasks());

        var result = taskService.getTasksWithFiltersForUser(
                user,
                ImportanceFilter.ALL,
                SortDirection.DESC
        );

        // all 4 tasks, sorted by due date desc: 12,11,10,9
        assertThat(result).hasSize(4);
        assertThat(result.get(0).getId()).isEqualTo(4L); // 12
        assertThat(result.get(1).getId()).isEqualTo(2L); // 11
        assertThat(result.get(2).getId()).isEqualTo(1L); // 10
        assertThat(result.get(3).getId()).isEqualTo(3L); // 9
    }
}
