package com.aigo.dto.work;

import com.aigo.dto.episode.EpisodeListItem;
import com.aigo.entity.Work;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkResponse {
    private String id;
    private String userId;
    private String title;
    private String description;
    private Boolean isPublic;
    private String coverImage;
    private Integer likesCount;
    private Integer viewsCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EpisodeListItem> episodes;
    private String authorName;
    private String authorAvatar;
    
    public static WorkResponse fromEntity(Work work) {
        return WorkResponse.builder()
                .id(work.getId())
                .userId(work.getUserId())
                .title(work.getTitle())
                .description(work.getDescription())
                .isPublic(work.getIsPublic())
                .coverImage(work.getCoverImage())
                .likesCount(work.getLikesCount())
                .viewsCount(work.getViewsCount())
                .createdAt(work.getCreatedAt())
                .updatedAt(work.getUpdatedAt())
                .authorName(work.getUser() != null ? work.getUser().getUsername() : null)
                .authorAvatar(work.getUser() != null ? work.getUser().getAvatarUrl() : null)
                .build();
    }
    
    public static WorkResponse fromEntityWithEpisodes(Work work) {
        WorkResponse response = fromEntity(work);
        if (work.getEpisodes() != null) {
            response.setEpisodes(work.getEpisodes().stream()
                    .map(EpisodeListItem::fromEntity)
                    .collect(Collectors.toList()));
        }
        return response;
    }
}
