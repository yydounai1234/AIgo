package com.aigo.dto.episode;

import com.aigo.entity.Episode;
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
    private LocalDateTime createdAt;
    
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
                .createdAt(episode.getCreatedAt())
                .build();
    }
}
