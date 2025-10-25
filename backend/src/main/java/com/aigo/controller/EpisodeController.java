package com.aigo.controller;

import com.aigo.dto.ApiResponse;
import com.aigo.dto.episode.CreateEpisodeRequest;
import com.aigo.dto.episode.EpisodeResponse;
import com.aigo.dto.episode.PurchaseResponse;
import com.aigo.dto.episode.UpdateEpisodeRequest;
import com.aigo.security.JwtUtil;
import com.aigo.service.EpisodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EpisodeController {
    
    private final EpisodeService episodeService;
    private final JwtUtil jwtUtil;
    
    private String getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractClaims(token).get("userId", String.class);
        }
        return null;
    }
    
    @PostMapping("/works/{workId}/episodes")
    public ApiResponse<EpisodeResponse> createEpisode(@PathVariable String workId,
                                                        @Valid @RequestBody CreateEpisodeRequest request,
                                                        HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        EpisodeResponse response = episodeService.createEpisode(userId, workId, request);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/episodes/{id}")
    public Object getEpisode(@PathVariable String id, HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        Object response = episodeService.getEpisode(userId, id);
        
        if (response instanceof EpisodeResponse) {
            return ApiResponse.success((EpisodeResponse) response);
        }
        return response;
    }
    
    @PutMapping("/episodes/{id}")
    public ApiResponse<EpisodeResponse> updateEpisode(@PathVariable String id,
                                                        @Valid @RequestBody UpdateEpisodeRequest request,
                                                        HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        EpisodeResponse response = episodeService.updateEpisode(userId, id, request);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/episodes/{id}/publish")
    public ApiResponse<EpisodeResponse> publishEpisode(@PathVariable String id,
                                                         HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        EpisodeResponse response = episodeService.publishEpisode(userId, id);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/episodes/{id}/purchase")
    public ApiResponse<PurchaseResponse> purchaseEpisode(@PathVariable String id,
                                                           HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        PurchaseResponse response = episodeService.purchaseEpisode(userId, id);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/episodes/{id}/retry")
    public ApiResponse<EpisodeResponse> retryEpisode(@PathVariable String id,
                                                       HttpServletRequest httpRequest) {
        String userId = getUserIdFromRequest(httpRequest);
        EpisodeResponse response = episodeService.retryEpisode(userId, id);
        return ApiResponse.success(response);
    }
}
