package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.*;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.EmailAuthService;
import com.taskmanager.taskmanager.service.GoogleAuthService;
import com.taskmanager.taskmanager.service.JwtService;
import com.taskmanager.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final EmailAuthService emailAuthService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/google")
    public AuthResponse loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        return googleAuthService.loginWithGoogle(request.idToken());
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return emailAuthService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return emailAuthService.login(request);
    }

    @GetMapping("/me")
    public UserProfileResponse me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatarUrl()
        );
    }

    // --------- UPDATE CURRENT USER (name/avatar/password) ---------

    @PutMapping("/me")
    public UserProfileResponse updateMe(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateUserRequest request
    ) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        UserEntity updated = userService.updateCurrentUser(userId, request);

        return new UserProfileResponse(
                updated.getId(),
                updated.getFullName(),
                updated.getEmail(),
                updated.getAvatarUrl()
        );
    }
}


