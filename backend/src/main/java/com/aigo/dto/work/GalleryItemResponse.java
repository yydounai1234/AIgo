package com.aigo.dto.work;

import com.aigo.entity.Work;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryItemResponse {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String coverImage;
    private Integer likesCount;
    private Integer viewsCount;
    private Boolean isLiked;
    private Integer episodeCount;
    private LocalDateTime createdAt;
    private String authorName;
    private String authorAvatar;
    
    public static GalleryItemResponse fromEntity(Work work, boolean isLiked, int episodeCount) {
        return GalleryItemResponse.builder()
                .id(work.getId())
                .userId(work.getUserId())
                .title(work.getTitle())
                .description(work.getDescription())
                .coverImage(work.getCoverImage())
                .likesCount(work.getLikesCount())
                .viewsCount(work.getViewsCount())
                .isLiked(isLiked)
                .episodeCount(episodeCount)
                .createdAt(work.getCreatedAt())
                .authorName(work.getUser() != null ? work.getUser().getUsername() : null)
                .authorAvatar(work.getUser() != null ? work.getUser().getAvatarUrl() : null)
                .build();
    }
}
