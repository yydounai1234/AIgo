package com.aigo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class VideoGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoGenerationService.class);
    
    @Value("${qiniu.video.api.key:demo-key}")
    private String apiKey;
    
    @Value("${qiniu.video.api.base.url:https://api.qnaigc.com}")
    private String baseUrl;
    
    @Value("${qiniu.video.model.name:veo-3.0-fast-generate-001}")
    private String modelName;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final QiniuStorageService qiniuStorageService;
    
    @Autowired
    public VideoGenerationService(QiniuStorageService qiniuStorageService) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(120000);
        this.restTemplate = new RestTemplate(factory);
        this.qiniuStorageService = qiniuStorageService;
    }
    
    public String generateVideoFromImageAndPrompt(String baseImageUrl, String prompt) {
        if ("demo-key".equals(apiKey)) {
            logger.info("[VideoGenerationService] Using demo mode - returning placeholder video URL");
            return "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4";
        }
        
        try {
            logger.info("[VideoGenerationService] Starting video generation with prompt: {}", 
                prompt.substring(0, Math.min(100, prompt.length())));
            
            String taskId = createVideoGenerationTask(baseImageUrl, prompt);
            logger.info("[VideoGenerationService] Video generation task created with ID: {}", taskId);
            
            String videoUrl = pollVideoGenerationStatus(taskId);
            logger.info("[VideoGenerationService] Video generation completed: {}", videoUrl);
            
            return videoUrl;
            
        } catch (Exception e) {
            logger.error("[VideoGenerationService] Failed to generate video", e);
            throw new RuntimeException("视频生成失败: " + e.getMessage(), e);
        }
    }
    
    private String createVideoGenerationTask(String imageUrl, String prompt) throws Exception {
        String url = baseUrl + "/videos/generations";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("uri", imageUrl);
        imageData.put("mimeType", "image/jpeg");
        
        Map<String, Object> instance = new HashMap<>();
        instance.put("prompt", prompt);
        instance.put("image", imageData);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("durationSeconds", 8);
        parameters.put("sampleCount", 1);
        parameters.put("aspectRatio", "16:9");
        parameters.put("generateAudio", true);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("instances", Collections.singletonList(instance));
        requestBody.put("parameters", parameters);
        requestBody.put("model", modelName);
        
        String requestJson = objectMapper.writeValueAsString(requestBody);
        logger.debug("[VideoGenerationService] Request body: {}", requestJson);
        
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        String taskId = jsonResponse.get("id").asText();
        
        return taskId;
    }
    
    private String pollVideoGenerationStatus(String taskId) throws Exception {
        String url = baseUrl + "/videos/generations/" + taskId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        int maxAttempts = 60;
        int attemptCount = 0;
        
        while (attemptCount < maxAttempts) {
            attemptCount++;
            
            try {
                Thread.sleep(5000);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                String status = jsonResponse.get("status").asText();
                logger.info("[VideoGenerationService] Task {} status: {} (attempt {}/{}), jsonResponse: {}", 
                    taskId, status, attemptCount, maxAttempts, jsonResponse);
                
                if ("Completed".equals(status)) {
                    JsonNode videos = jsonResponse.at("/data/videos");
                    if (videos.isArray() && videos.size() > 0) {
                        String url = videos.get(0).get("url").asText();
                        return url;
                    } else {
                        throw new RuntimeException("No video generated in response");
                    }
                } else if ("Failed".equals(status)) {
                    throw new RuntimeException("Video generation failed with status: " + status);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Video generation interrupted", e);
            }
        }
        
        throw new RuntimeException("Video generation timeout after " + maxAttempts + " attempts");
    }
    
    public String generateVideoFromScenes(List<String> scenePrompts, String baseImageUrl) {
        if (scenePrompts == null || scenePrompts.isEmpty()) {
            throw new IllegalArgumentException("Scene prompts cannot be empty");
        }
        
        StringBuilder combinedPrompt = new StringBuilder();
        combinedPrompt.append("Create an anime-style video with the following scenes:\n");
        
        for (int i = 0; i < scenePrompts.size(); i++) {
            combinedPrompt.append(i + 1).append(". ").append(scenePrompts.get(i)).append("\n");
        }
        
        return generateVideoFromImageAndPrompt(baseImageUrl, combinedPrompt.toString());
    }
}
