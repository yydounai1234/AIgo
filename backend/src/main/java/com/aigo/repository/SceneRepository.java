package com.aigo.repository;

import com.aigo.entity.SceneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SceneRepository extends JpaRepository<SceneEntity, Long> {
    
    Optional<SceneEntity> findBySceneNumber(Integer sceneNumber);
    
    List<SceneEntity> findByCharacter(String character);
    
    List<SceneEntity> findBySceneNumberBetween(Integer start, Integer end);
}
