package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.AuthResponse;
import com.taskmanager.taskmanager.dto.LoginRequest;
import com.taskmanager.taskmanager.dto.RegisterRequest;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        UserEntity user = UserEntity.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        return new AuthResponse(accessToken, user.getFullName(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.getPasswordHash() == null) {
            throw new RuntimeException("Account uses Google login only");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(user);
        return new AuthResponse(accessToken, user.getFullName(), user.getEmail());
    }
}

