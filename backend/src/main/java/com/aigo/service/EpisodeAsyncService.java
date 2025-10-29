package com.aigo.service;

import com.aigo.entity.Episode;
import com.aigo.entity.Work;
import com.aigo.model.AnimeSegment;
import com.aigo.repository.EpisodeRepository;
import com.aigo.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EpisodeAsyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(EpisodeAsyncService.class);
    
    private final EpisodeRepository episodeRepository;
    private final WorkRepository workRepository;
    private final NovelParseService novelParseService;
    private final CharacterService characterService;
    private final VideoGenerationService videoGenerationService;
    
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
            
            Work work = workRepository.findById(episode.getWorkId()).orElse(null);
            if (work == null) {
                throw new RuntimeException("Work not found for episode: " + episodeId);
            }
            
            boolean isVideoContent = "video".equalsIgnoreCase(work.getContentType());
            
            AnimeSegment segment = novelParseService.parseNovelTextWithWorkId(
                novelText, 
                episode.getStyle(), 
                episode.getTargetAudience(),
                episode.getWorkId()
            );
            
            java.util.Map<String, java.util.List<String>> nicknameMap = new java.util.HashMap<>();
            if (segment.getCharacters() != null && !segment.getCharacters().isEmpty()) {
                try {
                    nicknameMap = novelParseService.detectCharacterNicknames(novelText, segment.getCharacters());
                    logger.info("[EpisodeAsyncService] Detected nicknames for {} characters", nicknameMap.size());
                } catch (Exception e) {
                    logger.warn("[EpisodeAsyncService] Failed to detect nicknames, continuing without them", e);
                }
                
                for (com.aigo.model.Character character : segment.getCharacters()) {
                    boolean isProtagonist = "我".equals(character.getName()) || 
                                           "主角".equals(character.getName()) ||
                                           "主人公".equals(character.getName());
                    boolean isPlaceholder = character.getName().matches("^[男女未知][a-z]$");
                    
                    java.util.List<String> nicknames = nicknameMap.get(character.getName());
                    
                    characterService.createOrUpdateWorkCharacter(
                        episode.getWorkId(),
                        character.getName(),
                        character.getDescription(),
                        character.getAppearance(),
                        character.getPersonality(),
                        character.getGender(),
                        isProtagonist,
                        character.getBodyType(),
                        character.getFacialFeatures(),
                        character.getClothingStyle(),
                        character.getDistinguishingFeatures(),
                        isPlaceholder,
                        nicknames
                    );
                }
            }
            
            episode.setCharacters(segment.getCharacters());
            episode.setScenes(segment.getScenes().stream()
                    .map(scene -> new Episode.SceneData(
                            scene.getSceneNumber(),
                            scene.getDialogue(),
                            scene.getImageUrl(),
                            scene.getAudioUrl()))
                    .toList());
            episode.setPlotSummary(segment.getPlotSummary());
            episode.setGenre(segment.getGenre());
            episode.setMood(segment.getMood());
            
            if (isVideoContent) {
                logger.info("[EpisodeAsyncService] Generating video for episode {}", episodeId);
                try {
                    String videoUrl = generateVideoFromSegment(segment, episode.getWorkId());
                    episode.setVideoUrl(videoUrl);
                    logger.info("[EpisodeAsyncService] Video generated successfully: {}", videoUrl);
                } catch (Exception e) {
                    logger.error("[EpisodeAsyncService] Video generation failed, but keeping anime scenes", e);
                }
            }
            
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
    
    private String generateVideoFromSegment(AnimeSegment segment, String workId) {
        if (segment.getScenes() == null || segment.getScenes().isEmpty()) {
            throw new RuntimeException("No scenes available for video generation");
        }
        
        String baseImageUrl = null;
        if (segment.getScenes().get(0).getImageUrl() != null) {
            baseImageUrl = segment.getScenes().get(0).getImageUrl();
        }
        
        if (baseImageUrl == null) {
            throw new RuntimeException("No base image available for video generation");
        }
        
        java.util.List<String> scenePrompts = segment.getScenes().stream()
            .map(scene -> {
                StringBuilder prompt = new StringBuilder();
                if (scene.getVisualDescription() != null && !scene.getVisualDescription().isEmpty()) {
                    prompt.append(scene.getVisualDescription());
                }
                if (scene.getDialogue() != null && !scene.getDialogue().isEmpty()) {
                    if (prompt.length() > 0) prompt.append(" ");
                    prompt.append("对话: ").append(scene.getDialogue());
                }
                if (scene.getAtmosphere() != null && !scene.getAtmosphere().isEmpty()) {
                    if (prompt.length() > 0) prompt.append(" ");
                    prompt.append("氛围: ").append(scene.getAtmosphere());
                }
                return prompt.toString();
            })
            .collect(Collectors.toList());
        
        return videoGenerationService.generateVideoFromScenes(scenePrompts, baseImageUrl);
    }
}
