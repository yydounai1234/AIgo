package com.aigo.service;

import com.aigo.entity.Episode;
import com.aigo.model.AnimeSegment;
import com.aigo.model.Character;
import com.aigo.model.Scene;
import com.aigo.repository.EpisodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EpisodeAsyncServiceTest {

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private NovelParseService novelParseService;

    @Mock
    private CharacterService characterService;

    @Mock
    private SceneService sceneService;

    @Mock
    private VideoGenerationService videoGenerationService;

    @InjectMocks
    private EpisodeAsyncService episodeAsyncService;

    private Episode testEpisode;
    private AnimeSegment animeSegment;

    @BeforeEach
    void setUp() {
        testEpisode = Episode.builder()
                .id("episode1")
                .workId("work1")
                .episodeNumber(1)
                .title("第一集")
                .novelText("小说文本")
                .style("动漫风格")
                .targetAudience("青少年")
                .status("PENDING")
                .build();

        Character character = new Character();
        character.setName("小明");
        character.setGender("男");
        character.setDescription("主角");

        Scene scene = new Scene();
        scene.setSceneNumber(1);
        scene.setLocation("公园");
        scene.setDialogue("你好");
        scene.setDescription("场景描述");

        animeSegment = new AnimeSegment();
        animeSegment.setCharacters(Arrays.asList(character));
        animeSegment.setScenes(Arrays.asList(scene));
    }

    @Test
    void testProcessEpisodeAsync_Success() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(novelParseService.parseNovelTextWithWorkId(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(animeSegment);
        when(episodeRepository.save(any(Episode.class))).thenReturn(testEpisode);

        episodeAsyncService.processEpisodeAsync("episode1", "小说文本");

        verify(episodeRepository, timeout(5000).atLeastOnce()).save(any(Episode.class));
    }

    @Test
    void testProcessEpisodeAsync_EpisodeNotFound() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.empty());

        episodeAsyncService.processEpisodeAsync("episode1", "小说文本");

        verify(novelParseService, never()).parseNovelTextWithWorkId(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessEpisodeAsync_NullNovelText() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));

        episodeAsyncService.processEpisodeAsync("episode1", null);

        verify(episodeRepository, timeout(5000).atLeastOnce()).findById("episode1");
    }

    @Test
    void testProcessEpisodeAsync_EmptyNovelText() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));

        episodeAsyncService.processEpisodeAsync("episode1", "");

        verify(episodeRepository, timeout(5000).atLeastOnce()).findById("episode1");
    }

    @Test
    void testProcessEpisodeAsync_ParseServiceThrowsException() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(novelParseService.parseNovelTextWithWorkId(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("解析失败"));
        when(episodeRepository.save(any(Episode.class))).thenReturn(testEpisode);

        episodeAsyncService.processEpisodeAsync("episode1", "小说文本");

        verify(episodeRepository, timeout(5000).atLeastOnce()).save(argThat(episode -> 
            "FAILED".equals(episode.getStatus())
        ));
    }
}
