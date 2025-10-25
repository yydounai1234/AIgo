package com.aigo.service;

import com.aigo.entity.CharacterEntity;
import com.aigo.exception.BusinessException;
import com.aigo.repository.CharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @InjectMocks
    private CharacterService characterService;

    private CharacterEntity testCharacter;

    @BeforeEach
    void setUp() {
        testCharacter = new CharacterEntity();
        testCharacter.setId(1L);
        testCharacter.setName("Test Character");
        testCharacter.setDescription("Test Description");
    }

    @Test
    void testCreateCharacter() {
        when(characterRepository.save(any(CharacterEntity.class))).thenReturn(testCharacter);

        CharacterEntity result = characterService.createCharacter(testCharacter);

        assertNotNull(result);
        assertEquals("Test Character", result.getName());
        verify(characterRepository).save(testCharacter);
    }

    @Test
    void testGetCharacterById() {
        when(characterRepository.findById(anyLong())).thenReturn(Optional.of(testCharacter));

        CharacterEntity result = characterService.getCharacterById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(characterRepository).findById(1L);
    }

    @Test
    void testGetCharacterByIdNotFound() {
        when(characterRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> characterService.getCharacterById(1L));
        verify(characterRepository).findById(1L);
    }

    @Test
    void testGetCharacterByName() {
        when(characterRepository.findByName(anyString())).thenReturn(Optional.of(testCharacter));

        CharacterEntity result = characterService.getCharacterByName("Test Character");

        assertNotNull(result);
        assertEquals("Test Character", result.getName());
        verify(characterRepository).findByName("Test Character");
    }

    @Test
    void testGetCharacterByNameNotFound() {
        when(characterRepository.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> 
            characterService.getCharacterByName("Unknown"));
        verify(characterRepository).findByName("Unknown");
    }

    @Test
    void testGetAllCharacters() {
        when(characterRepository.findAll()).thenReturn(Arrays.asList(testCharacter));

        List<CharacterEntity> results = characterService.getAllCharacters();

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(characterRepository).findAll();
    }

    @Test
    void testSearchCharactersByName() {
        when(characterRepository.findByNameContainingIgnoreCase(anyString()))
                .thenReturn(Arrays.asList(testCharacter));

        List<CharacterEntity> results = characterService.searchCharactersByName("test");

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(characterRepository).findByNameContainingIgnoreCase("test");
    }

    @Test
    void testUpdateCharacter() {
        CharacterEntity updatedChar = new CharacterEntity();
        updatedChar.setName("Updated Name");
        updatedChar.setDescription("Updated Description");

        when(characterRepository.findById(anyLong())).thenReturn(Optional.of(testCharacter));
        when(characterRepository.save(any(CharacterEntity.class))).thenReturn(testCharacter);

        CharacterEntity result = characterService.updateCharacter(1L, updatedChar);

        assertNotNull(result);
        verify(characterRepository).findById(1L);
        verify(characterRepository).save(testCharacter);
    }

    @Test
    void testDeleteCharacter() {
        when(characterRepository.existsById(anyLong())).thenReturn(true);

        characterService.deleteCharacter(1L);

        verify(characterRepository).existsById(1L);
        verify(characterRepository).deleteById(1L);
    }

    @Test
    void testDeleteCharacterNotFound() {
        when(characterRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(BusinessException.class, () -> characterService.deleteCharacter(1L));
        verify(characterRepository).existsById(1L);
        verify(characterRepository, never()).deleteById(anyLong());
    }
}
