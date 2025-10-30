package com.aigo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VideoGenerationServiceTest {

    @Mock
    private QiniuStorageService qiniuStorageService;

    private VideoGenerationService videoGenerationService;

    @BeforeEach
    void setUp() {
        videoGenerationService = new VideoGenerationService(qiniuStorageService);
        ReflectionTestUtils.setField(videoGenerationService, "apiKey", "demo-key");
        ReflectionTestUtils.setField(videoGenerationService, "baseUrl", "https://api.qnaigc.com");
        ReflectionTestUtils.setField(videoGenerationService, "modelName", "veo-3.0-fast-generate-001");
    }

    @Test
    void testGenerateVideoFromImageAndPrompt_DemoMode() {
        String baseImageUrl = "http://example.com/image.jpg";
        String prompt = "生成一个动画视频";

        String result = videoGenerationService.generateVideoFromImageAndPrompt(baseImageUrl, prompt);

        assertNotNull(result);
        assertTrue(result.contains("mp4") || result.contains("video"));
    }

    @Test
    void testGenerateVideoFromImageAndPrompt_WithLongPrompt() {
        String baseImageUrl = "http://example.com/image.jpg";
        String prompt = "这是一个非常长的提示词，包含很多细节描述，用于测试视频生成功能。".repeat(10);

        String result = videoGenerationService.generateVideoFromImageAndPrompt(baseImageUrl, prompt);

        assertNotNull(result);
    }

    @Test
    void testGenerateVideoFromImageAndPrompt_WithEmptyPrompt() {
        String baseImageUrl = "http://example.com/image.jpg";
        String prompt = "";

        String result = videoGenerationService.generateVideoFromImageAndPrompt(baseImageUrl, prompt);

        assertNotNull(result);
    }

    @Test
    void testGenerateVideoFromImageAndPrompt_WithNullImageUrl() {
        String baseImageUrl = null;
        String prompt = "生成视频";

        assertDoesNotThrow(() -> {
            videoGenerationService.generateVideoFromImageAndPrompt(baseImageUrl, prompt);
        });
    }
}
