package com.aigo.service;

import com.aigo.dto.ErrorCode;
import com.aigo.dto.comment.CommentResponse;
import com.aigo.dto.comment.CreateCommentRequest;
import com.aigo.entity.Comment;
import com.aigo.entity.User;
import com.aigo.exception.BusinessException;
import com.aigo.repository.CommentRepository;
import com.aigo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(String targetType, String targetId) {
        List<Comment> comments = commentRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
        return comments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        
        Comment comment = Comment.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .userId(userId)
                .content(request.getContent())
                .build();
        
        Comment saved = commentRepository.save(comment);
        
        CommentResponse response = toCommentResponse(saved);
        response.setUsername(user.getUsername());
        return response;
    }
    
    @Transactional
    public void deleteComment(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评论不存在"));
        
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限删除此评论");
        }
        
        commentRepository.delete(comment);
    }
    
    private CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .targetType(comment.getTargetType())
                .targetId(comment.getTargetId())
                .userId(comment.getUserId())
                .username(comment.getUser() != null ? comment.getUser().getUsername() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
