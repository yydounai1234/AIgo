package com.aigo.service;

import com.aigo.model.AnimeSegment;
import com.aigo.model.Character;
import com.aigo.model.Scene;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NovelParseService {
    
    private static final Logger logger = LoggerFactory.getLogger(NovelParseService.class);
    
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.base.url}")
    private String baseUrl;
    
    @Value("${deepseek.model.name}")
    private String modelName;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AnimeSegment parseNovelText(String text, String style, String targetAudience) {
        if ("demo-key".equals(apiKey)) {
            return createDemoResponse(text);
        }
        
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .build();
            
            String prompt = buildPrompt(text, style, targetAudience);
            String response = model.generate(prompt);
            
            return parseResponse(response);
            
        } catch (Exception e) {
            logger.error("Failed to parse novel text with DeepSeek", e);
            throw new RuntimeException("LLM 处理失败: " + e.getMessage(), e);
        }
    }
    
    private String buildPrompt(String text, String style, String targetAudience) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的动漫脚本分析师。请深度理解以下小说文本,并将其转换为动漫桥段。\n\n");
        prompt.append("请提取以下信息:\n");
        prompt.append("1. 角色信息 (姓名、描述、外貌、性格)\n");
        prompt.append("2. 场景分镜 (场景编号、画面描述、氛围、对话、动作)\n");
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
        prompt.append("  \"characters\": [{\"name\": \"角色名\", \"description\": \"描述\", \"appearance\": \"外貌\", \"personality\": \"性格\"}],\n");
        prompt.append("  \"scenes\": [{\"sceneNumber\": 1, \"visualDescription\": \"画面描述\", \"atmosphere\": \"氛围\", \"dialogues\": [\"对话1\", \"对话2\"], \"action\": \"动作描述\"}],\n");
        prompt.append("  \"plotSummary\": \"剧情总结\",\n");
        prompt.append("  \"genre\": \"类型\",\n");
        prompt.append("  \"mood\": \"情绪基调\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    private AnimeSegment parseResponse(String response) {
        try {
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonContent = response.substring(jsonStart, jsonEnd);
                return objectMapper.readValue(jsonContent, AnimeSegment.class);
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
        characters.add(new Character("主角", "故事的主人公", "年轻、充满活力", "勇敢、善良"));
        segment.setCharacters(characters);
        
        List<Scene> scenes = new ArrayList<>();
        scenes.add(new Scene(1, "清晨的城市街道,阳光洒在街道上", "宁静、温暖", 
            List.of("主角: 新的一天开始了!"), "主角走在街道上"));
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
}
