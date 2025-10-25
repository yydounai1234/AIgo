package com.aigo.repository;

import com.aigo.entity.SceneEntity;
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
class SceneRepositoryTest {

    @Autowired
    private SceneRepository sceneRepository;

    private SceneEntity testScene;

    @BeforeEach
    void setUp() {
        testScene = new SceneEntity();
        testScene.setSceneNumber(1);
        testScene.setContent("Test scene content");
        testScene.setCharacter("Hero");
    }

    @Test
    void testSaveScene() {
        SceneEntity saved = sceneRepository.save(testScene);

        assertNotNull(saved.getId());
        assertEquals(1, saved.getSceneNumber());
    }

    @Test
    void testFindBySceneNumber() {
        sceneRepository.save(testScene);

        Optional<SceneEntity> found = sceneRepository.findBySceneNumber(1);

        assertTrue(found.isPresent());
        assertEquals(1, found.get().getSceneNumber());
    }

    @Test
    void testFindBySceneNumberNotFound() {
        Optional<SceneEntity> found = sceneRepository.findBySceneNumber(999);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByCharacter() {
        sceneRepository.save(testScene);

        SceneEntity another = new SceneEntity();
        another.setSceneNumber(2);
        another.setContent("Another scene");
        another.setCharacter("Hero");
        sceneRepository.save(another);

        List<SceneEntity> scenes = sceneRepository.findByCharacter("Hero");

        assertEquals(2, scenes.size());
    }

    @Test
    void testFindBySceneNumberBetween() {
        sceneRepository.save(testScene);

        SceneEntity scene2 = new SceneEntity();
        scene2.setSceneNumber(5);
        scene2.setContent("Scene 5");
        scene2.setCharacter("Villain");
        sceneRepository.save(scene2);

        SceneEntity scene3 = new SceneEntity();
        scene3.setSceneNumber(10);
        scene3.setContent("Scene 10");
        scene3.setCharacter("Hero");
        sceneRepository.save(scene3);

        List<SceneEntity> scenes = sceneRepository.findBySceneNumberBetween(1, 6);

        assertEquals(2, scenes.size());
    }
}
