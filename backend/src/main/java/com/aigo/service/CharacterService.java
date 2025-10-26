package com.aigo.service;

import com.aigo.entity.CharacterEntity;
import com.aigo.exception.BusinessException;
import com.aigo.dto.ErrorCode;
import com.aigo.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CharacterService {
    
    private final CharacterRepository characterRepository;
    
    @Transactional
    public CharacterEntity createCharacter(CharacterEntity character) {
        if (character.getWorkId() != null) {
            Optional<CharacterEntity> existing = characterRepository.findByWorkIdAndName(
                character.getWorkId(), character.getName());
            if (existing.isPresent()) {
                return existing.get();
            }
        } else if (characterRepository.findByName(character.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色名称已存在");
        }
        return characterRepository.save(character);
    }
    
    @Transactional
    public CharacterEntity createOrUpdateWorkCharacter(String workId, String characterName, 
                                                       String description, String appearance, 
                                                       String personality, String gender, 
                                                       Boolean isProtagonist) {
        Optional<CharacterEntity> existing = characterRepository.findByWorkIdAndName(workId, characterName);
        
        CharacterEntity character;
        if (existing.isPresent()) {
            character = existing.get();
            if (description != null && !description.isEmpty()) {
                character.setDescription(description);
            }
            if (appearance != null && !appearance.isEmpty()) {
                character.setAppearance(appearance);
            }
            if (personality != null && !personality.isEmpty()) {
                character.setPersonality(personality);
            }
            if (gender != null && !gender.isEmpty()) {
                character.setGender(gender);
            }
            if (isProtagonist != null) {
                character.setIsProtagonist(isProtagonist);
            }
        } else {
            character = new CharacterEntity();
            character.setWorkId(workId);
            character.setName(characterName);
            character.setDescription(description);
            character.setAppearance(appearance);
            character.setPersonality(personality);
            character.setGender(gender);
            character.setIsProtagonist(isProtagonist != null ? isProtagonist : false);
        }
        
        return characterRepository.save(character);
    }
    
    @Transactional(readOnly = true)
    public CharacterEntity getCharacterById(Long id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "角色不存在"));
    }
    
    @Transactional(readOnly = true)
    public CharacterEntity getCharacterByName(String name) {
        return characterRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "角色不存在"));
    }
    
    @Transactional(readOnly = true)
    public List<CharacterEntity> getAllCharacters() {
        return characterRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<CharacterEntity> searchCharactersByName(String name) {
        return characterRepository.findByNameContaining(name);
    }
    
    @Transactional
    public CharacterEntity updateCharacter(Long id, CharacterEntity character) {
        CharacterEntity existingCharacter = getCharacterById(id);
        if (character.getName() != null) {
            existingCharacter.setName(character.getName());
        }
        if (character.getDescription() != null) {
            existingCharacter.setDescription(character.getDescription());
        }
        if (character.getAppearance() != null) {
            existingCharacter.setAppearance(character.getAppearance());
        }
        if (character.getPersonality() != null) {
            existingCharacter.setPersonality(character.getPersonality());
        }
        return characterRepository.save(existingCharacter);
    }
    
    @Transactional
    public void deleteCharacter(Long id) {
        if (!characterRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        characterRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<CharacterEntity> getCharactersByWorkId(String workId) {
        return characterRepository.findByWorkIdOrderByCreatedAtAsc(workId);
    }
    
    @Transactional(readOnly = true)
    public Optional<CharacterEntity> getWorkCharacterByName(String workId, String name) {
        return characterRepository.findByWorkIdAndName(workId, name);
    }
}
