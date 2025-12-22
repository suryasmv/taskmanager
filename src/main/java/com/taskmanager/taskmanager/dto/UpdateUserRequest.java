package com.taskmanager.taskmanager.dto;

public record UpdateUserRequest(
        String fullName,
        String avatarUrl,
        String currentPassword,   // required when changing password
        String newPassword,
        String confirmNewPassword
) {}
