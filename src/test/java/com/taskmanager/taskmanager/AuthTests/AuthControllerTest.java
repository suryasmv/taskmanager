package com.taskmanager.taskmanager.AuthTests;

import com.taskmanager.taskmanager.controller.AuthController;
import com.taskmanager.taskmanager.dto.*;
import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.service.EmailAuthService;
import com.taskmanager.taskmanager.service.GoogleAuthService;
import com.taskmanager.taskmanager.service.JwtService;
import com.taskmanager.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GoogleAuthService googleAuthService;

    @MockitoBean
    private EmailAuthService emailAuthService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    @Test
    void loginWithGoogle_returnsAuthResponse() throws Exception {
        GoogleLoginRequest req = new GoogleLoginRequest("ID_TOKEN");
        AuthResponse resp = new AuthResponse("ACCESS", "User", "user@example.com");

        when(googleAuthService.loginWithGoogle("ID_TOKEN")).thenReturn(resp);

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("ACCESS")))
                .andExpect(jsonPath("$.fullName", is("User")))
                .andExpect(jsonPath("$.email", is("user@example.com")));

        verify(googleAuthService).loginWithGoogle("ID_TOKEN");
    }

    @Test
    void register_usesEmailAuthService() throws Exception {
        RegisterRequest req = new RegisterRequest("User", "user@example.com", "pw", "pw");
        AuthResponse resp = new AuthResponse("ACCESS", "User", "user@example.com");

        when(emailAuthService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("user@example.com")));

        verify(emailAuthService).register(any(RegisterRequest.class));
    }

    @Test
    void login_usesEmailAuthService() throws Exception {
        LoginRequest req = new LoginRequest("user@example.com", "pw");
        AuthResponse resp = new AuthResponse("ACCESS", "User", "user@example.com");

        when(emailAuthService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("ACCESS")));

        verify(emailAuthService).login(any(LoginRequest.class));
    }

    @Test
    void me_returnsUserProfile() throws Exception {
        String token = "JWT";
        Long userId = 5L;

        when(jwtService.extractUserId(token)).thenReturn(userId);

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setFullName("User");
        user.setEmail("user@example.com");
        user.setAvatarUrl("pic");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.fullName", is("User")))
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.avatarUrl", is("pic")));

        // filter + controller both use these
        verify(jwtService, times(2)).extractUserId(token);
        verify(userRepository, times(2)).findById(userId);
    }

    @Test
    void updateMe_callsUserServiceAndReturnsProfile() throws Exception {
        String token = "JWT";
        Long userId = 5L;
        when(jwtService.extractUserId(token)).thenReturn(userId);

        UpdateUserRequest req = new UpdateUserRequest("New Name", "pic2", "oldPw", "newPw", "newPw");

        UserEntity updated = new UserEntity();
        updated.setId(userId);
        updated.setFullName("New Name");
        updated.setEmail("user@example.com");
        updated.setAvatarUrl("pic2");

        when(userService.updateCurrentUser(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.fullName", is("New Name")))
                .andExpect(jsonPath("$.avatarUrl", is("pic2")));

        verify(userService).updateCurrentUser(eq(userId), any(UpdateUserRequest.class));
    }
}
