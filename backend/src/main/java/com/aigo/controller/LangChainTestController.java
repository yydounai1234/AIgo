package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import com.aigo.dto.ErrorCode;
import com.aigo.exception.BusinessException;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/langchain")
public class LangChainTestController {

    @Value("${openai.api.key:demo-key}")
    private String openaiApiKey;

    @GetMapping("/test")
    public ApiResponse<LangChainStatus> testLangChain() {
        LangChainStatus status = new LangChainStatus();
        status.setStatus("ready");
        status.setMessage("LangChain4j is integrated and ready to use");
        status.setNote("Please configure openai.api.key in application.properties to use AI features");
        return ApiResponse.success(status);
    }

    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Message cannot be empty");
        }

        ChatResponse chatResponse = new ChatResponse();
        
        if ("demo-key".equals(openaiApiKey)) {
            chatResponse.setResponse("Demo mode: Echo - " + request.getMessage());
            chatResponse.setNote("Configure openai.api.key to enable real AI chat");
        } else {
            try {
                ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(openaiApiKey)
                    .timeout(Duration.ofSeconds(60))
                    .maxRetries(3)
                    .build();
                
                String aiResponse = model.generate(request.getMessage());
                chatResponse.setResponse(aiResponse);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to get AI response: " + e.getMessage());
            }
        }
        
        return ApiResponse.success(chatResponse);
    }
    
    @Data
    public static class LangChainStatus {
        private String status;
        private String message;
        private String note;
    }
    
    @Data
    public static class ChatRequest {
        private String message;
    }
    
    @Data
    public static class ChatResponse {
        private String response;
        private String note;
    }
}
