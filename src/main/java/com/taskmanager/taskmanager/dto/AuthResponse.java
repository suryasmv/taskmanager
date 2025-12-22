package com.taskmanager.taskmanager.dto;

public record AuthResponse(String accessToken, String fullName, String email) {
}
