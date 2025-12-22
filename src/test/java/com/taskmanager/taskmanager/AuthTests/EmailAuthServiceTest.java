package com.taskmanager.taskmanager.AuthTests;

import com.taskmanager.taskmanager.dto.AuthResponse;
import com.taskmanager.taskmanager.dto.LoginRequest;
import com.taskmanager.taskmanager.dto.RegisterRequest;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.EmailAuthService;
import com.taskmanager.taskmanager.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private EmailAuthService emailAuthService;

    @BeforeEach
    void setUp() {
        emailAuthService = new EmailAuthService(userRepository, passwordEncoder, jwtService);
    }

    // ---------- register ----------

    @Test
    void register_successful_whenPasswordsMatch_andEmailNotRegistered() {
        RegisterRequest req = new RegisterRequest("Test User", "test@example.com", "pass", "pass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("ENC");
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("TOKEN");

        AuthResponse resp = emailAuthService.register(req);

        assertThat(resp.accessToken()).isEqualTo("TOKEN");
        assertThat(resp.email()).isEqualTo("test@example.com");
        assertThat(resp.fullName()).isEqualTo("Test User");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("ENC");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("pass");
        verify(jwtService).generateToken(any(UserEntity.class));
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_passwordsDoNotMatch_throws() {
        RegisterRequest req = new RegisterRequest("Test User", "test@example.com", "a", "b");

        assertThatThrownBy(() -> emailAuthService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Passwords do not match");

        verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_emailAlreadyRegistered_throws() {
        RegisterRequest req = new RegisterRequest("Test User", "test@example.com", "pass", "pass");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new UserEntity()));

        assertThatThrownBy(() -> emailAuthService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository).findByEmail("test@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    // ---------- login ----------

    @Test
    void login_successful_withCorrectPassword() {
        LoginRequest req = new LoginRequest("test@example.com", "pass");

        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPasswordHash("ENC");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "ENC")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("TOKEN");

        AuthResponse resp = emailAuthService.login(req);

        assertThat(resp.accessToken()).isEqualTo("TOKEN");
        assertThat(resp.email()).isEqualTo("test@example.com");
        assertThat(resp.fullName()).isEqualTo("Test User");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("pass", "ENC");
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_userNotFound_throwsInvalidEmailOrPassword() {
        LoginRequest req = new LoginRequest("missing@example.com", "pass");

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailAuthService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail("missing@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_googleOnlyAccount_throws() {
        LoginRequest req = new LoginRequest("test@example.com", "pass");

        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPasswordHash(null); // Google only

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> emailAuthService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Google login only");

        verify(userRepository).findByEmail("test@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_wrongPassword_throwsInvalidEmailOrPassword() {
        LoginRequest req = new LoginRequest("test@example.com", "wrong");

        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPasswordHash("ENC");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "ENC")).thenReturn(false);

        assertThatThrownBy(() -> emailAuthService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrong", "ENC");
        verifyNoMoreInteractions(userRepository, passwordEncoder);
        verifyNoInteractions(jwtService);
    }
}
