package com.aigo.service;

import com.aigo.model.Character;
import com.aigo.model.Scene;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private final String apiKey;
    private final String baseUrl;
    private final QiniuStorageService qiniuStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<String, String> characterVoiceCache = new HashMap<>();
    private List<VoiceProfile> availableVoices = null;
    
    public TextToSpeechService(
            @Value("${qiniu.tts.api.key}") String apiKey,
            @Value("${qiniu.tts.api.base.url}") String baseUrl,
            QiniuStorageService qiniuStorageService) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.qiniuStorageService = qiniuStorageService;
    }
    
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
        String voiceType = selectVoiceByGender(gender);
        
        characterVoiceCache.put(characterName, voiceType);
        logger.info("[TextToSpeechService] Mapped character '{}' to voice '{}' (gender: {})", 
            characterName, voiceType, gender);
        
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
            "男", "他", "先生", "男性", "男孩", "男人", "少年", "哥哥", "兄弟", "父亲", "爸爸"
        ));
        Set<String> femaleKeywords = new HashSet<>(Arrays.asList(
            "女", "她", "女士", "女性", "女孩", "女人", "少女", "姐姐", "妹妹", "母亲", "妈妈"
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
