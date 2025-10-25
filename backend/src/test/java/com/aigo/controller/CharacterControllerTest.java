package com.aigo.controller;

import com.aigo.entity.CharacterEntity;
import com.aigo.service.CharacterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void testCreateCharacter() throws Exception {
        when(characterService.createCharacter(any(CharacterEntity.class)))
                .thenReturn(testCharacter);

        mockMvc.perform(post("/api/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCharacter)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Character"));
    }

    @Test
    void testGetCharacterById() throws Exception {
        when(characterService.getCharacterById(anyLong())).thenReturn(testCharacter);

        mockMvc.perform(get("/api/characters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetCharacterByName() throws Exception {
        when(characterService.getCharacterByName(anyString())).thenReturn(testCharacter);

        mockMvc.perform(get("/api/characters/name/Test Character"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Character"));
    }

    @Test
    void testGetAllCharacters() throws Exception {
        when(characterService.getAllCharacters()).thenReturn(Arrays.asList(testCharacter));

        mockMvc.perform(get("/api/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testSearchCharacters() throws Exception {
        when(characterService.searchCharactersByName(anyString()))
                .thenReturn(Arrays.asList(testCharacter));

        mockMvc.perform(get("/api/characters/search")
                        .param("name", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testUpdateCharacter() throws Exception {
        when(characterService.updateCharacter(anyLong(), any(CharacterEntity.class)))
                .thenReturn(testCharacter);

        mockMvc.perform(put("/api/characters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCharacter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteCharacter() throws Exception {
        mockMvc.perform(delete("/api/characters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
