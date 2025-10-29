package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import com.aigo.dto.comment.CommentResponse;
import com.aigo.dto.comment.CreateCommentRequest;
import com.aigo.security.JwtUtil;
import com.aigo.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    private final JwtUtil jwtUtil;
    
    private String getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractClaims(token).get("userId", String.class);
        }
        return null;
    }
    
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
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        CommentResponse comment = commentService.createComment(request, userId);
        return ApiResponse.success(comment);
    }
    
    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteComment(
            @PathVariable String commentId,
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        commentService.deleteComment(commentId, userId);
        return ApiResponse.success("评论已删除");
    }
}
