package com.aigo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TextToImageServiceTest {

    @Mock
    private QiniuStorageService qiniuStorageService;

    private TextToImageService textToImageService;

    @BeforeEach
    void setUp() {
        textToImageService = new TextToImageService(qiniuStorageService);
        ReflectionTestUtils.setField(textToImageService, "apiKey", "demo-key");
        ReflectionTestUtils.setField(textToImageService, "baseUrl", "https://api.example.com");
    }

    @Test
    void testGenerateImageUrl_DemoMode() {
        String description = "一个美丽的风景";
        String workId = "work123";

        when(qiniuStorageService.uploadImageFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/image.jpg");

        String result = textToImageService.generateImageUrl(description, workId);

        assertNotNull(result);
        assertTrue(result.contains("http"));
    }

    @Test
    void testGenerateImageUrl_EmptyDescription() {
        String description = "";
        String workId = "work123";

        when(qiniuStorageService.uploadImageFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/image.jpg");

        String result = textToImageService.generateImageUrl(description, workId);

        assertNotNull(result);
    }

    @Test
    void testGenerateImageUrl_NullWorkId() {
        String description = "测试描述";
        String workId = null;

        when(qiniuStorageService.uploadImageFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/image.jpg");

        String result = textToImageService.generateImageUrl(description, workId);

        assertNotNull(result);
    }

    @Test
    void testGenerateImageUrl_LongDescription() {
        String description = "这是一个很长的描述".repeat(50);
        String workId = "work123";

        when(qiniuStorageService.uploadImageFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/image.jpg");

        String result = textToImageService.generateImageUrl(description, workId);

        assertNotNull(result);
    }
}
