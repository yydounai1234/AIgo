package com.aigo.service;

import com.aigo.entity.CharacterEntity;
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
        return generateImagesForScenes(scenes, characterAppearances, null);
    }
    
    public List<String> generateImagesForScenes(List<Scene> scenes, Map<String, String> characterAppearances,
                                                Map<String, Map<String, Object>> existingEmbeddings) {
        logger.info("[TextToImageService] Generating images for {} scenes in parallel", scenes.size());
        
        if (scenes.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (existingEmbeddings != null && !existingEmbeddings.isEmpty()) {
            for (Map.Entry<String, Map<String, Object>> entry : existingEmbeddings.entrySet()) {
                characterEmbeddings.put(entry.getKey(), entry.getValue());
            }
            logger.info("[TextToImageService] Loaded {} existing character embeddings", existingEmbeddings.size());
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
                prompt.append("\n\n【关键约束 - 必须遵守】: ").append(embedding.get("strictFeatures")).append("。");
            }
            
            prompt.append("\n\n参考角色特征数据库：");
            if (embedding.containsKey("visualStyle")) {
                prompt.append("\n- 视觉风格: ").append(embedding.get("visualStyle"));
            }
            if (embedding.containsKey("keyFeatures")) {
                prompt.append("\n- 关键特征: ").append(embedding.get("keyFeatures"));
            }
            if (embedding.containsKey("hairColor")) {
                prompt.append("\n- 发色: ").append(embedding.get("hairColor"));
            }
            if (embedding.containsKey("hairType")) {
                prompt.append("\n- 发型: ").append(embedding.get("hairType"));
            }
            if (embedding.containsKey("eyeColor")) {
                prompt.append("\n- 眼睛颜色: ").append(embedding.get("eyeColor"));
            }
            if (embedding.containsKey("eyeType")) {
                prompt.append("\n- 眼型: ").append(embedding.get("eyeType"));
            }
            if (embedding.containsKey("faceShape")) {
                prompt.append("\n- 脸型: ").append(embedding.get("faceShape"));
            }
            if (embedding.containsKey("skinTone")) {
                prompt.append("\n- 肤色: ").append(embedding.get("skinTone"));
            }
            if (embedding.containsKey("bodyType")) {
                prompt.append("\n- 体型: ").append(embedding.get("bodyType"));
            }
            if (embedding.containsKey("clothingStyle")) {
                prompt.append("\n- 服装风格: ").append(embedding.get("clothingStyle"));
            }
            
            if (embedding.containsKey("consistencyPrompt")) {
                prompt.append("\n\n").append(embedding.get("consistencyPrompt"));
            }
            
            prompt.append("\n\n注意：角色的发色、发型、眼睛特征在所有画面中必须完全相同，不允许有任何变化。");
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
    
    public Map<String, Object> extractCharacterEmbeddingFromEntity(CharacterEntity character) {
        if (character == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> embedding = new HashMap<>();
        
        embedding.put("characterName", character.getName());
        embedding.put("timestamp", System.currentTimeMillis());
        
        StringBuilder fullDescription = new StringBuilder();
        if (character.getAppearance() != null && !character.getAppearance().isEmpty()) {
            fullDescription.append(character.getAppearance());
        }
        if (character.getDescription() != null && !character.getDescription().isEmpty()) {
            if (fullDescription.length() > 0) fullDescription.append(". ");
            fullDescription.append(character.getDescription());
        }
        embedding.put("description", fullDescription.toString());
        
        embedding.put("visualStyle", "动漫风格");
        
        List<String> keyFeatures = new ArrayList<>();
        List<String> strictFeatures = new ArrayList<>();
        
        if (character.getHairColor() != null && !character.getHairColor().isEmpty()) {
            keyFeatures.add(character.getHairColor() + "头发");
            strictFeatures.add("发色必须为" + character.getHairColor());
            embedding.put("hairColor", character.getHairColor());
        }
        
        if (character.getHairType() != null && !character.getHairType().isEmpty()) {
            keyFeatures.add(character.getHairType());
            strictFeatures.add("发型必须为" + character.getHairType());
            embedding.put("hairType", character.getHairType());
        }
        
        if (character.getEyeColor() != null && !character.getEyeColor().isEmpty()) {
            keyFeatures.add(character.getEyeColor() + "眼睛");
            strictFeatures.add("眼睛颜色必须为" + character.getEyeColor());
            embedding.put("eyeColor", character.getEyeColor());
        }
        
        if (character.getEyeType() != null && !character.getEyeType().isEmpty()) {
            keyFeatures.add(character.getEyeType());
            strictFeatures.add("眼型必须为" + character.getEyeType());
            embedding.put("eyeType", character.getEyeType());
        }
        
        if (character.getFaceShape() != null && !character.getFaceShape().isEmpty()) {
            keyFeatures.add(character.getFaceShape());
            embedding.put("faceShape", character.getFaceShape());
        }
        
        if (character.getSkinTone() != null && !character.getSkinTone().isEmpty()) {
            keyFeatures.add(character.getSkinTone() + "肤色");
            embedding.put("skinTone", character.getSkinTone());
        }
        
        if (character.getHeight() != null && !character.getHeight().isEmpty()) {
            keyFeatures.add(character.getHeight());
            embedding.put("height", character.getHeight());
        }
        
        if (character.getBuild() != null && !character.getBuild().isEmpty()) {
            keyFeatures.add(character.getBuild());
            embedding.put("build", character.getBuild());
        }
        
        if (character.getBodyType() != null && !character.getBodyType().isEmpty()) {
            keyFeatures.add(character.getBodyType());
            embedding.put("bodyType", character.getBodyType());
        }
        
        if (character.getClothingStyle() != null && !character.getClothingStyle().isEmpty()) {
            keyFeatures.add(character.getClothingStyle());
            embedding.put("clothingStyle", character.getClothingStyle());
        }
        
        if (character.getDistinguishingFeatures() != null && !character.getDistinguishingFeatures().isEmpty()) {
            keyFeatures.add(character.getDistinguishingFeatures());
            embedding.put("distinguishingFeatures", character.getDistinguishingFeatures());
        }
        
        if (character.getFacialFeatures() != null && !character.getFacialFeatures().isEmpty()) {
            embedding.put("facialFeatures", character.getFacialFeatures());
        }
        
        embedding.put("keyFeatures", String.join(", ", keyFeatures));
        embedding.put("strictFeatures", strictFeatures);
        
        String consistencyPrompt = "【关键约束 - 必须遵守】角色" + character.getName() + "的以下特征在所有场景中绝对不能改变: " + 
            String.join("; ", strictFeatures.isEmpty() ? Collections.singletonList("保持整体外观一致") : strictFeatures);
        embedding.put("consistencyPrompt", consistencyPrompt);
        
        logger.info("[TextToImageService] Extracted detailed embedding for '{}' with {} key features and {} strict constraints",
            character.getName(), keyFeatures.size(), strictFeatures.size());
        
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
    
    public String generateBaseCharacterImage(CharacterEntity character) {
        if ("demo-key".equals(apiKey)) {
            logger.info("[TextToImageService] Using demo mode for base character image: {}", character.getName());
            return "http://via.placeholder.com/1024x1024.png?text=Character+" + character.getName();
        }
        
        int maxRetries = 5;
        int retryDelaySeconds = 30;
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String prompt = buildBaseCharacterPrompt(character);
                
                logger.info("[TextToImageService] Generating base image for character '{}' (attempt {}/{})", 
                    character.getName(), attempt, maxRetries);
                String base64ImageData = callTextToImageApi(prompt);
                
                String filePrefix = "character_base_" + character.getName() + "_" + System.currentTimeMillis();
                String publicUrl = qiniuStorageService.uploadBase64Image(base64ImageData, filePrefix);
                
                logger.info("[TextToImageService] Base character image generated: {}", publicUrl);
                return publicUrl;
                
            } catch (Exception e) {
                lastException = e;
                logger.error("[TextToImageService] Failed to generate base character image for '{}' (attempt {}/{})", 
                    character.getName(), attempt, maxRetries, e);
                
                if (attempt < maxRetries) {
                    try {
                        logger.info("[TextToImageService] Retrying in {} seconds...", retryDelaySeconds);
                        Thread.sleep(retryDelaySeconds * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断: " + ie.getMessage(), ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("基础角色图片生成失败，已重试" + maxRetries + "次: " + lastException.getMessage(), lastException);
    }
    
    private String buildBaseCharacterPrompt(CharacterEntity character) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("动漫/漫画风格角色立绘。");
        prompt.append("正面全身像，标准姿势，白色或简单纯色背景。");
        
        if (character.getName() != null && !character.getName().isEmpty()) {
            prompt.append("角色名称：").append(character.getName()).append("。");
        }
        if (character.getDescription() != null && !character.getDescription().isEmpty()) {
            prompt.append("角色描述：").append(character.getDescription()).append("。");
        }
        if (character.getHairColor() != null && !character.getHairColor().isEmpty()) {
            prompt.append("发色：").append(character.getHairColor()).append("。");
        }
        if (character.getHairType() != null && !character.getHairType().isEmpty()) {
            prompt.append("发型：").append(character.getHairType()).append("。");
        }
        if (character.getEyeColor() != null && !character.getEyeColor().isEmpty()) {
            prompt.append("眼睛颜色：").append(character.getEyeColor()).append("。");
        }
        if (character.getEyeType() != null && !character.getEyeType().isEmpty()) {
            prompt.append("眼型：").append(character.getEyeType()).append("。");
        }
        if (character.getFaceShape() != null && !character.getFaceShape().isEmpty()) {
            prompt.append("脸型：").append(character.getFaceShape()).append("。");
        }
        if (character.getSkinTone() != null && !character.getSkinTone().isEmpty()) {
            prompt.append("肤色：").append(character.getSkinTone()).append("。");
        }
        if (character.getBodyType() != null && !character.getBodyType().isEmpty()) {
            prompt.append("体型：").append(character.getBodyType()).append("。");
        }
        if (character.getHeight() != null && !character.getHeight().isEmpty()) {
            prompt.append("身高：").append(character.getHeight()).append("。");
        }
        if (character.getClothingStyle() != null && !character.getClothingStyle().isEmpty()) {
            prompt.append("服装：").append(character.getClothingStyle()).append("。");
        }
        if (character.getAppearance() != null && !character.getAppearance().isEmpty()) {
            prompt.append("外貌补充：").append(character.getAppearance()).append("。");
        }
        if (character.getDistinguishingFeatures() != null && !character.getDistinguishingFeatures().isEmpty()) {
            prompt.append("显著特征：").append(character.getDistinguishingFeatures()).append("。");
        }
        
        prompt.append("高质量，细节丰富，角色清晰完整。");
        
        return prompt.toString();
    }
    
    public String generateSceneFromBaseImage(Scene scene, String baseImageUrl, CharacterEntity character) {
        if ("demo-key".equals(apiKey)) {
            logger.info("[TextToImageService] Using demo mode for scene {} with base image", scene.getSceneNumber());
            return createDemoImageUrl(scene);
        }
        
        try {
            String prompt = buildScenePromptForImageToImage(scene, character);
            
            logger.info("[TextToImageService] Generating scene {} from base image using Image-to-Image", scene.getSceneNumber());
            String base64ImageData = callImageToImageApi(baseImageUrl, prompt);
            
            String filePrefix = "scene_" + scene.getSceneNumber() + "_" + System.currentTimeMillis();
            String publicUrl = qiniuStorageService.uploadBase64Image(base64ImageData, filePrefix);
            
            return publicUrl;
            
        } catch (Exception e) {
            logger.error("[TextToImageService] Failed to generate scene {} from base image", scene.getSceneNumber(), e);
            throw new RuntimeException("场景图片生成失败: " + e.getMessage(), e);
        }
    }
    
    private String buildScenePromptForImageToImage(Scene scene, CharacterEntity character) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("保持角色外观完全一致。");
        
        if (scene.getVisualDescription() != null && !scene.getVisualDescription().isEmpty()) {
            prompt.append("场景环境：").append(scene.getVisualDescription()).append("。");
        }
        if (scene.getAction() != null && !scene.getAction().isEmpty()) {
            prompt.append("角色动作：").append(scene.getAction()).append("。");
        }
        if (scene.getAtmosphere() != null && !scene.getAtmosphere().isEmpty()) {
            prompt.append("画面氛围：").append(scene.getAtmosphere()).append("。");
        }
        if (scene.getDialogue() != null && !scene.getDialogue().isEmpty()) {
            prompt.append("对话内容：").append(scene.getDialogue()).append("。");
        }
        
        prompt.append("动漫/漫画风格，高质量，细节丰富。");
        prompt.append("角色的发型、发色、眼睛、脸型、服装必须与原图完全一致。");
        
        return prompt.toString();
    }
    
    private String callImageToImageApi(String imageUrl, String prompt) throws Exception {
        String url = "https://api.qnaigc.com/v1/images/edits";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("image", imageUrl);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        logger.info("[TextToImageService] Calling Image-to-Image API with prompt: {}", prompt);
        
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
        
        throw new RuntimeException("Image-to-Image API 返回的响应中没有图片数据");
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
