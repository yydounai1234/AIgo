package com.aigo.dto.episode;

import com.aigo.entity.Episode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeListItem {
    private String id;
    private Integer episodeNumber;
    private String title;
    private Boolean isFree;
    private Integer coinPrice;
    private Boolean isPublished;
    
    public static EpisodeListItem fromEntity(Episode episode) {
        return EpisodeListItem.builder()
                .id(episode.getId())
                .episodeNumber(episode.getEpisodeNumber())
                .title(episode.getTitle())
                .isFree(episode.getIsFree())
                .coinPrice(episode.getCoinPrice())
                .isPublished(episode.getIsPublished())
                .build();
    }
}
