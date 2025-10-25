package com.aigo.service;

import com.aigo.model.AnimeSegment;
import com.aigo.model.Character;
import com.aigo.model.Scene;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import java.time.Duration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class NovelParseService {
    
    private static final Logger logger = LoggerFactory.getLogger(NovelParseService.class);
    
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.base.url}")
    private String baseUrl;
    
    @Value("${deepseek.model.name}")
    private String modelName;
    
    @Autowired
    private TextToImageService textToImageService;
    
    @Autowired
    private TextToSpeechService textToSpeechService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AnimeSegment parseNovelText(String text, String style, String targetAudience) {
        logger.info("[NovelParseService] Starting parseNovelText - text length: {}, style: {}, targetAudience: {}",
            text != null ? text.length() : 0, style, targetAudience);
        
        if ("demo-key".equals(apiKey)) {
            logger.info("[NovelParseService] Using demo mode");
            AnimeSegment segment = createDemoResponse(text);
            generateImagesForSegment(segment);
            generateAudioForSegment(segment);
            return segment;
        }
        
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .build();
            
            String prompt = buildPrompt(text, style, targetAudience);
            
            logger.info("[NovelParseService] Calling LLM model");
            String response = model.generate(prompt);
            logger.info("[NovelParseService] LLM response received");
            
            AnimeSegment segment = parseResponse(response);
            logger.info("[NovelParseService] Parsed - characters: {}, scenes: {}",
                segment.getCharacters() != null ? segment.getCharacters().size() : 0,
                segment.getScenes() != null ? segment.getScenes().size() : 0);
            
            generateImagesForSegment(segment);
            generateAudioForSegment(segment);
            
            return segment;
            
        } catch (Exception e) {
            logger.error("[NovelParseService] Failed to parse novel text", e);
            throw new RuntimeException("LLM 处理失败: " + e.getMessage(), e);
        }
    }
    
    private void generateImagesForSegment(AnimeSegment segment) {
        if (segment.getScenes() == null || segment.getScenes().isEmpty()) {
            return;
        }
        
        logger.info("[NovelParseService] Generating images for {} scenes", segment.getScenes().size());
        
        Map<String, String> characterAppearances = new HashMap<>();
        if (segment.getCharacters() != null) {
            for (Character character : segment.getCharacters()) {
                String fullDesc = String.format("%s,%s", 
                    character.getAppearance(), 
                    character.getDescription());
                characterAppearances.put(character.getName(), fullDesc);
            }
        }
        
        try {
            List<String> imageUrls = textToImageService.generateImagesForScenes(
                segment.getScenes(), 
                characterAppearances
            );
            
            for (int i = 0; i < segment.getScenes().size() && i < imageUrls.size(); i++) {
                segment.getScenes().get(i).setImageUrl(imageUrls.get(i));
            }
            
            logger.info("[NovelParseService] Image generation completed");
        } catch (Exception e) {
            logger.error("[NovelParseService] Failed to generate images", e);
        }
    }
    
    private void generateAudioForSegment(AnimeSegment segment) {
        if (segment.getScenes() == null || segment.getScenes().isEmpty()) {
            return;
        }
        
        logger.info("[NovelParseService] Generating audio for {} scenes", segment.getScenes().size());
        
        try {
            List<String> audioUrls = textToSpeechService.generateAudioForScenes(
                segment.getScenes(), 
                segment.getCharacters()
            );
            
            for (int i = 0; i < segment.getScenes().size() && i < audioUrls.size(); i++) {
                segment.getScenes().get(i).setAudioUrl(audioUrls.get(i));
            }
            
            logger.info("[NovelParseService] Audio generation completed");
        } catch (Exception e) {
            logger.error("[NovelParseService] Failed to generate audio", e);
        }
    }
    
    private String buildPrompt(String text, String style, String targetAudience) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的动漫脚本分析师。请深度理解以下小说文本,并将其转换为动漫桥段。\n\n");
        prompt.append("请提取以下信息:\n");
        prompt.append("1. 角色信息 (姓名、描述、外貌、性格、性别)\n");
        prompt.append("2. 场景分镜 - 重要: 每个角色的每句对话都应该是一个独立的场景,用于生成独立的漫画图片\n");
        prompt.append("   - 场景编号: 连续递增的数字\n");
        prompt.append("   - 角色: 说话的角色名称\n");
        prompt.append("   - 对话: 该角色在这个场景中说的话\n");
        prompt.append("   - 画面描述: 这个场景的视觉画面,包括人物动作、表情、背景等\n");
        prompt.append("   - 氛围: 场景的情绪氛围\n");
        prompt.append("   - 动作: 角色的具体动作描述\n");
        prompt.append("3. 剧情总结\n");
        prompt.append("4. 类型和情绪基调\n\n");
        
        if (style != null && !style.isEmpty()) {
            prompt.append("动漫风格: ").append(style).append("\n");
        }
        if (targetAudience != null && !targetAudience.isEmpty()) {
            prompt.append("目标观众: ").append(targetAudience).append("\n");
        }
        
        prompt.append("\n小说文本:\n").append(text).append("\n\n");
        prompt.append("请以 JSON 格式返回结果,结构如下:\n");
        prompt.append("{\n");
        prompt.append("  \"characters\": [{\"name\": \"角色名\", \"description\": \"描述\", \"appearance\": \"外貌\", \"personality\": \"性格\", \"gender\": \"male/female/unknown\"}],\n");
        prompt.append("  \"scenes\": [\n");
        prompt.append("    {\"sceneNumber\": 1, \"character\": \"角色名\", \"dialogue\": \"该角色说的话\", \"visualDescription\": \"画面描述\", \"atmosphere\": \"氛围\", \"action\": \"动作描述\"},\n");
        prompt.append("    {\"sceneNumber\": 2, \"character\": \"另一个角色名\", \"dialogue\": \"该角色说的话\", \"visualDescription\": \"画面描述\", \"atmosphere\": \"氛围\", \"action\": \"动作描述\"}\n");
        prompt.append("  ],\n");
        prompt.append("  \"plotSummary\": \"剧情总结\",\n");
        prompt.append("  \"genre\": \"类型\",\n");
        prompt.append("  \"mood\": \"情绪基调\"\n");
        prompt.append("}\n\n");
        prompt.append("注意: 每个角色的每句对话都必须是一个独立的场景对象,这样才能为每句对话生成对应的漫画图片。\n");
        
        return prompt.toString();
    }
    
    private AnimeSegment parseResponse(String response) {
        try {
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonContent = response.substring(jsonStart, jsonEnd);
                AnimeSegment segment = objectMapper.readValue(jsonContent, AnimeSegment.class);
                enrichCharactersWithGender(segment.getCharacters());
                return segment;
            } else {
                logger.warn("No JSON found in response, creating structured response from text");
                return createFallbackResponse(response);
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON response", e);
            return createFallbackResponse(response);
        }
    }
    
    private AnimeSegment createDemoResponse(String text) {
        AnimeSegment segment = new AnimeSegment();
        
        List<Character> characters = new ArrayList<>();
        characters.add(new Character("主角", "故事的主人公", "年轻、充满活力", "勇敢、善良", "male"));
        characters.add(new Character("旁白", "叙述者", "无形", "客观", "neutral"));
        segment.setCharacters(characters);
        
        List<Scene> scenes = new ArrayList<>();
        scenes.add(new Scene(1, "旁白", "新的一天开始了。", 
            "清晨的城市街道,阳光洒在街道上", "宁静、温暖", "镜头从天空慢慢拉近街道", null, null));
        scenes.add(new Scene(2, "主角", "今天会是美好的一天!", 
            "主角站在街道上,面带微笑仰望天空", "充满希望", "主角伸展双臂,深呼吸", null, null));
        segment.setScenes(scenes);
        
        segment.setPlotSummary("这是一个关于" + text.substring(0, Math.min(20, text.length())) + "...的故事");
        segment.setGenre("青春、冒险");
        segment.setMood("积极向上");
        
        return segment;
    }
    
    private AnimeSegment createFallbackResponse(String rawResponse) {
        AnimeSegment segment = new AnimeSegment();
        segment.setCharacters(new ArrayList<>());
        segment.setScenes(new ArrayList<>());
        segment.setPlotSummary(rawResponse.substring(0, Math.min(200, rawResponse.length())));
        segment.setGenre("未分类");
        segment.setMood("未知");
        return segment;
    }
    
    private void enrichCharactersWithGender(List<Character> characters) {
        if (characters == null || characters.isEmpty()) {
            return;
        }
        
        for (Character character : characters) {
            if (character.getGender() == null || character.getGender().trim().isEmpty() || 
                "unknown".equalsIgnoreCase(character.getGender())) {
                String detectedGender = detectGender(character.getName(), character);
                character.setGender(detectedGender);
                logger.info("[NovelParseService] Detected gender '{}' for character '{}'", 
                    detectedGender, character.getName());
            }
        }
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
}
