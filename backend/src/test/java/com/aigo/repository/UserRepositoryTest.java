package com.aigo.repository;

import com.aigo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .coinBalance(100)
                .build();
    }

    @Test
    void testSaveUser() {
        User saved = userRepository.save(testUser);

        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
        assertEquals("test@example.com", saved.getEmail());
        assertEquals(100, saved.getCoinBalance());
    }

    @Test
    void testFindByUsername() {
        userRepository.save(testUser);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByUsernameNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUsername() {
        userRepository.save(testUser);

        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        userRepository.save(testUser);

        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testUpdateUser() {
        User saved = userRepository.save(testUser);
        saved.setCoinBalance(200);

        User updated = userRepository.save(saved);

        assertEquals(200, updated.getCoinBalance());
    }

    @Test
    void testDeleteUser() {
        User saved = userRepository.save(testUser);
        String userId = saved.getId();

        userRepository.deleteById(userId);

        assertFalse(userRepository.findById(userId).isPresent());
    }
}
