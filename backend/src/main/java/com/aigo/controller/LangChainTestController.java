package com.aigo.controller;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/langchain")
public class LangChainTestController {

    @Value("${openai.api.key:demo-key}")
    private String openaiApiKey;

    @GetMapping("/test")
    public Map<String, String> testLangChain() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ready");
        response.put("message", "LangChain4j is integrated and ready to use");
        response.put("note", "Please configure openai.api.key in application.properties to use AI features");
        return response;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Message cannot be empty");
            return error;
        }

        Map<String, String> response = new HashMap<>();
        
        if ("demo-key".equals(openaiApiKey)) {
            response.put("response", "Demo mode: Echo - " + userMessage);
            response.put("note", "Configure openai.api.key to enable real AI chat");
        } else {
            try {
                ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(openaiApiKey)
                    .build();
                
                String aiResponse = model.generate(userMessage);
                response.put("response", aiResponse);
            } catch (Exception e) {
                response.put("error", "Failed to get AI response: " + e.getMessage());
            }
        }
        
        return response;
    }
}
