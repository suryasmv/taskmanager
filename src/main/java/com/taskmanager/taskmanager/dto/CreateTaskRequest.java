package com.taskmanager.taskmanager.dto;

import java.time.LocalDate;

public record CreateTaskRequest (
        String title,
        String description,
        String status,
        LocalDate dueDate,
        Long projectId
) {}

