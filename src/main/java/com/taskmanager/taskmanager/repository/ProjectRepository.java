package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Optional<ProjectEntity> findByName(String name);

    // PROJECT tasks (project-specific)
    @Query("select max(t.priority) from TaskEntity t where t.projectId = :projectId")
    Optional<Integer> findMaxPriorityByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
}
