package com.taskmanager.taskmanager.AuthTests;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.taskmanager.taskmanager.dto.AuthResponse;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.GoogleAuthService;
import com.taskmanager.taskmanager.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Spy
    @InjectMocks
    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setClientId() throws Exception {
        Field f = GoogleAuthService.class.getDeclaredField("googleClientId");
        f.setAccessible(true);
        f.set(googleAuthService, "test-client-id");
    }

    private GoogleIdToken.Payload payload(
            String googleId, String email, String fullName, String picture
    ) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject(googleId);
        payload.setEmail(email);
        payload.set("name", fullName);
        payload.set("picture", picture);
        return payload;
    }

    // ---------- existing user ----------

    @Test
    void loginWithGoogle_existingUser_updatesLastLoginAndReturnsToken() throws Exception {
        GoogleIdToken.Payload payload = payload(
                "google-123", "test@example.com", "Test User", "pic-url");
        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        // stub the private verify() call through the spy
        doReturn(token).when(googleAuthService).verify(anyString());

        UserEntity existing = new UserEntity();
        existing.setGoogleId("google-123");
        existing.setEmail("test@example.com");
        existing.setFullName("Test User");

        when(userRepository.findByGoogleId("google-123"))
                .thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(jwtService.generateToken(existing)).thenReturn("TOKEN");

        AuthResponse resp = googleAuthService.loginWithGoogle("ID_TOKEN_STRING");

        assertThat(resp.accessToken()).isEqualTo("TOKEN");
        assertThat(resp.email()).isEqualTo("test@example.com");
        assertThat(resp.fullName()).isEqualTo("Test User");
        assertThat(existing.getLastLoggedInAt()).isNotNull();

        verify(userRepository).findByGoogleId("google-123");
        verify(userRepository).save(existing);
        verify(jwtService).generateToken(existing);
    }

    // ---------- new user (save called twice) ----------

    @Test
    void loginWithGoogle_newUser_createsAndReturnsToken() throws Exception {
        GoogleIdToken.Payload payload = payload(
                "google-999", "new@example.com", "New User", "pic-url");
        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        doReturn(token).when(googleAuthService).verify(anyString());

        when(userRepository.findByGoogleId("google-999"))
                .thenReturn(Optional.empty());

        UserEntity saved = new UserEntity();
        saved.setId(10L);
        saved.setGoogleId("google-999");
        saved.setEmail("new@example.com");
        saved.setFullName("New User");

        when(userRepository.save(any(UserEntity.class))).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("TOKEN");

        AuthResponse resp = googleAuthService.loginWithGoogle("ID_TOKEN_STRING");

        assertThat(resp.accessToken()).isEqualTo("TOKEN");
        assertThat(resp.email()).isEqualTo("new@example.com");
        assertThat(resp.fullName()).isEqualTo("New User");

        verify(userRepository).findByGoogleId("google-999");
        verify(userRepository, times(2)).save(any(UserEntity.class)); // create + lastLogin update
        verify(jwtService).generateToken(saved);
    }

    // ---------- invalid token ----------

    @Test
    void loginWithGoogle_invalidToken_throws() throws Exception {
        doThrow(new RuntimeException("Failed to verify ID token"))
                .when(googleAuthService).verify(anyString());

        assertThatThrownBy(() -> googleAuthService.loginWithGoogle("BAD_TOKEN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to verify ID token");
    }
}
