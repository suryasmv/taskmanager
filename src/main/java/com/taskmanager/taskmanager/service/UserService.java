package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.UpdateUserRequest;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEntity updateCurrentUser(Long userId, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // update basic profile
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }

        // handle password change if newPassword present
        if (request.newPassword() != null && !request.newPassword().isBlank()) {

            if (user.getPasswordHash() == null) {
                throw new RuntimeException("This account was created with Google.");
            }

            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new RuntimeException("Current password is required to change password.");
            }

            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Current password is incorrect.");
            }

            if (!request.newPassword().equals(request.confirmNewPassword())) {
                throw new RuntimeException("New passwords do not match.");
            }

            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        return userRepository.save(user);
    }
}
