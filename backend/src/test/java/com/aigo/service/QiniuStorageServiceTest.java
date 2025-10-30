package com.aigo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QiniuStorageServiceTest {

    private QiniuStorageService qiniuStorageService;

    @BeforeEach
    void setUp() {
        qiniuStorageService = new QiniuStorageService();
        ReflectionTestUtils.setField(qiniuStorageService, "accessKey", "demo-access-key");
        ReflectionTestUtils.setField(qiniuStorageService, "secretKey", "demo-secret-key");
        ReflectionTestUtils.setField(qiniuStorageService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(qiniuStorageService, "domain", "https://cdn.example.com");
    }

    @Test
    void testUploadImageFromUrl_DemoMode() {
        String imageUrl = "http://example.com/image.jpg";
        String workId = "work123";

        String result = qiniuStorageService.uploadImageFromUrl(imageUrl, workId);

        assertNotNull(result);
        assertTrue(result.contains("http") || result.equals(imageUrl));
    }

    @Test
    void testUploadAudioFromUrl_DemoMode() {
        String audioUrl = "http://example.com/audio.mp3";
        String characterName = "小明";

        String result = qiniuStorageService.uploadAudioFromUrl(audioUrl, characterName);

        assertNotNull(result);
        assertTrue(result.contains("http") || result.equals(audioUrl));
    }

    @Test
    void testUploadImageFromUrl_NullWorkId() {
        String imageUrl = "http://example.com/image.jpg";
        String workId = null;

        String result = qiniuStorageService.uploadImageFromUrl(imageUrl, workId);

        assertNotNull(result);
    }

    @Test
    void testUploadAudioFromUrl_NullCharacterName() {
        String audioUrl = "http://example.com/audio.mp3";
        String characterName = null;

        String result = qiniuStorageService.uploadAudioFromUrl(audioUrl, characterName);

        assertNotNull(result);
    }

    @Test
    void testUploadImageFromUrl_EmptyUrl() {
        String imageUrl = "";
        String workId = "work123";

        assertDoesNotThrow(() -> {
            qiniuStorageService.uploadImageFromUrl(imageUrl, workId);
        });
    }

    @Test
    void testUploadAudioFromUrl_EmptyUrl() {
        String audioUrl = "";
        String characterName = "小明";

        assertDoesNotThrow(() -> {
            qiniuStorageService.uploadAudioFromUrl(audioUrl, characterName);
        });
    }
}
