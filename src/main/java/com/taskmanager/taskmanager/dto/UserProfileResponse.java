// UserProfileResponse.java
package com.taskmanager.taskmanager.dto;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String avatarUrl
) {}
