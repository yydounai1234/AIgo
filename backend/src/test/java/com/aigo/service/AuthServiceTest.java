package com.aigo.service;

import com.aigo.dto.auth.AuthResponse;
import com.aigo.dto.auth.LoginRequest;
import com.aigo.dto.auth.RegisterRequest;
import com.aigo.entity.User;
import com.aigo.exception.BusinessException;
import com.aigo.repository.UserRepository;
import com.aigo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id("user123")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .coinBalance(100)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("test-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals(100, response.getUser().getCoinBalance());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(eq("testuser"), anyMap());
    }

    @Test
    void testRegisterWithExistingUsername() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterWithExistingEmail() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("test-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "hashedpassword");
        verify(jwtUtil).generateToken(eq("testuser"), anyMap());
    }

    @Test
    void testLoginWithInvalidUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLoginWithInvalidPassword() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "hashedpassword");
        verify(jwtUtil, never()).generateToken(anyString(), anyMap());
    }
}
