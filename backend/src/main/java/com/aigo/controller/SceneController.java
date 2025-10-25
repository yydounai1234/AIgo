package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import com.aigo.entity.SceneEntity;
import com.aigo.service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenes")
@RequiredArgsConstructor
public class SceneController {
    
    private final SceneService sceneService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<SceneEntity>> createScene(@RequestBody SceneEntity scene) {
        SceneEntity created = sceneService.createScene(scene);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SceneEntity>> getSceneById(@PathVariable Long id) {
        SceneEntity scene = sceneService.getSceneById(id);
        return ResponseEntity.ok(ApiResponse.success(scene));
    }
    
    @GetMapping("/number/{sceneNumber}")
    public ResponseEntity<ApiResponse<SceneEntity>> getSceneByNumber(@PathVariable Integer sceneNumber) {
        SceneEntity scene = sceneService.getSceneByNumber(sceneNumber);
        return ResponseEntity.ok(ApiResponse.success(scene));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SceneEntity>>> getAllScenes() {
        List<SceneEntity> scenes = sceneService.getAllScenes();
        return ResponseEntity.ok(ApiResponse.success(scenes));
    }
    
    @GetMapping("/character/{character}")
    public ResponseEntity<ApiResponse<List<SceneEntity>>> getScenesByCharacter(@PathVariable String character) {
        List<SceneEntity> scenes = sceneService.getScenesByCharacter(character);
        return ResponseEntity.ok(ApiResponse.success(scenes));
    }
    
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<SceneEntity>>> getScenesByRange(
            @RequestParam Integer start,
            @RequestParam Integer end) {
        List<SceneEntity> scenes = sceneService.getScenesByRange(start, end);
        return ResponseEntity.ok(ApiResponse.success(scenes));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SceneEntity>> updateScene(
            @PathVariable Long id,
            @RequestBody SceneEntity scene) {
        SceneEntity updated = sceneService.updateScene(id, scene);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScene(@PathVariable Long id) {
        sceneService.deleteScene(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
