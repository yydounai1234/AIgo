package com.aigo.service;

import com.aigo.entity.SceneEntity;
import com.aigo.exception.BusinessException;
import com.aigo.repository.SceneRepository;
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
class SceneServiceTest {

    @Mock
    private SceneRepository sceneRepository;

    @InjectMocks
    private SceneService sceneService;

    private SceneEntity testScene;

    @BeforeEach
    void setUp() {
        testScene = new SceneEntity();
        testScene.setId(1L);
        testScene.setSceneNumber(1);
        testScene.setContent("Test scene content");
        testScene.setCharacter("Test Character");
    }

    @Test
    void testCreateScene() {
        when(sceneRepository.save(any(SceneEntity.class))).thenReturn(testScene);

        SceneEntity result = sceneService.createScene(testScene);

        assertNotNull(result);
        assertEquals(1, result.getSceneNumber());
        verify(sceneRepository).save(testScene);
    }

    @Test
    void testGetSceneById() {
        when(sceneRepository.findById(anyLong())).thenReturn(Optional.of(testScene));

        SceneEntity result = sceneService.getSceneById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(sceneRepository).findById(1L);
    }

    @Test
    void testGetSceneByIdNotFound() {
        when(sceneRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> sceneService.getSceneById(1L));
        verify(sceneRepository).findById(1L);
    }

    @Test
    void testGetSceneByNumber() {
        when(sceneRepository.findBySceneNumber(anyInt())).thenReturn(Optional.of(testScene));

        SceneEntity result = sceneService.getSceneByNumber(1);

        assertNotNull(result);
        assertEquals(1, result.getSceneNumber());
        verify(sceneRepository).findBySceneNumber(1);
    }

    @Test
    void testGetSceneByNumberNotFound() {
        when(sceneRepository.findBySceneNumber(anyInt())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> sceneService.getSceneByNumber(999));
        verify(sceneRepository).findBySceneNumber(999);
    }

    @Test
    void testGetAllScenes() {
        when(sceneRepository.findAll()).thenReturn(Arrays.asList(testScene));

        List<SceneEntity> results = sceneService.getAllScenes();

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(sceneRepository).findAll();
    }

    @Test
    void testGetScenesByCharacter() {
        when(sceneRepository.findByCharacter(anyString())).thenReturn(Arrays.asList(testScene));

        List<SceneEntity> results = sceneService.getScenesByCharacter("Test Character");

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(sceneRepository).findByCharacter("Test Character");
    }

    @Test
    void testGetScenesByRange() {
        when(sceneRepository.findBySceneNumberBetween(anyInt(), anyInt()))
                .thenReturn(Arrays.asList(testScene));

        List<SceneEntity> results = sceneService.getScenesByRange(1, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(sceneRepository).findBySceneNumberBetween(1, 10);
    }

    @Test
    void testUpdateScene() {
        SceneEntity updatedScene = new SceneEntity();
        updatedScene.setContent("Updated content");

        when(sceneRepository.findById(anyLong())).thenReturn(Optional.of(testScene));
        when(sceneRepository.save(any(SceneEntity.class))).thenReturn(testScene);

        SceneEntity result = sceneService.updateScene(1L, updatedScene);

        assertNotNull(result);
        verify(sceneRepository).findById(1L);
        verify(sceneRepository).save(testScene);
    }

    @Test
    void testDeleteScene() {
        when(sceneRepository.existsById(anyLong())).thenReturn(true);

        sceneService.deleteScene(1L);

        verify(sceneRepository).existsById(1L);
        verify(sceneRepository).deleteById(1L);
    }

    @Test
    void testDeleteSceneNotFound() {
        when(sceneRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(BusinessException.class, () -> sceneService.deleteScene(1L));
        verify(sceneRepository).existsById(1L);
        verify(sceneRepository, never()).deleteById(anyLong());
    }
}
