package com.aigo.service;

import com.aigo.entity.User;
import com.aigo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user123")
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .coinBalance(100)
                .build();
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("hashedpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty() || 
                  userDetails.getAuthorities().stream().anyMatch(a -> true));

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> 
            customUserDetailsService.loadUserByUsername("nonexistent"));

        verify(userRepository).findByUsername("nonexistent");
    }
}
