package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import com.aigo.entity.CharacterEntity;
import com.aigo.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {
    
    private final CharacterService characterService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<CharacterEntity>> createCharacter(@RequestBody CharacterEntity character) {
        CharacterEntity created = characterService.createCharacter(character);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CharacterEntity>> getCharacterById(@PathVariable Long id) {
        CharacterEntity character = characterService.getCharacterById(id);
        return ResponseEntity.ok(ApiResponse.success(character));
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<CharacterEntity>> getCharacterByName(@PathVariable String name) {
        CharacterEntity character = characterService.getCharacterByName(name);
        return ResponseEntity.ok(ApiResponse.success(character));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CharacterEntity>>> getAllCharacters() {
        List<CharacterEntity> characters = characterService.getAllCharacters();
        return ResponseEntity.ok(ApiResponse.success(characters));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CharacterEntity>>> searchCharacters(@RequestParam String name) {
        List<CharacterEntity> characters = characterService.searchCharactersByName(name);
        return ResponseEntity.ok(ApiResponse.success(characters));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CharacterEntity>> updateCharacter(
            @PathVariable Long id,
            @RequestBody CharacterEntity character) {
        CharacterEntity updated = characterService.updateCharacter(id, character);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/work/{workId}")
    public ResponseEntity<ApiResponse<List<CharacterEntity>>> getCharactersByWork(@PathVariable String workId) {
        List<CharacterEntity> characters = characterService.getCharactersByWorkId(workId);
        return ResponseEntity.ok(ApiResponse.success(characters));
    }
}
