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
    private final Map<String, String> sceneContexts = new HashMap<>();
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
    
    public String generateImageForScene(Scene scene, Map<String, String> characterAppearances) {
        return generateImageForScene(scene, characterAppearances, null);
    }
    
    public String generateImageForScene(Scene scene, Map<String, String> characterAppearances, 
                                       Map<String, Map<String, Object>> existingEmbeddings) {
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
        
        String characterKey = scene.getCharacter();
        
        String sceneContextKey = extractSceneContextKey(scene);
        if (sceneContexts.containsKey(sceneContextKey)) {
            prompt.append("场景延续：").append(sceneContexts.get(sceneContextKey)).append("。");
        }
        
        prompt.append("角色：").append(characterDescription)
            .append("。");
        
        if (characterEmbeddings.containsKey(characterKey)) {
            Map<String, Object> embedding = characterEmbeddings.get(characterKey);
            
            if (embedding.containsKey("strictFeatures")) {
                prompt.append("【关键约束 - 必须遵守】: ").append(embedding.get("strictFeatures")).append("。");
            }
            
            if (embedding.containsKey("consistencyPrompt")) {
                prompt.append(embedding.get("consistencyPrompt")).append("。");
            }
            
            prompt.append("注意：角色的发色、发型、眼睛特征在所有画面中必须完全相同，不允许有任何变化。");
        }
        
        if (scene.getVisualDescription() != null && !scene.getVisualDescription().isEmpty()) {
            prompt.append("场景：").append(scene.getVisualDescription()).append("。");
            updateSceneContext(sceneContextKey, scene.getVisualDescription());
        }
        
        if (scene.getAction() != null && !scene.getAction().isEmpty()) {
            prompt.append("动作：").append(scene.getAction()).append("。");
        }
        
        if (scene.getAtmosphere() != null && !scene.getAtmosphere().isEmpty()) {
            prompt.append("氛围：").append(scene.getAtmosphere()).append("。");
        }
        
        prompt.append("高质量、细节丰富、所有场景中保持完全一致的角色外观设计。");
        
        return prompt.toString();
    }
    
    private String extractSceneContextKey(Scene scene) {
        if (scene.getVisualDescription() != null && !scene.getVisualDescription().isEmpty()) {
            String desc = scene.getVisualDescription();
            if (desc.contains("房间") || desc.contains("室内")) return "indoor";
            if (desc.contains("街道") || desc.contains("户外") || desc.contains("外面")) return "outdoor";
            if (desc.contains("学校") || desc.contains("教室")) return "school";
            if (desc.contains("公园")) return "park";
            if (desc.contains("咖啡") || desc.contains("餐厅")) return "cafe";
        }
        return "general_" + (scene.getSceneNumber() / 3);
    }
    
    private void updateSceneContext(String contextKey, String visualDescription) {
        if (!sceneContexts.containsKey(contextKey)) {
            String context = "保持相同的场景背景和环境";
            if (visualDescription.contains("房间")) {
                context = "保持相同的房间布局、装饰和光照";
            } else if (visualDescription.contains("街道")) {
                context = "保持相同的街道环境、建筑和天气";
            } else if (visualDescription.contains("学校") || visualDescription.contains("教室")) {
                context = "保持相同的学校/教室环境和布置";
            } else if (visualDescription.contains("公园")) {
                context = "保持相同的公园景色和环境";
            }
            sceneContexts.put(contextKey, context);
            logger.info("[TextToImageService] Created scene context for '{}': {}", contextKey, context);
        }
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
        List<String> strictFeatures = new ArrayList<>();
        
        String hairColor = extractHairColor(characterDescription);
        if (hairColor != null) {
            keyFeatures.add(hairColor);
            strictFeatures.add("发色必须为" + hairColor);
        }
        
        String hairStyle = extractHairStyle(characterDescription);
        if (hairStyle != null) {
            keyFeatures.add(hairStyle);
            strictFeatures.add("发型必须为" + hairStyle);
        }
        
        String eyeFeatures = extractEyeFeatures(characterDescription);
        if (eyeFeatures != null) {
            keyFeatures.add(eyeFeatures);
            strictFeatures.add("眼睛特征: " + eyeFeatures);
        }
        
        String bodyType = extractBodyType(characterDescription);
        if (bodyType != null) {
            keyFeatures.add(bodyType);
            strictFeatures.add("身材: " + bodyType);
        }
        
        String clothingStyle = extractClothingStyle(characterDescription);
        if (clothingStyle != null) {
            keyFeatures.add(clothingStyle);
            strictFeatures.add("服装风格: " + clothingStyle);
        }
        
        String facialFeatures = extractFacialFeatures(characterDescription);
        if (facialFeatures != null) {
            keyFeatures.add(facialFeatures);
            strictFeatures.add("面部特征: " + facialFeatures);
        }
        
        embedding.put("keyFeatures", String.join(", ", keyFeatures));
        embedding.put("strictFeatures", String.join("; ", strictFeatures));
        embedding.put("consistencyPrompt", "严格保持" + characterName + "的固定特征: " + String.join("; ", strictFeatures) + "。这些特征在所有场景中都绝对不能改变。");
        
        return embedding;
    }
    
    private String extractHairColor(String description) {
        if (description.contains("黑发") || description.contains("黑色头发")) return "黑发";
        if (description.contains("棕发") || description.contains("棕色头发")) return "棕发";
        if (description.contains("金发") || description.contains("金色头发")) return "金发";
        if (description.contains("银发") || description.contains("银色头发")) return "银发";
        if (description.contains("白发") || description.contains("白色头发")) return "白发";
        if (description.contains("红发") || description.contains("红色头发")) return "红发";
        if (description.contains("蓝发") || description.contains("蓝色头发")) return "蓝发";
        return null;
    }
    
    private String extractHairStyle(String description) {
        if (description.contains("长发")) return "长发";
        if (description.contains("短发")) return "短发";
        if (description.contains("中长发")) return "中长发";
        if (description.contains("马尾")) return "马尾";
        if (description.contains("双马尾")) return "双马尾";
        if (description.contains("卷发")) return "卷发";
        if (description.contains("直发")) return "直发";
        if (description.contains("波浪")) return "波浪发";
        return null;
    }
    
    private String extractEyeFeatures(String description) {
        List<String> eyeFeats = new ArrayList<>();
        if (description.contains("蓝眼") || description.contains("蓝色眼睛")) eyeFeats.add("蓝色眼睛");
        else if (description.contains("棕眼") || description.contains("棕色眼睛")) eyeFeats.add("棕色眼睛");
        else if (description.contains("黑眼") || description.contains("黑色眼睛")) eyeFeats.add("黑色眼睛");
        else if (description.contains("绿眼") || description.contains("绿色眼睛")) eyeFeats.add("绿色眼睛");
        else if (description.contains("红眼") || description.contains("红色眼睛")) eyeFeats.add("红色眼睛");
        
        if (description.contains("大眼睛")) eyeFeats.add("大眼睛");
        if (description.contains("细长眼")) eyeFeats.add("细长眼");
        if (description.contains("圆眼")) eyeFeats.add("圆眼睛");
        
        return eyeFeats.isEmpty() ? null : String.join("、", eyeFeats);
    }
    
    private String extractBodyType(String description) {
        if (description.contains("高挑")) return "高挑身材";
        if (description.contains("娇小")) return "娇小身材";
        if (description.contains("苗条")) return "苗条身材";
        if (description.contains("健壮")) return "健壮体型";
        if (description.contains("匀称")) return "匀称体型";
        return null;
    }
    
    private String extractClothingStyle(String description) {
        if (description.contains("西装")) return "西装";
        if (description.contains("和服")) return "和服";
        if (description.contains("校服")) return "校服";
        if (description.contains("休闲装")) return "休闲装";
        if (description.contains("运动装")) return "运动装";
        if (description.contains("连衣裙")) return "连衣裙";
        return null;
    }
    
    private String extractFacialFeatures(String description) {
        List<String> features = new ArrayList<>();
        if (description.contains("圆脸")) features.add("圆脸");
        else if (description.contains("瓜子脸")) features.add("瓜子脸");
        else if (description.contains("方脸")) features.add("方脸");
        
        if (description.contains("高鼻梁")) features.add("高鼻梁");
        if (description.contains("小鼻")) features.add("小巧鼻子");
        
        return features.isEmpty() ? null : String.join("、", features);
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
