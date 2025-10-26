package com.aigo.service;

import com.aigo.entity.CharacterEntity;
import com.aigo.exception.BusinessException;
import com.aigo.dto.ErrorCode;
import com.aigo.repository.CharacterRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CharacterService {
    
    private static final Logger logger = LoggerFactory.getLogger(CharacterService.class);
    
    private final CharacterRepository characterRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.base.url}")
    private String baseUrl;
    
    @Value("${deepseek.model.name}")
    private String modelName;
    
    @Transactional
    public CharacterEntity createCharacter(CharacterEntity character) {
        if (character.getWorkId() != null) {
            Optional<CharacterEntity> existing = characterRepository.findByWorkIdAndName(
                character.getWorkId(), character.getName());
            if (existing.isPresent()) {
                return existing.get();
            }
        } else if (characterRepository.findByName(character.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色名称已存在");
        }
        return characterRepository.save(character);
    }
    
    @Transactional
    public CharacterEntity createOrUpdateWorkCharacter(String workId, String characterName, 
                                                       String description, String appearance, 
                                                       String personality, String gender, 
                                                       Boolean isProtagonist) {
        return createOrUpdateWorkCharacter(workId, characterName, description, appearance, 
            personality, gender, isProtagonist, null, null, null, null, null);
    }
    
    @Transactional
    public CharacterEntity createOrUpdateWorkCharacter(String workId, String characterName, 
                                                       String description, String appearance, 
                                                       String personality, String gender, 
                                                       Boolean isProtagonist,
                                                       String bodyType, String facialFeatures,
                                                       String clothingStyle, String distinguishingFeatures,
                                                       Boolean isPlaceholderName) {
        Optional<CharacterEntity> existing = characterRepository.findByWorkIdAndName(workId, characterName);
        
        CharacterEntity character;
        if (existing.isPresent()) {
            character = existing.get();
            if (description != null && !description.isEmpty()) {
                character.setDescription(description);
            }
            if (appearance != null && !appearance.isEmpty()) {
                character.setAppearance(appearance);
            }
            if (personality != null && !personality.isEmpty()) {
                character.setPersonality(personality);
            }
            if (gender != null && !gender.isEmpty()) {
                character.setGender(gender);
            }
            if (isProtagonist != null) {
                character.setIsProtagonist(isProtagonist);
            }
            if (bodyType != null && !bodyType.isEmpty()) {
                character.setBodyType(bodyType);
            }
            if (facialFeatures != null && !facialFeatures.isEmpty()) {
                character.setFacialFeatures(facialFeatures);
            }
            if (clothingStyle != null && !clothingStyle.isEmpty()) {
                character.setClothingStyle(clothingStyle);
            }
            if (distinguishingFeatures != null && !distinguishingFeatures.isEmpty()) {
                character.setDistinguishingFeatures(distinguishingFeatures);
            }
            if (isPlaceholderName != null && !isPlaceholderName && character.getIsPlaceholderName()) {
                character.setIsPlaceholderName(false);
            }
        } else {
            character = new CharacterEntity();
            character.setWorkId(workId);
            character.setName(characterName);
            character.setDescription(description);
            character.setAppearance(appearance);
            character.setPersonality(personality);
            character.setGender(gender);
            character.setIsProtagonist(isProtagonist != null ? isProtagonist : false);
            character.setBodyType(bodyType);
            character.setFacialFeatures(facialFeatures);
            character.setClothingStyle(clothingStyle);
            character.setDistinguishingFeatures(distinguishingFeatures);
            character.setIsPlaceholderName(isPlaceholderName != null ? isPlaceholderName : false);
        }
        
        ensureCompleteCharacterFeatures(character, workId);
        
        return characterRepository.save(character);
    }
    
    @Transactional(readOnly = true)
    public CharacterEntity getCharacterById(Long id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "角色不存在"));
    }
    
    @Transactional(readOnly = true)
    public CharacterEntity getCharacterByName(String name) {
        return characterRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "角色不存在"));
    }
    
    @Transactional(readOnly = true)
    public List<CharacterEntity> getAllCharacters() {
        return characterRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<CharacterEntity> searchCharactersByName(String name) {
        return characterRepository.findByNameContaining(name);
    }
    
    @Transactional
    public CharacterEntity updateCharacter(Long id, CharacterEntity character) {
        CharacterEntity existingCharacter = getCharacterById(id);
        if (character.getName() != null) {
            existingCharacter.setName(character.getName());
        }
        if (character.getDescription() != null) {
            existingCharacter.setDescription(character.getDescription());
        }
        if (character.getAppearance() != null) {
            existingCharacter.setAppearance(character.getAppearance());
        }
        if (character.getPersonality() != null) {
            existingCharacter.setPersonality(character.getPersonality());
        }
        return characterRepository.save(existingCharacter);
    }
    
    @Transactional
    public void deleteCharacter(Long id) {
        if (!characterRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        characterRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<CharacterEntity> getCharactersByWorkId(String workId) {
        return characterRepository.findByWorkIdOrderByCreatedAtAsc(workId);
    }
    
    @Transactional(readOnly = true)
    public Optional<CharacterEntity> getWorkCharacterByName(String workId, String name) {
        return characterRepository.findByWorkIdAndName(workId, name);
    }
    
    private void ensureCompleteCharacterFeatures(CharacterEntity character, String workId) {
        if ("demo-key".equals(apiKey)) {
            logger.info("[CharacterService] Skipping AI feature generation in demo mode");
            return;
        }
        
        List<String> emptyFields = new ArrayList<>();
        
        if (isEmptyOrUnknown(character.getGender())) {
            emptyFields.add("gender");
        }
        if (isEmptyOrUnknown(character.getAppearance())) {
            emptyFields.add("appearance");
        }
        if (isEmptyOrUnknown(character.getBodyType())) {
            emptyFields.add("bodyType");
        }
        if (isEmptyOrUnknown(character.getFacialFeatures())) {
            emptyFields.add("facialFeatures");
        }
        if (isEmptyOrUnknown(character.getClothingStyle())) {
            emptyFields.add("clothingStyle");
        }
        if (isEmptyOrUnknown(character.getDistinguishingFeatures())) {
            emptyFields.add("distinguishingFeatures");
        }
        
        if (!emptyFields.isEmpty()) {
            logger.info("[CharacterService] Character '{}' has {} empty fields: {}", 
                       character.getName(), emptyFields.size(), emptyFields);
            
            try {
                Map<String, String> generatedFeatures = generateCharacterFeatures(
                    character.getName(),
                    character.getDescription(),
                    character.getPersonality(),
                    workId,
                    emptyFields
                );
                
                if (generatedFeatures.containsKey("gender") && isEmptyOrUnknown(character.getGender())) {
                    character.setGender(generatedFeatures.get("gender"));
                }
                if (generatedFeatures.containsKey("appearance") && isEmptyOrUnknown(character.getAppearance())) {
                    character.setAppearance(generatedFeatures.get("appearance"));
                }
                if (generatedFeatures.containsKey("bodyType") && isEmptyOrUnknown(character.getBodyType())) {
                    character.setBodyType(generatedFeatures.get("bodyType"));
                }
                if (generatedFeatures.containsKey("facialFeatures") && isEmptyOrUnknown(character.getFacialFeatures())) {
                    character.setFacialFeatures(generatedFeatures.get("facialFeatures"));
                }
                if (generatedFeatures.containsKey("clothingStyle") && isEmptyOrUnknown(character.getClothingStyle())) {
                    character.setClothingStyle(generatedFeatures.get("clothingStyle"));
                }
                if (generatedFeatures.containsKey("distinguishingFeatures") && isEmptyOrUnknown(character.getDistinguishingFeatures())) {
                    character.setDistinguishingFeatures(generatedFeatures.get("distinguishingFeatures"));
                }
                
                logger.info("[CharacterService] Successfully generated {} character features", generatedFeatures.size());
            } catch (Exception e) {
                logger.error("[CharacterService] Failed to generate character features for '{}'", character.getName(), e);
            }
        }
    }
    
    private boolean isEmptyOrUnknown(String value) {
        return value == null || 
               value.trim().isEmpty() || 
               value.equalsIgnoreCase("未知") ||
               value.equalsIgnoreCase("unknown") ||
               value.equals("null");
    }
    
    private Map<String, String> generateCharacterFeatures(
            String name,
            String description,
            String personality,
            String workId,
            List<String> fields) {
        
        logger.info("[CharacterService] Generating features for character '{}', fields: {}", name, fields);
        
        StringBuilder contextBuilder = new StringBuilder();
        if (workId != null) {
            try {
                List<CharacterEntity> workCharacters = characterRepository.findByWorkIdOrderByCreatedAtAsc(workId);
                if (!workCharacters.isEmpty()) {
                    contextBuilder.append("\n作品中已有角色：\n");
                    for (CharacterEntity c : workCharacters) {
                        if (!c.getName().equals(name)) {
                            contextBuilder.append(String.format("- %s: %s\n", c.getName(), 
                                c.getDescription() != null ? c.getDescription() : "无描述"));
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("[CharacterService] Failed to load work characters for context", e);
            }
        }
        
        String prompt = String.format("""
            请根据以下信息生成角色的外貌特征。
            
            角色名称：%s
            角色描述：%s
            性格特征：%s%s
            
            需要生成的字段：%s
            
            请生成合理且一致的角色外貌特征。要求：
            1. gender: 必须是 "male" 或 "female" 之一
            2. appearance: 完整的外貌描述（50-100字），包含整体印象和主要特征
            3. bodyType: 身高和体型的简短描述（如："高挑匀称"、"中等身材"、"矮小精悍"）
            4. facialFeatures: 五官特征的详细描述（如："瓜子脸，丹凤眼，高挺的鼻子"）
            5. clothingStyle: 典型的服装风格（如："休闲运动装"、"商务正装"、"校园风"）
            6. distinguishingFeatures: 最显著的外貌特征（如："齐肩黑色长发"、"左眼角有颗泪痣"）
            
            请以纯JSON格式返回，不要包含任何其他文字，格式如下：
            {
              "gender": "male",
              "appearance": "外貌描述...",
              "bodyType": "体型描述",
              "facialFeatures": "五官描述",
              "clothingStyle": "服装风格",
              "distinguishingFeatures": "显著特征"
            }
            
            只返回需要生成的字段即可。
            """,
            name,
            description != null ? description : "无",
            personality != null ? personality : "无",
            contextBuilder.toString(),
            String.join(", ", fields)
        );
        
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.8)
                .timeout(Duration.ofSeconds(30))
                .maxRetries(2)
                .build();
            
            String response = model.generate(prompt);
            logger.info("[CharacterService] LLM response: {}", response);
            
            String jsonContent = extractJsonFromResponse(response);
            Map<String, String> features = objectMapper.readValue(jsonContent, new TypeReference<Map<String, String>>() {});
            
            return features;
        } catch (Exception e) {
            logger.error("[CharacterService] Failed to generate character features", e);
            return new HashMap<>();
        }
    }
    
    private String extractJsonFromResponse(String response) {
        if (response == null) {
            return "{}";
        }
        
        response = response.trim();
        
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        
        response = response.trim();
        
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return response;
    }
}
