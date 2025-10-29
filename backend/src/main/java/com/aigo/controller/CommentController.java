package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import com.aigo.dto.comment.CommentResponse;
import com.aigo.dto.comment.CreateCommentRequest;
import com.aigo.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @GetMapping("/{targetType}/{targetId}")
    public ApiResponse<List<CommentResponse>> getComments(
            @PathVariable String targetType,
            @PathVariable String targetId) {
        List<CommentResponse> comments = commentService.getComments(targetType, targetId);
        return ApiResponse.success(comments);
    }
    
    @PostMapping
    public ApiResponse<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        CommentResponse comment = commentService.createComment(request, userId);
        return ApiResponse.success(comment);
    }
    
    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteComment(
            @PathVariable String commentId,
            Authentication authentication) {
        String userId = authentication.getName();
        commentService.deleteComment(commentId, userId);
        return ApiResponse.success("评论已删除");
    }
}
