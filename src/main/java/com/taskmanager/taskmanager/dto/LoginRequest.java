package com.taskmanager.taskmanager.dto;

public record LoginRequest(
        String email,
        String password
) { }
