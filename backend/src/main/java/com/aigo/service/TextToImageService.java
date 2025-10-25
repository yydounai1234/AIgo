package com.aigo.service;

import com.aigo.model.Scene;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TextToImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(TextToImageService.class);
    
    @Value("${qiniu.text2img.api.key}")
    private String apiKey;
    
    @Value("${qiniu.text2img.api.base.url:https://openai.qiniu.com/v1}")
    private String baseUrl;
    
    @Value("${qiniu.text2img.model.name:gemini-2.5-flash-image}")
    private String modelName;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> characterDescriptions = new HashMap<>();
    
    public TextToImageService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
    }
    
    public String generateImageForScene(Scene scene, Map<String, String> characterAppearances) {
        if ("demo-key".equals(apiKey)) {
            logger.info("[TextToImageService] Using demo mode for scene {}", scene.getSceneNumber());
            return createDemoImageUrl(scene);
        }
        
        try {
            String characterDesc = characterAppearances.getOrDefault(scene.getCharacter(), scene.getCharacter());
            characterDescriptions.putIfAbsent(scene.getCharacter(), characterDesc);
            
            String prompt = buildImagePrompt(scene, characterDesc);
            
            logger.info("[TextToImageService] Generating image for scene {}", scene.getSceneNumber());
            String imageData = callTextToImageApi(prompt);
            
            return imageData;
            
        } catch (Exception e) {
            logger.error("[TextToImageService] Failed to generate image for scene {}", scene.getSceneNumber(), e);
            throw new RuntimeException("图片生成失败: " + e.getMessage(), e);
        }
    }
    
    public List<String> generateImagesForScenes(List<Scene> scenes, Map<String, String> characterAppearances) {
        logger.info("[TextToImageService] Generating images for {} scenes", scenes.size());
        
        List<String> imageUrls = new ArrayList<>();
        
        for (Scene scene : scenes) {
            try {
                String imageUrl = generateImageForScene(scene, characterAppearances);
                imageUrls.add(imageUrl);
                
                Thread.sleep(500);
                
            } catch (Exception e) {
                logger.error("[TextToImageService] Failed for scene {}, using placeholder", 
                    scene.getSceneNumber(), e);
                String placeholder = createDemoImageUrl(scene);
                imageUrls.add(placeholder);
            }
        }
        
        logger.info("[TextToImageService] Completed, generated {} images", imageUrls.size());
        return imageUrls;
    }
    
    private String buildImagePrompt(Scene scene, String characterDescription) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("漫画风格插画。");
        
        prompt.append("角色: ").append(characterDescription).append("。");
        
        if (scene.getVisualDescription() != null && !scene.getVisualDescription().isEmpty()) {
            prompt.append("画面: ").append(scene.getVisualDescription()).append("。");
        }
        
        if (scene.getAction() != null && !scene.getAction().isEmpty()) {
            prompt.append("动作: ").append(scene.getAction()).append("。");
        }
        
        if (scene.getAtmosphere() != null && !scene.getAtmosphere().isEmpty()) {
            prompt.append("氛围: ").append(scene.getAtmosphere()).append("。");
        }
        
        prompt.append("保持角色外观一致性。高质量，细节丰富。");
        
        
        return prompt.toString();
    }
    
    private String callTextToImageApi(String prompt) throws Exception {
        String url = baseUrl + "/images/generations";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");
        requestBody.put("temperature", 0.3);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String.class
        );
        
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode dataArray = responseJson.get("data");
        
        if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
            JsonNode firstImage = dataArray.get(0);
            
            if (firstImage.has("b64_json")) {
                return "data:image/png;base64," + firstImage.get("b64_json").asText();
            } else if (firstImage.has("url")) {
                return firstImage.get("url").asText();
            }
        }
        
        throw new RuntimeException("API 返回的响应中没有图片数据");
    }
    
    private String createDemoImageUrl(Scene scene) {
        return "https://via.placeholder.com/1024x1024.png?text=Scene+" + scene.getSceneNumber() + ":" + scene.getCharacter();
    }
}
