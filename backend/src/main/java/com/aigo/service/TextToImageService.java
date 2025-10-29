package com.aigo.service;

import com.aigo.model.Scene;
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

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.Base64;

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
    private final Map<String, Map<String, Object>> characterEmbeddings = new HashMap<>();
    private final QiniuStorageService qiniuStorageService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    @Autowired
    public TextToImageService(QiniuStorageService qiniuStorageService) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
        this.qiniuStorageService = qiniuStorageService;
    }
    
    public String generateImageForScene(Scene scene, Map<String, String> characterAppearances) {\n        return generateImageForScene(scene, characterAppearances, null);\n    }\n    \n    public String generateImageForScene(Scene scene, Map<String, String> characterAppearances, \n                                       Map<String, Map<String, Object>> existingEmbeddings) {
        if ("demo-key".equals(apiKey)) {
            logger.info("[TextToImageService] Using demo mode for scene {}", scene.getSceneNumber());
            return createDemoImageUrl(scene);
        }
        
        try {
            String characterKey = scene.getCharacter();
            String characterDesc = characterAppearances.getOrDefault(characterKey, characterKey);
            characterDescriptions.putIfAbsent(characterKey, characterDesc);
            
            if (existingEmbeddings != null && existingEmbeddings.containsKey(characterKey)) {
                characterEmbeddings.put(characterKey, existingEmbeddings.get(characterKey));
                logger.info("[TextToImageService] Using existing character embedding for '{}'", characterKey);
            }
            
            String prompt = buildImagePrompt(scene, characterDesc);
            
            logger.info("[TextToImageService] Generating image for scene {}", scene.getSceneNumber());
            String base64ImageData = callTextToImageApi(prompt);
            
            String filePrefix = "scene_" + scene.getSceneNumber();
            String publicUrl = qiniuStorageService.uploadBase64Image(base64ImageData, filePrefix);
            
            if (!characterEmbeddings.containsKey(characterKey)) {
                Map<String, Object> embedding = extractCharacterEmbedding(characterDesc, characterKey);
                characterEmbeddings.put(characterKey, embedding);
                logger.info("[TextToImageService] Extracted and stored character embedding for '{}'", characterKey);
            }
            
            return publicUrl;
            
        } catch (Exception e) {
            logger.error("[TextToImageService] Failed to generate image for scene {}", scene.getSceneNumber(), e);
            throw new RuntimeException("图片生成失败: " + e.getMessage(), e);
        }
    }
    
    public List<String> generateImagesForScenes(List<Scene> scenes, Map<String, String> characterAppearances) {
        logger.info("[TextToImageService] Generating images for {} scenes in parallel", scenes.size());
        
        if (scenes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<CompletableFuture<ImageResult>> futures = scenes.stream()
            .map(scene -> CompletableFuture.supplyAsync(() -> {
                int maxRetries = 3;
                int retryDelay = 3000;
                Exception lastException = null;
                
                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                    try {
                        String imageUrl = generateImageForScene(scene, characterAppearances);
                        if (attempt > 1) {
                            logger.info("[TextToImageService] Scene {} succeeded on attempt {}", 
                                scene.getSceneNumber(), attempt);
                        }
                        return new ImageResult(scene.getSceneNumber(), imageUrl, null);
                    } catch (Exception e) {
                        lastException = e;
                        if (attempt < maxRetries) {
                            logger.warn("[TextToImageService] Scene {} failed on attempt {}/{}, retrying in {}ms", 
                                scene.getSceneNumber(), attempt, maxRetries, retryDelay);
                            try {
                                Thread.sleep(retryDelay);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                logger.error("[TextToImageService] Retry interrupted for scene {}", 
                                    scene.getSceneNumber());
                                break;
                            }
                        } else {
                            logger.error("[TextToImageService] Scene {} failed after {} attempts, using placeholder", 
                                scene.getSceneNumber(), maxRetries, e);
                        }
                    }
                }
                
                String placeholder = createDemoImageUrl(scene);
                return new ImageResult(scene.getSceneNumber(), placeholder, 
                    lastException != null ? lastException.getMessage() : "Unknown error");
            }, executorService))
            .collect(Collectors.toList());
        
        List<ImageResult> results = futures.stream()
            .map(CompletableFuture::join)
            .sorted(Comparator.comparingInt(ImageResult::getSceneNumber))
            .collect(Collectors.toList());
        
        List<String> imageUrls = results.stream()
            .map(ImageResult::getImageUrl)
            .collect(Collectors.toList());
        
        long failedCount = results.stream().filter(r -> r.getError() != null).count();
        logger.info("[TextToImageService] Completed, generated {} images ({} failed)", 
            imageUrls.size(), failedCount);
        
        return imageUrls;
    }
    
    private String buildImagePrompt(Scene scene, String characterDescription) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("动漫/漫画风格插画。");
        
        prompt.append("角色：").append(characterDescription)
            .append("。重要提示：保持此角色外观与之前场景一致 - ");
        
        String characterKey = scene.getCharacter();
        if (characterDescriptions.containsKey(characterKey)) {
            prompt.append("使用这个精确描述：").append(characterDescriptions.get(characterKey)).append("。");
        }
        
        if (characterEmbeddings.containsKey(characterKey)) {
            Map<String, Object> embedding = characterEmbeddings.get(characterKey);
            prompt.append("参考角色特征：");
            if (embedding.containsKey("visualStyle")) {
                prompt.append("视觉风格: ").append(embedding.get("visualStyle")).append(", ");
            }
            if (embedding.containsKey("keyFeatures")) {
                prompt.append("关键特征: ").append(embedding.get("keyFeatures")).append("。");
            }
            prompt.append("严格保持与首次生成图像的一致性。");
        }
        
        if (scene.getVisualDescription() != null && !scene.getVisualDescription().isEmpty()) {
            prompt.append("场景：").append(scene.getVisualDescription()).append("。");
        }
        
        if (scene.getAction() != null && !scene.getAction().isEmpty()) {
            prompt.append("动作：").append(scene.getAction()).append("。");
        }
        
        if (scene.getAtmosphere() != null && !scene.getAtmosphere().isEmpty()) {
            prompt.append("氛围：").append(scene.getAtmosphere()).append("。");
        }
        
        prompt.append("高质量、细节丰富、所有场景中保持一致的角色设计。");
        prompt.append("角色").append(characterKey).append("保持相同的面部、发型和服装。");
        
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
        return "http://via.placeholder.com/1024x1024.png?text=Scene+" + scene.getSceneNumber() + ":" + scene.getCharacter();
    }
    
    private Map<String, Object> extractCharacterEmbedding(String characterDescription, String characterName) {
        Map<String, Object> embedding = new HashMap<>();
        
        embedding.put("characterName", characterName);
        embedding.put("timestamp", System.currentTimeMillis());
        embedding.put("description", characterDescription);
        
        embedding.put("visualStyle", "动漫风格");
        
        List<String> keyFeatures = new ArrayList<>();
        if (characterDescription.contains("黑发") || characterDescription.contains("黑色头发")) {
            keyFeatures.add("黑发");
        }
        if (characterDescription.contains("棕发") || characterDescription.contains("棕色头发")) {
            keyFeatures.add("棕发");
        }
        if (characterDescription.contains("长发")) {
            keyFeatures.add("长发");
        }
        if (characterDescription.contains("短发")) {
            keyFeatures.add("短发");
        }
        if (characterDescription.contains("眼睛")) {
            keyFeatures.add("特征性眼睛");
        }
        if (characterDescription.contains("高挑") || characterDescription.contains("高")) {
            keyFeatures.add("高挑身材");
        }
        if (characterDescription.contains("娇小") || characterDescription.contains("矮")) {
            keyFeatures.add("娇小身材");
        }
        
        embedding.put("keyFeatures", String.join(", ", keyFeatures));
        embedding.put("consistencyPrompt", "保持" + characterName + "的" + String.join("、", keyFeatures) + "特征不变");
        
        return embedding;
    }
    
    public Map<String, Map<String, Object>> getCharacterEmbeddings() {
        return new HashMap<>(characterEmbeddings);
    }
    
    public void setCharacterEmbedding(String characterName, Map<String, Object> embedding) {
        if (characterName != null && embedding != null) {
            characterEmbeddings.put(characterName, embedding);
            logger.info("[TextToImageService] Set character embedding for '{}'", characterName);
        }
    }
    private static class ImageResult {
        private final int sceneNumber;
        private final String imageUrl;
        private final String error;
        
        public ImageResult(int sceneNumber, String imageUrl, String error) {
            this.sceneNumber = sceneNumber;
            this.imageUrl = imageUrl;
            this.error = error;
        }
        
        public int getSceneNumber() { return sceneNumber; }
        public String getImageUrl() { return imageUrl; }
        public String getError() { return error; }
    }
}
