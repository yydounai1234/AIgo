package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import com.aigo.dto.work.CreateWorkRequest;
import com.aigo.dto.work.GalleryItemResponse;
import com.aigo.dto.work.UpdateWorkRequest;
import com.aigo.dto.work.WorkResponse;
import com.aigo.security.JwtUtil;
import com.aigo.service.WorkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkController {
    
    private final WorkService workService;
    private final JwtUtil jwtUtil;
    
    private String getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractClaims(token).get("userId", String.class);
        }
        return null;
    }
    
    @PostMapping("/works")
    public ApiResponse<WorkResponse> createWork(@Valid @RequestBody CreateWorkRequest request,
                                                 HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        WorkResponse response = workService.createWork(userId, request);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/works/{id}")
    public ApiResponse<WorkResponse> getWork(@PathVariable String id,
                                              HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        WorkResponse response = workService.getWork(id, userId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/works/{id}")
    public ApiResponse<WorkResponse> updateWork(@PathVariable String id,
                                                 @Valid @RequestBody UpdateWorkRequest request,
                                                 HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        WorkResponse response = workService.updateWork(userId, id, request);
        return ApiResponse.success(response);
    }
    
    @DeleteMapping("/works/{id}")
    public ApiResponse<Map<String, String>> deleteWork(@PathVariable String id,
                                                         HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        workService.deleteWork(userId, id);
        return ApiResponse.success(Map.of("message", "作品已删除"));
    }
    
    @GetMapping("/my-works")
    public ApiResponse<List<WorkResponse>> getMyWorks(HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        List<WorkResponse> response = workService.getMyWorks(userId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/gallery")
    public ApiResponse<List<GalleryItemResponse>> getGallery(
            @RequestParam(defaultValue = "latest") String sortBy,
            HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        List<GalleryItemResponse> response = workService.getGallery(userId, sortBy);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/works/{id}/like")
    public ApiResponse<Map<String, String>> likeWork(@PathVariable String id,
                                                       HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        workService.likeWork(userId, id);
        return ApiResponse.success(Map.of("message", "点赞成功"));
    }
    
    @DeleteMapping("/works/{id}/like")
    public ApiResponse<Map<String, String>> unlikeWork(@PathVariable String id,
                                                         HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        workService.unlikeWork(userId, id);
        return ApiResponse.success(Map.of("message", "取消点赞成功"));
    }
}
