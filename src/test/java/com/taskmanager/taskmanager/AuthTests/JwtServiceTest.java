package com.taskmanager.taskmanager.AuthTests;

import com.taskmanager.taskmanager.entity.UserEntity;
import com.taskmanager.taskmanager.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // set @Value fields manually
        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, "super-secret-key-super-secret-key-123456"); // >= 32 bytes

        Field expField = JwtService.class.getDeclaredField("expirationMinutes");
        expField.setAccessible(true);
        expField.set(jwtService, 60L);
    }

    @Test
    void generateToken_and_extractUserId() {
        UserEntity user = new UserEntity();
        user.setId(42L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        String token = jwtService.generateToken(user);

        Long extractedId = jwtService.extractUserId(token);
        assertThat(extractedId).isEqualTo(42L);

        Jws<Claims> parsed = jwtService.parseToken(token);
        assertThat(parsed.getBody().get("email")).isEqualTo("test@example.com");
        assertThat(parsed.getBody().get("fullName")).isEqualTo("Test User");
    }
}
