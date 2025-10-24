package com.aigo.controller;

import com.aigo.model.AnimeSegment;
import com.aigo.model.NovelParseRequest;
import com.aigo.service.NovelParseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/novel")
@CrossOrigin(origins = "*")
public class NovelParseController {
    
    @Autowired
    private NovelParseService novelParseService;
    
    @PostMapping("/parse")
    public ResponseEntity<?> parseNovel(@Valid @RequestBody NovelParseRequest request) {
        try {
            AnimeSegment segment = novelParseService.parseNovelText(
                request.getText(),
                request.getStyle(),
                request.getTargetAudience()
            );
            
            return ResponseEntity.ok(segment);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "解析失败");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Novel Parse Service");
        return response;
    }
}
