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
    
    @Autowired
    private CharacterService characterService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AnimeSegment parseNovelText(String text, String style, String targetAudience) {
        return parseNovelTextWithWorkId(text, style, targetAudience, null);
    }
    
    public AnimeSegment parseNovelTextWithWorkId(String text, String style, String targetAudience, String workId) {
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
            List<com.aigo.entity.CharacterEntity> workCharacters = null;
            if (workId != null) {
                try {
                    workCharacters = characterService.getCharactersByWorkId(workId);
                    logger.info("[NovelParseService] Loaded {} existing work characters for context", 
                        workCharacters != null ? workCharacters.size() : 0);
                } catch (Exception e) {
                    logger.warn("[NovelParseService] Failed to load work characters", e);
                }
            }
            
            ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .build();
            
            String prompt = buildPromptWithWorkCharacters(text, style, targetAudience, workCharacters);
            
            logger.info("[NovelParseService] Calling LLM model");
            String response = model.generate(prompt);
            logger.info("[NovelParseService] LLM response received");
            
            AnimeSegment segment = parseResponse(response);
            logger.info("[NovelParseService] Parsed - characters: {}, scenes: {}",
                segment.getCharacters() != null ? segment.getCharacters().size() : 0,
                segment.getScenes() != null ? segment.getScenes().size() : 0);
            
            assignPlaceholderNames(segment, workId);
            resolvePronounsInScenes(segment, workCharacters);
            enrichSegmentWithWorkCharacters(segment, workId);
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
                StringBuilder descBuilder = new StringBuilder();
                if (character.getAppearance() != null && !character.getAppearance().isEmpty()) {
                    descBuilder.append(character.getAppearance());
                }
                if (character.getBodyType() != null && !character.getBodyType().isEmpty()) {
                    if (descBuilder.length() > 0) descBuilder.append(", ");
                    descBuilder.append("体型: ").append(character.getBodyType());
                }
                if (character.getFacialFeatures() != null && !character.getFacialFeatures().isEmpty()) {
                    if (descBuilder.length() > 0) descBuilder.append(", ");
                    descBuilder.append("面部: ").append(character.getFacialFeatures());
                }
                if (character.getClothingStyle() != null && !character.getClothingStyle().isEmpty()) {
                    if (descBuilder.length() > 0) descBuilder.append(", ");
                    descBuilder.append("服装: ").append(character.getClothingStyle());
                }
                if (character.getDistinguishingFeatures() != null && !character.getDistinguishingFeatures().isEmpty()) {
                    if (descBuilder.length() > 0) descBuilder.append(", ");
                    descBuilder.append("特征: ").append(character.getDistinguishingFeatures());
                }
                if (character.getDescription() != null && !character.getDescription().isEmpty()) {
                    if (descBuilder.length() > 0) descBuilder.append(", ");
                    descBuilder.append(character.getDescription());
                }
                characterAppearances.put(character.getName(), descBuilder.toString());
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
        return buildPromptWithWorkCharacters(text, style, targetAudience, null);
    }
    
    private String buildPromptWithWorkCharacters(String text, String style, String targetAudience, List<com.aigo.entity.CharacterEntity> workCharacters) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的动漫脚本分析师。请深度理解以下小说文本,并将其转换为动漫桥段。\n\n");
        
        if (workCharacters != null && !workCharacters.isEmpty()) {
            prompt.append("已知的角色信息（来自之前的集数）:\n");
            for (com.aigo.entity.CharacterEntity character : workCharacters) {
                prompt.append(String.format("- %s: %s, 外貌: %s\n", 
                    character.getName(), 
                    character.getDescription() != null ? character.getDescription() : "未知",
                    character.getAppearance() != null ? character.getAppearance() : "未知"));
            }
            prompt.append("\n");
        }
        
        prompt.append("请提取以下信息:\n");
        prompt.append("1. 角色信息 (姓名、描述、外貌、性格、性别、详细外观特征)\n");
        prompt.append("   - 姓名: 如果文本中明确提到角色姓名，使用该姓名；如果未提及姓名，使用占位符（男性用'未知男性'，女性用'未知女性'）\n");
        prompt.append("   - 描述: 角色的基本描述和背景\n");
        prompt.append("   - 外貌: 整体外貌特征\n");
        prompt.append("   - **体型**: 身高（高/中等/矮）、体型（瘦弱/苗条/匀称/健壮/丰满/肥胖等）\n");
        prompt.append("   - **面部特征**: 详细的五官描述（眼睛颜色和形状、鼻子、嘴唇、脸型等）\n");
        prompt.append("   - **服装风格**: 常穿的服装类型和风格\n");
        prompt.append("   - **显著特征**: 疤痕、纹身、配饰或其他独特标记\n");
        prompt.append("   - 性格: 性格特点\n");
        prompt.append("   - 性别: male/female/unknown\n");
        prompt.append("   **重要**: 如果文本中使用了代词（我、你、她、他、它），请根据上下文识别代词指代的具体角色名称\n");
        prompt.append("2. 场景分镜 - 重要: 每个角色的每句对话都应该是一个独立的场景,用于生成独立的漫画图片\n");
        prompt.append("   - 场景编号: 连续递增的数字\n");
        prompt.append("   - 角色: **必须使用具体的角色名称，不要使用代词（我、你、她、他、它）**\n");
        prompt.append("   - 如果文本中某句对话使用了代词，请根据上下文和已知角色信息，将代词替换为实际的角色名称\n");
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
        prompt.append("  \"characters\": [{\n");
        prompt.append("    \"name\": \"角色名或'未知男性'/'未知女性'\",\n");
        prompt.append("    \"description\": \"描述\",\n");
        prompt.append("    \"appearance\": \"整体外貌\",\n");
        prompt.append("    \"bodyType\": \"体型描述（身高+体型）\",\n");
        prompt.append("    \"facialFeatures\": \"详细面部特征\",\n");
        prompt.append("    \"clothingStyle\": \"服装风格\",\n");
        prompt.append("    \"distinguishingFeatures\": \"显著特征\",\n");
        prompt.append("    \"personality\": \"性格\",\n");
        prompt.append("    \"gender\": \"male/female/unknown\"\n");
        prompt.append("  }],\n");
        prompt.append("  \"scenes\": [\n");
        prompt.append("    {\"sceneNumber\": 1, \"character\": \"具体角色名（非代词）\", \"dialogue\": \"该角色说的话\", \"visualDescription\": \"画面描述\", \"atmosphere\": \"氛围\", \"action\": \"动作描述\"},\n");
        prompt.append("    {\"sceneNumber\": 2, \"character\": \"具体角色名（非代词）\", \"dialogue\": \"该角色说的话\", \"visualDescription\": \"画面描述\", \"atmosphere\": \"氛围\", \"action\": \"动作描述\"}\n");
        prompt.append("  ],\n");
        prompt.append("  \"plotSummary\": \"剧情总结\",\n");
        prompt.append("  \"genre\": \"类型\",\n");
        prompt.append("  \"mood\": \"情绪基调\"\n");
        prompt.append("}\n\n");
        prompt.append("注意事项:\n");
        prompt.append("1. 每个角色的每句对话都必须是一个独立的场景对象,这样才能为每句对话生成对应的漫画图片\n");
        prompt.append("2. **关键**: 场景中的character字段必须使用具体的角色名称，绝对不能使用代词（我、你、她、他、它）\n");
        prompt.append("3. 如果文本中角色用代词说话，请分析上下文确定是哪个角色，然后用该角色的真实名称\n");
        
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
        characters.add(new Character("主角", "故事的主人公", "年轻、充满活力", "勇敢、善良", "male", 
            "中等身高、匀称", "清秀的五官", "休闲装", "无"));
        characters.add(new Character("旁白", "叙述者", "无形", "客观", "neutral", 
            null, null, null, null));
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
    
    private void assignPlaceholderNames(AnimeSegment segment, String workId) {
        if (segment.getCharacters() == null || segment.getCharacters().isEmpty()) {
            return;
        }
        
        Map<String, Integer> genderCounters = new HashMap<>();
        genderCounters.put("male", 0);
        genderCounters.put("female", 0);
        genderCounters.put("unknown", 0);
        
        if (workId != null) {
            try {
                List<com.aigo.entity.CharacterEntity> existingChars = characterService.getCharactersByWorkId(workId);
                for (com.aigo.entity.CharacterEntity existingChar : existingChars) {
                    if (existingChar.getIsPlaceholderName() != null && existingChar.getIsPlaceholderName()) {
                        String name = existingChar.getName();
                        if (name.startsWith("男") && name.length() > 1) {
                            char suffix = name.charAt(1);
                            int index = suffix - 'a' + 1;
                            genderCounters.put("male", Math.max(genderCounters.get("male"), index));
                        } else if (name.startsWith("女") && name.length() > 1) {
                            char suffix = name.charAt(1);
                            int index = suffix - 'a' + 1;
                            genderCounters.put("female", Math.max(genderCounters.get("female"), index));
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("[NovelParseService] Failed to load existing placeholder characters", e);
            }
        }
        
        for (Character character : segment.getCharacters()) {
            String name = character.getName();
            if (name == null || name.isEmpty() || "未知男性".equals(name) || "未知女性".equals(name)) {
                String gender = character.getGender();
                if (gender == null || gender.isEmpty()) {
                    gender = "unknown";
                }
                
                int counter = genderCounters.get(gender);
                char suffix = (char) ('a' + counter);
                String placeholderName;
                
                if ("male".equalsIgnoreCase(gender)) {
                    placeholderName = "男" + suffix;
                } else if ("female".equalsIgnoreCase(gender)) {
                    placeholderName = "女" + suffix;
                } else {
                    placeholderName = "未知" + suffix;
                }
                
                character.setName(placeholderName);
                genderCounters.put(gender, counter + 1);
                
                logger.info("[NovelParseService] Assigned placeholder name '{}' to {} character", 
                    placeholderName, gender);
            }
        }
    }
    
    private void resolvePronounsInScenes(AnimeSegment segment, List<com.aigo.entity.CharacterEntity> workCharacters) {
        if (segment.getScenes() == null || segment.getScenes().isEmpty()) {
            return;
        }
        
        Set<String> pronouns = new HashSet<>(Arrays.asList("我", "你", "她", "他", "它"));
        Map<String, String> pronounToCharacter = new HashMap<>();
        
        if (workCharacters != null && !workCharacters.isEmpty()) {
            for (com.aigo.entity.CharacterEntity workChar : workCharacters) {
                if (workChar.getIsProtagonist() != null && workChar.getIsProtagonist()) {
                    pronounToCharacter.put("我", workChar.getName());
                    logger.info("[NovelParseService] Mapped pronoun '我' to protagonist '{}'", workChar.getName());
                }
                
                if (workChar.getGender() != null) {
                    if ("female".equalsIgnoreCase(workChar.getGender())) {
                        pronounToCharacter.putIfAbsent("她", workChar.getName());
                    } else if ("male".equalsIgnoreCase(workChar.getGender())) {
                        pronounToCharacter.putIfAbsent("他", workChar.getName());
                    }
                }
            }
        }
        
        if (segment.getCharacters() != null) {
            for (Character character : segment.getCharacters()) {
                if ("我".equals(character.getName()) || "主角".equals(character.getName()) || "主人公".equals(character.getName())) {
                    pronounToCharacter.putIfAbsent("我", character.getName());
                }
                
                if (character.getGender() != null) {
                    if ("female".equalsIgnoreCase(character.getGender())) {
                        pronounToCharacter.putIfAbsent("她", character.getName());
                    } else if ("male".equalsIgnoreCase(character.getGender())) {
                        pronounToCharacter.putIfAbsent("他", character.getName());
                    }
                }
            }
        }
        
        for (Scene scene : segment.getScenes()) {
            String characterName = scene.getCharacter();
            if (characterName != null && pronouns.contains(characterName)) {
                String resolvedName = pronounToCharacter.get(characterName);
                if (resolvedName != null) {
                    logger.info("[NovelParseService] Resolved pronoun '{}' to character '{}' in scene {}", 
                        characterName, resolvedName, scene.getSceneNumber());
                    scene.setCharacter(resolvedName);
                } else {
                    logger.warn("[NovelParseService] Could not resolve pronoun '{}' in scene {}, keeping as is", 
                        characterName, scene.getSceneNumber());
                }
            }
        }
    }
    
    private void enrichSegmentWithWorkCharacters(AnimeSegment segment, String workId) {
        if (workId == null || segment.getCharacters() == null) {
            return;
        }
        
        try {
            List<com.aigo.entity.CharacterEntity> workCharacters = characterService.getCharactersByWorkId(workId);
            Map<String, com.aigo.entity.CharacterEntity> workCharacterMap = new HashMap<>();
            for (com.aigo.entity.CharacterEntity workChar : workCharacters) {
                workCharacterMap.put(workChar.getName(), workChar);
            }
            
            for (Character character : segment.getCharacters()) {
                com.aigo.entity.CharacterEntity existingChar = workCharacterMap.get(character.getName());
                if (existingChar != null) {
                    if (existingChar.getAppearance() != null && !existingChar.getAppearance().isEmpty()) {
                        character.setAppearance(existingChar.getAppearance());
                    }
                    if (existingChar.getDescription() != null && !existingChar.getDescription().isEmpty()) {
                        character.setDescription(existingChar.getDescription());
                    }
                    if (existingChar.getPersonality() != null && !existingChar.getPersonality().isEmpty()) {
                        character.setPersonality(existingChar.getPersonality());
                    }
                    if (existingChar.getGender() != null && !existingChar.getGender().isEmpty()) {
                        character.setGender(existingChar.getGender());
                    }
                    if (existingChar.getBodyType() != null && !existingChar.getBodyType().isEmpty()) {
                        character.setBodyType(existingChar.getBodyType());
                    }
                    if (existingChar.getFacialFeatures() != null && !existingChar.getFacialFeatures().isEmpty()) {
                        character.setFacialFeatures(existingChar.getFacialFeatures());
                    }
                    if (existingChar.getClothingStyle() != null && !existingChar.getClothingStyle().isEmpty()) {
                        character.setClothingStyle(existingChar.getClothingStyle());
                    }
                    if (existingChar.getDistinguishingFeatures() != null && !existingChar.getDistinguishingFeatures().isEmpty()) {
                        character.setDistinguishingFeatures(existingChar.getDistinguishingFeatures());
                    }
                    logger.info("[NovelParseService] Enriched character '{}' with existing work data", character.getName());
                }
            }
        } catch (Exception e) {
            logger.warn("[NovelParseService] Failed to enrich segment with work characters", e);
        }
    }
    
    public Map<String, List<String>> detectCharacterNicknames(String text, List<Character> characters) {
        if ("demo-key".equals(apiKey) || characters == null || characters.isEmpty()) {
            return new HashMap<>();
        }
        
        logger.info("[NovelParseService] Detecting character nicknames from text");
        
        try {
            StringBuilder characterInfo = new StringBuilder();
            for (Character character : characters) {
                characterInfo.append("- ").append(character.getName());
                if (character.getGender() != null) {
                    characterInfo.append("(").append(character.getGender()).append(")");
                }
                characterInfo.append("\n");
            }
            
            String prompt = String.format("""
                请分析以下小说文本，识别角色的昵称和别称关系。
                
                文本内容：
                %s
                
                已识别的角色列表：
                %s
                
                请找出文本中所有对角色的不同称呼方式，包括代词（我、你、他、她、它）、昵称、别名等。
                
                要求：
                1. 分析哪些称呼指向同一个角色
                2. 将代词和昵称映射到具体的角色名称
                3. 只返回有明确关联的称呼关系
                
                请以纯JSON格式返回，格式如下：
                {
                  "小王": ["我", "老王", "王哥"],
                  "李明": ["他", "小李"]
                }
                
                注意：
                - 键是角色的主要名称（从角色列表中选择）
                - 值是该角色的所有昵称/别称数组
                - 如果某个代词可能指向多个角色，选择最可能的一个
                - 如果无法确定，不要包含该称呼
                - 只返回JSON，不要有其他文字
                """,
                text,
                characterInfo.toString()
            );
            
            ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(30))
                .maxRetries(2)
                .build();
            
            String response = model.generate(prompt);
            logger.info("[NovelParseService] Nickname detection response: {}", response);
            
            String jsonContent = extractJsonFromResponse(response);
            Map<String, List<String>> nicknameMap = objectMapper.readValue(
                jsonContent, 
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, List.class)
            );
            
            logger.info("[NovelParseService] Detected {} characters with nicknames", nicknameMap.size());
            return nicknameMap;
            
        } catch (Exception e) {
            logger.error("[NovelParseService] Failed to detect character nicknames", e);
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
