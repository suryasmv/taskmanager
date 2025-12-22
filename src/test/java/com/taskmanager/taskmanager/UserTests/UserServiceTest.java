package com.taskmanager.taskmanager.UserTests;

import com.taskmanager.taskmanager.dto.UpdateUserRequest;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.UserService;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    private UserEntity existingUserWithPassword() {
        UserEntity u = new UserEntity();
        u.setId(1L);
        u.setFullName("Old Name");
        u.setEmail("test@example.com");
        u.setAvatarUrl("old-avatar");
        u.setPasswordHash("ENC_OLD");
        return u;
    }

    // ---------- basic profile update (no password change) ----------

    @Test
    void updateCurrentUser_updatesNameAndAvatar_whenNoPasswordChange() {
        UserEntity existing = existingUserWithPassword();
        UpdateUserRequest req = new UpdateUserRequest(
                "New Name",
                "new-avatar",
                null,
                null,
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserEntity updated = userService.updateCurrentUser(1L, req);

        assertThat(updated.getFullName()).isEqualTo("New Name");
        assertThat(updated.getAvatarUrl()).isEqualTo("new-avatar");
        assertThat(updated.getPasswordHash()).isEqualTo("ENC_OLD"); // unchanged

        verify(userRepository).findById(1L);
        verify(userRepository).save(existing);
        verifyNoInteractions(passwordEncoder);
    }

    // ---------- user not found ----------

    @Test
    void updateCurrentUser_userNotFound_throws() {
        UpdateUserRequest req = new UpdateUserRequest(
                "Name", null, null, null, null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateCurrentUser(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    // ---------- password change: Google-only account ----------

    @Test
    void updateCurrentUser_passwordChangeForGoogleOnlyAccount_throws() {
        UserEntity existing = existingUserWithPassword();
        existing.setPasswordHash(null); // Google account

        UpdateUserRequest req = new UpdateUserRequest(
                null,
                null,
                "old",
                "newPass",
                "newPass"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.updateCurrentUser(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("created with Google");

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    // ---------- password change: current password missing ----------

    @Test
    void updateCurrentUser_newPasswordWithoutCurrentPassword_throws() {
        UserEntity existing = existingUserWithPassword();

        UpdateUserRequest req = new UpdateUserRequest(
                null,
                null,
                null,                 // currentPassword missing
                "newPass",
                "newPass"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.updateCurrentUser(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Current password is required");

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    // ---------- password change: current password incorrect ----------

    @Test
    void updateCurrentUser_incorrectCurrentPassword_throws() {
        UserEntity existing = existingUserWithPassword();

        UpdateUserRequest req = new UpdateUserRequest(
                null,
                null,
                "wrong",              // current
                "newPass",
                "newPass"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong", "ENC_OLD")).thenReturn(false);

        assertThatThrownBy(() -> userService.updateCurrentUser(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrong", "ENC_OLD");
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    // ---------- password change: new + confirm mismatch ----------

    @Test
    void updateCurrentUser_newPasswordsDoNotMatch_throws() {
        UserEntity existing = existingUserWithPassword();

        UpdateUserRequest req = new UpdateUserRequest(
                null,
                null,
                "oldPass",
                "newPass",
                "different"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("oldPass", "ENC_OLD")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateCurrentUser(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("New passwords do not match");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPass", "ENC_OLD");
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    // ---------- password change: full happy path ----------

    @Test
    void updateCurrentUser_passwordChangeSuccessful() {
        UserEntity existing = existingUserWithPassword();

        UpdateUserRequest req = new UpdateUserRequest(
                "New Name",
                "new-avatar",
                "oldPass",
                "newPass",
                "newPass"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("oldPass", "ENC_OLD")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("ENC_NEW");
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserEntity updated = userService.updateCurrentUser(1L, req);

        assertThat(updated.getFullName()).isEqualTo("New Name");
        assertThat(updated.getAvatarUrl()).isEqualTo("new-avatar");
        assertThat(updated.getPasswordHash()).isEqualTo("ENC_NEW");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPass", "ENC_OLD");
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(existing);
    }
}
