package com.aigo.service;

import com.aigo.model.Character;
import com.aigo.model.Scene;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class TextToSpeechService {
    
    private static final Logger logger = LoggerFactory.getLogger(TextToSpeechService.class);
    
    @Value("${qiniu.tts.api.key}")
    private String apiKey;
    
    @Value("${qiniu.tts.api.base.url}")
    private String baseUrl;
    
    @Autowired
    private QiniuStorageService qiniuStorageService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    private final Map<String, String> characterVoiceCache = new HashMap<>();
    private List<VoiceProfile> availableVoices = null;
    
    public List<String> generateAudioForScenes(List<Scene> scenes, List<Character> characters) {
        if (scenes == null || scenes.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.info("[TextToSpeechService] Generating audio for {} scenes", scenes.size());
        
        Map<String, Character> characterMap = new HashMap<>();
        if (characters != null) {
            for (Character character : characters) {
                characterMap.put(character.getName(), character);
            }
        }
        
        List<String> audioUrls = new ArrayList<>();
        
        for (Scene scene : scenes) {
            try {
                if (scene.getDialogue() == null || scene.getDialogue().trim().isEmpty()) {
                    audioUrls.add(null);
                    continue;
                }
                
                Character character = characterMap.get(scene.getCharacter());
                String voiceType = getVoiceForCharacter(scene.getCharacter(), character);
                
                String audioUrl = generateSingleAudio(scene.getDialogue(), voiceType, scene.getSceneNumber());
                audioUrls.add(audioUrl);
                
            } catch (Exception e) {
                logger.error("[TextToSpeechService] Failed to generate audio for scene {}", 
                    scene.getSceneNumber(), e);
                audioUrls.add(null);
            }
        }
        
        return audioUrls;
    }
    
    private String generateSingleAudio(String text, String voiceType, int sceneNumber) throws Exception {
        if ("demo-key".equals(apiKey)) {
            logger.info("[TextToSpeechService] Demo mode - returning placeholder audio URL");
            return "https://example.com/audio/scene_" + sceneNumber + ".mp3";
        }
        
        String endpoint = baseUrl + "/voice/tts";
        
        Map<String, Object> audioParams = new HashMap<>();
        audioParams.put("voice_type", voiceType);
        audioParams.put("encoding", "mp3");
        audioParams.put("speed_ratio", 1.0);
        
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("text", text);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("audio", audioParams);
        requestBody.put("request", requestParams);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        logger.debug("[TextToSpeechService] Request body: {}", jsonBody);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            logger.error("[TextToSpeechService] TTS API failed with status: {}", response.statusCode());
            logger.error("[TextToSpeechService] Request body was: {}", jsonBody);
            logger.error("[TextToSpeechService] Response body: {}", response.body());
            throw new RuntimeException("TTS API failed with status: " + response.statusCode() + ", response: " + response.body());
        }
        
        JsonNode responseJson = objectMapper.readTree(response.body());
        String base64Audio = responseJson.get("data").asText();
        
        String audioUrl = qiniuStorageService.uploadBase64Audio(base64Audio, "scene_" + sceneNumber);
        
        logger.info("[TextToSpeechService] Successfully generated audio for scene {}", sceneNumber);
        return audioUrl;
    }
    
    private String getVoiceForCharacter(String characterName, Character character) {
        if (characterVoiceCache.containsKey(characterName)) {
            return characterVoiceCache.get(characterName);
        }
        
        String gender = detectGender(characterName, character);
        String ageGroup = detectAgeGroup(characterName, character);
        String voiceType = selectVoiceByCharacteristics(gender, ageGroup);
        
        characterVoiceCache.put(characterName, voiceType);
        logger.info("[TextToSpeechService] Mapped character '{}' to voice '{}' (gender: {}, age: {})", 
            characterName, voiceType, gender, ageGroup);
        
        return voiceType;
    }
    
    private String detectGender(String characterName, Character character) {
        if (character == null) {
            return detectGenderFromName(characterName);
        }
        
        String combinedText = (character.getDescription() + " " + 
                              character.getAppearance() + " " + 
                              character.getPersonality()).toLowerCase();
        
        Set<String> maleKeywords = new HashSet<>(Arrays.asList(
            "男", "他", "先生", "男性", "男孩", "男人", "少年", "哥哥", "兄弟", "父亲", "爸爸", "叔叔", "爷爷", "公"
        ));
        Set<String> femaleKeywords = new HashSet<>(Arrays.asList(
            "女", "她", "女士", "女性", "女孩", "女人", "少女", "姐姐", "妹妹", "母亲", "妈妈", "阿姨", "奶奶", "婆婆"
        ));
        
        int maleScore = 0;
        int femaleScore = 0;
        
        for (String keyword : maleKeywords) {
            if (combinedText.contains(keyword)) {
                maleScore++;
            }
        }
        
        for (String keyword : femaleKeywords) {
            if (combinedText.contains(keyword)) {
                femaleScore++;
            }
        }
        
        if (maleScore > femaleScore) {
            return "male";
        } else if (femaleScore > maleScore) {
            return "female";
        }
        
        return detectGenderFromName(characterName);
    }
    
    private String detectAgeGroup(String characterName, Character character) {
        if (character == null) {
            return "young_adult";
        }
        
        String combinedText = (character.getDescription() + " " + 
                              character.getAppearance() + " " + 
                              character.getPersonality()).toLowerCase();
        
        if (combinedText.contains("老") || combinedText.contains("年迈") || 
            combinedText.contains("苍老") || combinedText.contains("白发") ||
            combinedText.contains("爷爷") || combinedText.contains("奶奶") ||
            combinedText.contains("长者") || combinedText.contains("老人")) {
            return "elderly";
        }
        
        if (combinedText.contains("中年") || combinedText.contains("叔叔") || 
            combinedText.contains("阿姨") || combinedText.contains("成熟") ||
            combinedText.contains("父亲") || combinedText.contains("母亲")) {
            return "middle_aged";
        }
        
        if (combinedText.contains("少年") || combinedText.contains("少女") || 
            combinedText.contains("小孩") || combinedText.contains("孩子") ||
            combinedText.contains("儿童") || combinedText.contains("童") ||
            combinedText.contains("幼")) {
            return "teenager";
        }
        
        if (combinedText.contains("青年") || combinedText.contains("年轻") ||
            combinedText.contains("小伙") || combinedText.contains("姑娘")) {
            return "young_adult";
        }
        
        return "young_adult";
    }
    
    private String detectGenderFromName(String name) {
        String nameLower = name.toLowerCase();
        
        Set<String> maleNameIndicators = new HashSet<>(Arrays.asList(
            "明", "强", "刚", "军", "伟", "涛", "龙", "杰", "鹏", "磊"
        ));
        Set<String> femaleNameIndicators = new HashSet<>(Arrays.asList(
            "娜", "婷", "丽", "芳", "静", "雅", "兰", "燕", "莉", "萍"
        ));
        
        for (String indicator : femaleNameIndicators) {
            if (nameLower.contains(indicator)) {
                return "female";
            }
        }
        
        for (String indicator : maleNameIndicators) {
            if (nameLower.contains(indicator)) {
                return "male";
            }
        }
        
        return "neutral";
    }
    
    private String selectVoiceByGender(String gender) {
        return "qiniu_zh_female_wwxkjx";
    }
    
    private String selectVoiceByCharacteristics(String gender, String ageGroup) {
        String key = gender + "_" + ageGroup;
        
        switch (key) {
            case "male_elderly":
                return "qiniu_zh_male_ybxknjs";
            
            case "male_middle_aged":
                return "qiniu_zh_male_wncwxz";
            
            case "male_young_adult":
                return "qiniu_zh_male_tyygjs";
            
            case "male_teenager":
                return "qiniu_zh_male_hlsnkk";
            
            case "female_elderly":
                return "qiniu_zh_female_sqjyay";
            
            case "female_middle_aged":
                return "qiniu_zh_female_kljxdd";
            
            case "female_young_adult":
                return "qiniu_zh_female_wwxkjx";
            
            case "female_teenager":
                return "qiniu_zh_female_xyqxxj";
            
            case "neutral_elderly":
                return "qiniu_zh_male_ybxknjs";
            
            case "neutral_middle_aged":
                return "qiniu_zh_male_tyygjs";
            
            case "neutral_young_adult":
                return "qiniu_zh_female_wwxkjx";
            
            case "neutral_teenager":
                return "qiniu_zh_male_hlsnkk";
            
            default:
                return "qiniu_zh_female_wwxkjx";
        }
    }
    
    private void fetchVoiceList() {
        if ("demo-key".equals(apiKey)) {
            logger.info("[TextToSpeechService] Demo mode - skipping voice list fetch");
            return;
        }
        
        try {
            String endpoint = baseUrl + "/voice/list";
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode voicesJson = objectMapper.readTree(response.body());
                availableVoices = parseVoiceList(voicesJson);
                logger.info("[TextToSpeechService] Fetched {} available voices", availableVoices.size());
            }
            
        } catch (Exception e) {
            logger.warn("[TextToSpeechService] Failed to fetch voice list, using defaults", e);
        }
    }
    
    private List<VoiceProfile> parseVoiceList(JsonNode json) {
        List<VoiceProfile> voices = new ArrayList<>();
        
        return voices;
    }
    
    private static class VoiceProfile {
        String id;
        String name;
        String gender;
        
        VoiceProfile(String id, String name, String gender) {
            this.id = id;
            this.name = name;
            this.gender = gender;
        }
    }
}
