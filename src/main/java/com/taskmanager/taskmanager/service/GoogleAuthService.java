package com.taskmanager.taskmanager.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;  // ← correct
import com.taskmanager.taskmanager.dto.AuthResponse;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.clientId}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthResponse loginWithGoogle(String idTokenString) {
        GoogleIdToken idToken = verify(idTokenString);

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        UserEntity user = userRepository
                .findByGoogleId(googleId)
                .orElseGet(() -> createNewUser(googleId, email, fullName, picture));

        user.setLastLoggedInAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);

        return new AuthResponse(accessToken, user.getFullName(), user.getEmail());
    }

    public GoogleIdToken verify(String idTokenString) {
        try {
            NetHttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory) // ← Builder(...)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Invalid ID token");
            }
            return idToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify ID token", e);
        }
    }

    private UserEntity createNewUser(String googleId, String email, String fullName, String picture) {
        UserEntity user = UserEntity.builder()
                .googleId(googleId)
                .email(email)
                .fullName(fullName)
                .avatarUrl(picture)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    public GoogleIdToken loginWithGoogle_verifyWrapper(String idTokenString) {
        return verify(idTokenString);
    }
}
