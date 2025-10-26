package com.aigo.controller;

import com.aigo.dto.work.CreateWorkRequest;
import com.aigo.dto.work.GalleryItemResponse;
import com.aigo.dto.work.UpdateWorkRequest;
import com.aigo.dto.work.WorkResponse;
import com.aigo.security.JwtUtil;
import com.aigo.service.WorkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkService workService;

    @MockBean
    private JwtUtil jwtUtil;

    private String token;
    private String userId;
    private WorkResponse workResponse;
    private Claims claims;

    @BeforeEach
    void setUp() {
        token = "Bearer test-token";
        userId = "user123";
        
        claims = Jwts.claims();
        claims.put("userId", userId);
        
        workResponse = WorkResponse.builder()
                .id("work123")
                .userId(userId)
                .title("Test Work")
                .description("Test Description")
                .isPublic(true)
                .likesCount(0)
                .episodes(Collections.emptyList())
                .build();

        when(jwtUtil.extractClaims(anyString())).thenReturn(claims);
    }

    @Test
    void testCreateWork() throws Exception {
        CreateWorkRequest request = new CreateWorkRequest();
        request.setTitle("New Work");
        request.setDescription("Description");

        when(workService.createWork(anyString(), any(CreateWorkRequest.class)))
                .thenReturn(workResponse);

        mockMvc.perform(post("/api/works")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("work123"));
    }

    @Test
    void testGetWork() throws Exception {
        when(workService.getWork(anyString(), anyString())).thenReturn(workResponse);

        mockMvc.perform(get("/api/works/work123")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("work123"));
    }

    @Test
    void testUpdateWork() throws Exception {
        UpdateWorkRequest request = new UpdateWorkRequest();
        request.setTitle("Updated Title");

        when(workService.updateWork(anyString(), anyString(), any(UpdateWorkRequest.class)))
                .thenReturn(workResponse);

        mockMvc.perform(put("/api/works/work123")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteWork() throws Exception {
        mockMvc.perform(delete("/api/works/work123")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetMyWorks() throws Exception {
        when(workService.getMyWorks(anyString()))
                .thenReturn(Arrays.asList(workResponse));

        mockMvc.perform(get("/api/my-works")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetGallery() throws Exception {
        GalleryItemResponse galleryItem = GalleryItemResponse.builder()
                .id("work123")
                .title("Test Work")
                .likesCount(0)
                .episodeCount(5)
                .build();

        when(workService.getGallery(anyString(), anyString()))
                .thenReturn(Arrays.asList(galleryItem));

        mockMvc.perform(get("/api/gallery")
                        .header("Authorization", token)
                        .param("sortBy", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testLikeWork() throws Exception {
        mockMvc.perform(post("/api/works/work123/like")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUnlikeWork() throws Exception {
        mockMvc.perform(delete("/api/works/work123/like")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
