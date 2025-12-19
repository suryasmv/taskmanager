package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    @Query(value = "SELECT * FROM projects p WHERE LOWER(TRIM(p.name)) = LOWER(TRIM(:name)) LIMIT 1", nativeQuery = true)
    Optional<ProjectEntity> findByName(@Param("name") String name);
}
