package com.aigo.controller;

import com.aigo.dto.episode.CreateEpisodeRequest;
import com.aigo.dto.episode.EpisodeResponse;
import com.aigo.dto.episode.PurchaseResponse;
import com.aigo.dto.episode.UpdateEpisodeRequest;
import com.aigo.security.JwtUtil;
import com.aigo.service.EpisodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EpisodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EpisodeService episodeService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String testToken;
    private String userId;
    private String workId;
    private String episodeId;
    private EpisodeResponse episodeResponse;

    @BeforeEach
    void setUp() {
        testToken = "test-jwt-token";
        userId = "user123";
        workId = "work123";
        episodeId = "episode123";
        
        Claims claims = new DefaultClaims();
        claims.put("userId", userId);
        when(jwtUtil.extractClaims(testToken)).thenReturn(claims);

        episodeResponse = EpisodeResponse.builder()
                .id(episodeId)
                .workId(workId)
                .title("第一集")
                .episodeNumber(1)
                .content("剧集内容")
                .price(100)
                .published(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateEpisode_Success() throws Exception {
        CreateEpisodeRequest request = CreateEpisodeRequest.builder()
                .title("第一集")
                .content("剧集内容")
                .price(100)
                .build();

        when(episodeService.createEpisode(eq(userId), eq(workId), any(CreateEpisodeRequest.class)))
                .thenReturn(episodeResponse);

        mockMvc.perform(post("/api/works/" + workId + "/episodes")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(episodeId))
                .andExpect(jsonPath("$.data.title").value("第一集"));
    }

    @Test
    void testGetEpisode_Success() throws Exception {
        when(episodeService.getEpisode(userId, episodeId)).thenReturn(episodeResponse);

        mockMvc.perform(get("/api/episodes/" + episodeId)
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(episodeId));
    }

    @Test
    void testUpdateEpisode_Success() throws Exception {
        UpdateEpisodeRequest request = UpdateEpisodeRequest.builder()
                .title("第一集（修改）")
                .content("修改后的内容")
                .price(200)
                .build();

        EpisodeResponse updatedResponse = EpisodeResponse.builder()
                .id(episodeId)
                .workId(workId)
                .title("第一集（修改）")
                .episodeNumber(1)
                .content("修改后的内容")
                .price(200)
                .published(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(episodeService.updateEpisode(eq(userId), eq(episodeId), any(UpdateEpisodeRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/episodes/" + episodeId)
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("第一集（修改）"))
                .andExpect(jsonPath("$.data.price").value(200));
    }

    @Test
    void testPublishEpisode_Success() throws Exception {
        EpisodeResponse publishedResponse = EpisodeResponse.builder()
                .id(episodeId)
                .workId(workId)
                .title("第一集")
                .episodeNumber(1)
                .content("剧集内容")
                .price(100)
                .published(true)
                .createdAt(LocalDateTime.now())
                .publishedAt(LocalDateTime.now())
                .build();

        when(episodeService.publishEpisode(userId, episodeId)).thenReturn(publishedResponse);

        mockMvc.perform(post("/api/episodes/" + episodeId + "/publish")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.published").value(true));
    }

    @Test
    void testPurchaseEpisode_Success() throws Exception {
        PurchaseResponse purchaseResponse = PurchaseResponse.builder()
                .episodeId(episodeId)
                .purchased(true)
                .remainingCoins(900)
                .build();

        when(episodeService.purchaseEpisode(userId, episodeId)).thenReturn(purchaseResponse);

        mockMvc.perform(post("/api/episodes/" + episodeId + "/purchase")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.purchased").value(true))
                .andExpect(jsonPath("$.data.remainingCoins").value(900));
    }

    @Test
    void testRetryEpisode_Success() throws Exception {
        when(episodeService.retryEpisode(userId, episodeId)).thenReturn(episodeResponse);

        mockMvc.perform(post("/api/episodes/" + episodeId + "/retry")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(episodeId));
    }

    @Test
    void testCreateEpisode_Unauthorized() throws Exception {
        CreateEpisodeRequest request = CreateEpisodeRequest.builder()
                .title("第一集")
                .content("剧集内容")
                .price(100)
                .build();

        mockMvc.perform(post("/api/works/" + workId + "/episodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
