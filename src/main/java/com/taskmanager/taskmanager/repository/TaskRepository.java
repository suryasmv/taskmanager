package com.taskmanager.taskmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taskmanager.taskmanager.entity.TaskEntity;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    @Query("""
           SELECT t FROM TaskEntity t
           WHERE t.projectId IS NULL
           ORDER BY CASE WHEN t.isImportant = true THEN 0 ELSE 1 END ASC,
                    t.dueDate ASC
           """)
    List<TaskEntity> findAllByProjectIdIsNullOrderByIsImportantAscDueDateAsc();

    @Query("""
           SELECT t FROM TaskEntity t
           WHERE t.projectId = :projectId
           ORDER BY CASE WHEN t.isImportant = true THEN 0 ELSE 1 END ASC,
                    t.dueDate ASC
           """)
    List<TaskEntity> findAllByProjectIdOrderByIsImportantDescDueDateDesc(@Param("projectId") Long projectId);

    void deleteByProjectId(Long projectId);
}
