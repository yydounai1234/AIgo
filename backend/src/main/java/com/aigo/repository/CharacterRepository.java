package com.aigo.repository;

import com.aigo.entity.CharacterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<CharacterEntity, Long> {
    
    Optional<CharacterEntity> findByName(String name);
    
    List<CharacterEntity> findByNameContaining(String name);
    
    List<CharacterEntity> findByWorkId(String workId);
    
    Optional<CharacterEntity> findByWorkIdAndName(String workId, String name);
    
    List<CharacterEntity> findByWorkIdOrderByCreatedAtAsc(String workId);
}
