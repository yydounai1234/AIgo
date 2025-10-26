package com.aigo.dto.episode;

import com.aigo.entity.Episode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeListItem {
    private String id;
    private Integer episodeNumber;
    private String title;
    private String novelText;
    private Boolean isFree;
    private Integer coinPrice;
    private Boolean isPublished;
    private Boolean isPurchased;
    private List<Episode.SceneData> scenes;
    
    public static EpisodeListItem fromEntity(Episode episode) {
        return EpisodeListItem.builder()
                .id(episode.getId())
                .episodeNumber(episode.getEpisodeNumber())
                .title(episode.getTitle())
                .novelText(episode.getNovelText())
                .isFree(episode.getIsFree())
                .coinPrice(episode.getCoinPrice())
                .isPublished(episode.getIsPublished())
                .isPurchased(false)
                .scenes(episode.getScenes())
                .build();
    }
}
