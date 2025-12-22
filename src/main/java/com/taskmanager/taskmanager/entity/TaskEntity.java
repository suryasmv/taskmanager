package com.taskmanager.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    String title;

    String description;

    String status;

    LocalDate dueDate;

    @Column(name = "is_important")
    Boolean isImportant = false;

    @CreationTimestamp LocalDateTime createdAt;

    @UpdateTimestamp LocalDateTime updatedAt;

    Long projectId;
}

