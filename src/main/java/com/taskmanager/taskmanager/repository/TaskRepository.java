package com.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.taskmanager.taskmanager.entity.TaskEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    //Find Max Priority
    @Query("select max(t.priority) from TaskEntity t where t.projectId is null")
    Optional<Integer> findMaxPriority();

    //Find Tasks by Priority
    @Query("""
           select t
           from TaskEntity t
           where t.projectId is null
           order by case when t.priority is null then 1 else 0 end,
           t.priority asc
           """)
    List<TaskEntity> findAllByOrderByPriorityAsc();

    @Query("""
           select t
           from TaskEntity t
           where t.projectId = :projectId
           order by case when t.priority is null then 1 else 0 end,
                    t.priority asc
           """)
    List<TaskEntity> findAllByProjectIdOrderByPriorityAsc(Long projectId);
}
