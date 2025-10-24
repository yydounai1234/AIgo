package com.aigo.service;

import com.aigo.entity.SceneEntity;
import com.aigo.exception.BusinessException;
import com.aigo.dto.ErrorCode;
import com.aigo.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SceneService {
    
    private final SceneRepository sceneRepository;
    
    @Transactional
    public SceneEntity createScene(SceneEntity scene) {
        return sceneRepository.save(scene);
    }
    
    @Transactional(readOnly = true)
    public SceneEntity getSceneById(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场景不存在"));
    }
    
    @Transactional(readOnly = true)
    public SceneEntity getSceneByNumber(Integer sceneNumber) {
        return sceneRepository.findBySceneNumber(sceneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "场景不存在"));
    }
    
    @Transactional(readOnly = true)
    public List<SceneEntity> getAllScenes() {
        return sceneRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<SceneEntity> getScenesByCharacter(String character) {
        return sceneRepository.findByCharacter(character);
    }
    
    @Transactional(readOnly = true)
    public List<SceneEntity> getScenesByRange(Integer start, Integer end) {
        return sceneRepository.findBySceneNumberBetween(start, end);
    }
    
    @Transactional
    public SceneEntity updateScene(Long id, SceneEntity scene) {
        SceneEntity existingScene = getSceneById(id);
        if (scene.getSceneNumber() != null) {
            existingScene.setSceneNumber(scene.getSceneNumber());
        }
        if (scene.getCharacter() != null) {
            existingScene.setCharacter(scene.getCharacter());
        }
        if (scene.getDialogue() != null) {
            existingScene.setDialogue(scene.getDialogue());
        }
        if (scene.getVisualDescription() != null) {
            existingScene.setVisualDescription(scene.getVisualDescription());
        }
        if (scene.getAtmosphere() != null) {
            existingScene.setAtmosphere(scene.getAtmosphere());
        }
        if (scene.getAction() != null) {
            existingScene.setAction(scene.getAction());
        }
        return sceneRepository.save(existingScene);
    }
    
    @Transactional
    public void deleteScene(Long id) {
        if (!sceneRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "场景不存在");
        }
        sceneRepository.deleteById(id);
    }
}
