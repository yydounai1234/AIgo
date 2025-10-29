package com.aigo.dto.episode;

import com.aigo.entity.Episode;
import com.aigo.model.Character;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeResponse {
    private String id;
    private String workId;
    private Integer episodeNumber;
    private String title;
    private String novelText;
    private List<Episode.SceneData> scenes;
    private Boolean isFree;
    private Integer coinPrice;
    private Boolean isPublished;
    private String status;
    private List<Character> characters;
    private String plotSummary;
    private String genre;
    private String mood;
    private String errorMessage;
    private String style;
    private String targetAudience;
    private LocalDateTime createdAt;
    private String authorName;
    private String authorAvatar;
    
    public static EpisodeResponse fromEntity(Episode episode) {
        return EpisodeResponse.builder()
                .id(episode.getId())
                .workId(episode.getWorkId())
                .episodeNumber(episode.getEpisodeNumber())
                .title(episode.getTitle())
                .novelText(episode.getNovelText())
                .scenes(episode.getScenes())
                .isFree(episode.getIsFree())
                .coinPrice(episode.getCoinPrice())
                .isPublished(episode.getIsPublished())
                .status(episode.getStatus())
                .characters(episode.getCharacters())
                .plotSummary(episode.getPlotSummary())
                .genre(episode.getGenre())
                .mood(episode.getMood())
                .errorMessage(episode.getErrorMessage())
                .style(episode.getStyle())
                .targetAudience(episode.getTargetAudience())
                .createdAt(episode.getCreatedAt())
                .authorName(episode.getWork() != null && episode.getWork().getUser() != null ? episode.getWork().getUser().getUsername() : null)
                .authorAvatar(episode.getWork() != null && episode.getWork().getUser() != null ? episode.getWork().getUser().getAvatarUrl() : null)
                .build();
    }
}
