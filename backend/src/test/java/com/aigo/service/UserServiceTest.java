package com.aigo.service;

import com.aigo.dto.user.BalanceResponse;
import com.aigo.entity.User;
import com.aigo.exception.BusinessException;
import com.aigo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .coinBalance(150)
                .build();
    }

    @Test
    void testGetBalance() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));

        BalanceResponse response = userService.getBalance(userId);

        assertNotNull(response);
        assertEquals(150, response.getBalance());

        verify(userRepository).findById(userId);
    }

    @Test
    void testGetBalanceUserNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userService.getBalance(userId));

        verify(userRepository).findById(userId);
    }
}
