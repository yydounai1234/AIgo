package com.aigo.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String id;
    private String targetType;
    private String targetId;
    private String userId;
    private String username;
    private String avatarUrl;
    private String content;
    private LocalDateTime createdAt;
}
