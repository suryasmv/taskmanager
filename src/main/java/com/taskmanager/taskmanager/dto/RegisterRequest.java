package com.taskmanager.taskmanager.dto;

public record RegisterRequest(
        String fullName,
        String email,
        String password,
        String confirmPassword
) { }
