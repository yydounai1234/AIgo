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
class TextToSpeechServiceTest {

    @Mock
    private QiniuStorageService qiniuStorageService;

    private TextToSpeechService textToSpeechService;

    @BeforeEach
    void setUp() {
        textToSpeechService = new TextToSpeechService(qiniuStorageService);
        ReflectionTestUtils.setField(textToSpeechService, "apiKey", "demo-key");
        ReflectionTestUtils.setField(textToSpeechService, "baseUrl", "https://api.example.com");
    }

    @Test
    void testGenerateAudioUrl_DemoMode() {
        String text = "你好，世界";
        String characterName = "小明";

        when(qiniuStorageService.uploadAudioFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/audio.mp3");

        String result = textToSpeechService.generateAudioUrl(text, characterName);

        assertNotNull(result);
        assertTrue(result.contains("mp3") || result.contains("audio"));
    }

    @Test
    void testGenerateAudioUrl_EmptyText() {
        String text = "";
        String characterName = "小红";

        when(qiniuStorageService.uploadAudioFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/audio.mp3");

        String result = textToSpeechService.generateAudioUrl(text, characterName);

        assertNotNull(result);
    }

    @Test
    void testGenerateAudioUrl_NullCharacterName() {
        String text = "测试文本";
        String characterName = null;

        when(qiniuStorageService.uploadAudioFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/audio.mp3");

        String result = textToSpeechService.generateAudioUrl(text, characterName);

        assertNotNull(result);
    }

    @Test
    void testGenerateAudioUrl_MaleCharacter() {
        String text = "我是男性角色";
        String characterName = "王先生";

        when(qiniuStorageService.uploadAudioFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/audio.mp3");

        String result = textToSpeechService.generateAudioUrl(text, characterName);

        assertNotNull(result);
    }

    @Test
    void testGenerateAudioUrl_FemaleCharacter() {
        String text = "我是女性角色";
        String characterName = "李女士";

        when(qiniuStorageService.uploadAudioFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/audio.mp3");

        String result = textToSpeechService.generateAudioUrl(text, characterName);

        assertNotNull(result);
    }

    @Test
    void testGenerateAudioUrl_LongText() {
        String text = "这是一段很长的文本".repeat(100);
        String characterName = "旁白";

        when(qiniuStorageService.uploadAudioFromUrl(anyString(), anyString()))
                .thenReturn("http://cdn.example.com/audio.mp3");

        String result = textToSpeechService.generateAudioUrl(text, characterName);

        assertNotNull(result);
    }
}
