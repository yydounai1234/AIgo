package com.aigo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserBuilder() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hash")
                .coinBalance(100)
                .build();

        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals(100, user.getCoinBalance());
    }

    @Test
    void testUserSettersAndGetters() {
        User user = new User();
        user.setId("123");
        user.setUsername("user");
        user.setEmail("email@test.com");
        user.setPasswordHash("hash");
        user.setCoinBalance(50);
        user.setCreatedAt(LocalDateTime.now());

        assertEquals("123", user.getId());
        assertEquals("user", user.getUsername());
        assertEquals("email@test.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals(50, user.getCoinBalance());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testDefaultCoinBalance() {
        User user = User.builder()
                .username("test")
                .email("test@test.com")
                .passwordHash("hash")
                .build();

        assertNotNull(user);
    }
}
