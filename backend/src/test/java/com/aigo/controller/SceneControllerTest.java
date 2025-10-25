package com.aigo.controller;

import com.aigo.entity.SceneEntity;
import com.aigo.service.SceneService;
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
class SceneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SceneService sceneService;

    private SceneEntity testScene;

    @BeforeEach
    void setUp() {
        testScene = new SceneEntity();
        testScene.setId(1L);
        testScene.setSceneNumber(1);
        testScene.setContent("Test content");
        testScene.setCharacter("Test Character");
    }

    @Test
    void testCreateScene() throws Exception {
        when(sceneService.createScene(any(SceneEntity.class))).thenReturn(testScene);

        mockMvc.perform(post("/api/scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testScene)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sceneNumber").value(1));
    }

    @Test
    void testGetSceneById() throws Exception {
        when(sceneService.getSceneById(anyLong())).thenReturn(testScene);

        mockMvc.perform(get("/api/scenes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetSceneByNumber() throws Exception {
        when(sceneService.getSceneByNumber(anyInt())).thenReturn(testScene);

        mockMvc.perform(get("/api/scenes/number/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sceneNumber").value(1));
    }

    @Test
    void testGetAllScenes() throws Exception {
        when(sceneService.getAllScenes()).thenReturn(Arrays.asList(testScene));

        mockMvc.perform(get("/api/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetScenesByCharacter() throws Exception {
        when(sceneService.getScenesByCharacter(anyString()))
                .thenReturn(Arrays.asList(testScene));

        mockMvc.perform(get("/api/scenes/character/Test Character"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetScenesByRange() throws Exception {
        when(sceneService.getScenesByRange(anyInt(), anyInt()))
                .thenReturn(Arrays.asList(testScene));

        mockMvc.perform(get("/api/scenes/range")
                        .param("start", "1")
                        .param("end", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testUpdateScene() throws Exception {
        when(sceneService.updateScene(anyLong(), any(SceneEntity.class)))
                .thenReturn(testScene);

        mockMvc.perform(put("/api/scenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testScene)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteScene() throws Exception {
        mockMvc.perform(delete("/api/scenes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
