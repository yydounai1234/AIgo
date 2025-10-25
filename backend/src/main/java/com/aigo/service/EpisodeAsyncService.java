package com.aigo.service;

import com.aigo.entity.Episode;
import com.aigo.model.AnimeSegment;
import com.aigo.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EpisodeAsyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(EpisodeAsyncService.class);
    
    private final EpisodeRepository episodeRepository;
    private final NovelParseService novelParseService;
    
    @Async
    public void processEpisodeAsync(String episodeId, String novelText) {
        logger.info("[EpisodeAsyncService] ========== Starting async processing ==========");
        logger.info("[EpisodeAsyncService] Episode ID: {}", episodeId);
        logger.info("[EpisodeAsyncService] Novel text length: {}", novelText != null ? novelText.length() : 0);
        logger.info("[EpisodeAsyncService] Novel text content: {}", novelText);
        logger.info("[EpisodeAsyncService] ================================================");
        
        try {
            Episode episode = episodeRepository.findById(episodeId).orElse(null);
            if (episode == null) {
                logger.error("[EpisodeAsyncService] Episode not found: {}", episodeId);
                return;
            }
            
            episode.setStatus("PROCESSING");
            episodeRepository.save(episode);
            
            AnimeSegment segment = novelParseService.parseNovelText(
                novelText, 
                episode.getStyle(), 
                episode.getTargetAudience()
            );
            
            episode.setCharacters(segment.getCharacters());
            episode.setScenes(segment.getScenes().stream()
                    .map(scene -> new Episode.SceneData(
                            scene.getSceneNumber(),
                            scene.getDialogue(),
                            scene.getImageUrl()))
                    .toList());
            episode.setPlotSummary(segment.getPlotSummary());
            episode.setGenre(segment.getGenre());
            episode.setMood(segment.getMood());
            episode.setStatus("SUCCESS");
            
            episodeRepository.save(episode);
            logger.info("[EpisodeAsyncService] Episode {} processed successfully", episodeId);
            
        } catch (Exception e) {
            logger.error("[EpisodeAsyncService] Failed to process episode " + episodeId, e);
            
            Episode episode = episodeRepository.findById(episodeId).orElse(null);
            if (episode != null) {
                episode.setStatus("FAILED");
                episode.setErrorMessage(e.getMessage());
                episodeRepository.save(episode);
            }
        }
    }
}
