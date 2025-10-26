package com.aigo.repository;

import com.aigo.entity.CharacterEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CharacterRepositoryTest {

    @Autowired
    private CharacterRepository characterRepository;

    private CharacterEntity testCharacter;

    @BeforeEach
    void setUp() {
        testCharacter = new CharacterEntity();
        testCharacter.setName("Test Character");
        testCharacter.setDescription("A test character");
    }

    @Test
    void testSaveCharacter() {
        CharacterEntity saved = characterRepository.save(testCharacter);

        assertNotNull(saved.getId());
        assertEquals("Test Character", saved.getName());
    }

    @Test
    void testFindByName() {
        characterRepository.save(testCharacter);

        Optional<CharacterEntity> found = characterRepository.findByName("Test Character");

        assertTrue(found.isPresent());
        assertEquals("Test Character", found.get().getName());
    }

    @Test
    void testFindByNameNotFound() {
        Optional<CharacterEntity> found = characterRepository.findByName("Nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        characterRepository.save(testCharacter);

        CharacterEntity another = new CharacterEntity();
        another.setName("Another Test");
        characterRepository.save(another);

        List<CharacterEntity> results = characterRepository.findByNameContainingIgnoreCase("test");

        assertEquals(2, results.size());
    }

    @Test
    void testFindByNameContainingIgnoreCaseCaseSensitivity() {
        characterRepository.save(testCharacter);

        List<CharacterEntity> results = characterRepository.findByNameContainingIgnoreCase("TEST");

        assertEquals(1, results.size());
        assertEquals("Test Character", results.get(0).getName());
    }
}
