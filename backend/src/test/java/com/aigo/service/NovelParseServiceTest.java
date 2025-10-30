package com.aigo.service;

import com.aigo.model.AnimeSegment;
import com.aigo.model.Character;
import com.aigo.model.Scene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NovelParseServiceTest {

    @Mock
    private TextToImageService textToImageService;

    @Mock
    private TextToSpeechService textToSpeechService;

    @Mock
    private CharacterService characterService;

    @InjectMocks
    private NovelParseService novelParseService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(novelParseService, "apiKey", "demo-key");
        ReflectionTestUtils.setField(novelParseService, "baseUrl", "https://api.example.com");
        ReflectionTestUtils.setField(novelParseService, "modelName", "deepseek-chat");
    }

    @Test
    void testParseNovelText_DemoMode() {
        String text = "小明和小红在公园里散步。";
        String style = "动漫风格";
        String targetAudience = "青少年";

        when(textToImageService.generateImageUrl(anyString(), anyString())).thenReturn("http://example.com/image.jpg");
        when(textToSpeechService.generateAudioUrl(anyString(), anyString())).thenReturn("http://example.com/audio.mp3");

        AnimeSegment result = novelParseService.parseNovelText(text, style, targetAudience);

        assertNotNull(result);
        assertNotNull(result.getCharacters());
        assertNotNull(result.getScenes());
        assertFalse(result.getCharacters().isEmpty());
        assertFalse(result.getScenes().isEmpty());
        
        verify(textToImageService, atLeastOnce()).generateImageUrl(anyString(), anyString());
        verify(textToSpeechService, atLeastOnce()).generateAudioUrl(anyString(), anyString());
    }

    @Test
    void testParseNovelText_EmptyText() {
        String text = "";
        String style = "动漫风格";
        String targetAudience = "青少年";

        when(textToImageService.generateImageUrl(anyString(), anyString())).thenReturn("http://example.com/image.jpg");
        when(textToSpeechService.generateAudioUrl(anyString(), anyString())).thenReturn("http://example.com/audio.mp3");

        AnimeSegment result = novelParseService.parseNovelText(text, style, targetAudience);

        assertNotNull(result);
    }

    @Test
    void testParseNovelText_NullStyle() {
        String text = "测试文本";
        String style = null;
        String targetAudience = "青少年";

        when(textToImageService.generateImageUrl(anyString(), anyString())).thenReturn("http://example.com/image.jpg");
        when(textToSpeechService.generateAudioUrl(anyString(), anyString())).thenReturn("http://example.com/audio.mp3");

        AnimeSegment result = novelParseService.parseNovelText(text, style, targetAudience);

        assertNotNull(result);
    }

    @Test
    void testParseNovelText_WithWorkId() {
        String text = "小明和小红在公园里散步。";
        String style = "动漫风格";
        String targetAudience = "青少年";
        String workId = "work123";

        when(characterService.getCharactersByWorkId(workId)).thenReturn(Arrays.asList());
        when(textToImageService.generateImageUrl(anyString(), anyString())).thenReturn("http://example.com/image.jpg");
        when(textToSpeechService.generateAudioUrl(anyString(), anyString())).thenReturn("http://example.com/audio.mp3");

        AnimeSegment result = novelParseService.parseNovelTextWithWorkId(text, style, targetAudience, workId);

        assertNotNull(result);
        verify(characterService).getCharactersByWorkId(workId);
    }

    @Test
    void testParseNovelText_DemoModeCharactersGenerated() {
        String text = "小明、小红和小李在公园里玩耍。";
        String style = "动漫风格";
        String targetAudience = "儿童";

        when(textToImageService.generateImageUrl(anyString(), anyString())).thenReturn("http://example.com/image.jpg");
        when(textToSpeechService.generateAudioUrl(anyString(), anyString())).thenReturn("http://example.com/audio.mp3");

        AnimeSegment result = novelParseService.parseNovelText(text, style, targetAudience);

        assertNotNull(result);
        assertNotNull(result.getCharacters());
        assertTrue(result.getCharacters().size() > 0);
        
        for (Character character : result.getCharacters()) {
            assertNotNull(character.getName());
            assertNotNull(character.getDescription());
        }
    }

    @Test
    void testParseNovelText_DemoModeScenesGenerated() {
        String text = "他们在公园里玩耍，非常开心。";
        String style = "动漫风格";
        String targetAudience = "青少年";

        when(textToImageService.generateImageUrl(anyString(), anyString())).thenReturn("http://example.com/image.jpg");
        when(textToSpeechService.generateAudioUrl(anyString(), anyString())).thenReturn("http://example.com/audio.mp3");

        AnimeSegment result = novelParseService.parseNovelText(text, style, targetAudience);

        assertNotNull(result);
        assertNotNull(result.getScenes());
        assertTrue(result.getScenes().size() > 0);
        
        for (Scene scene : result.getScenes()) {
            assertNotNull(scene.getDescription());
            assertTrue(scene.getSceneNumber() > 0);
        }
    }

    @Test
    void testParseNovelText_ImageGenerationCalled() {
        String text = "测试文本";
        String style = "动漫风格";
        String targetAudience = "青少年";

        when(textToImageService.generateImageUrl(anyString(), anyString()))
                .thenReturn("http://example.com/image1.jpg")
                .thenReturn("http://example.com/image2.jpg");
        when(textToSpeechService.generateAudioUrl(anyString(), anyString()))
                .thenReturn("http://example.com/audio.mp3");

        AnimeSegment result = novelParseService.parseNovelText(text, style, targetAudience);

        assertNotNull(result);
        verify(textToImageService, atLeastOnce()).generateImageUrl(anyString(), anyString());
    }

    @Test
    void testParseNovelText_AudioGenerationCalled() {
        String text = "测试文本";
        String style = "动漫风格";
        String targetAudience = "青少年";

        when(textToImageService.generateImageUrl(anyString(), anyString()))
                .thenReturn("http://example.com/image.jpg");
        when(textToSpeechService.generateAudioUrl(anyString(), anyString()))
                .thenReturn("http://example.com/audio1.mp3")
                .thenReturn("http://example.com/audio2.mp3");

        AnimeSegment result = novelParseService.parseNovelText(text, style, targetAudience);

        assertNotNull(result);
        verify(textToSpeechService, atLeastOnce()).generateAudioUrl(anyString(), anyString());
    }
}
