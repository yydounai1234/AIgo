package com.aigo.controller;

import com.aigo.dto.comment.CommentResponse;
import com.aigo.dto.comment.CreateCommentRequest;
import com.aigo.security.JwtUtil;
import com.aigo.service.CommentService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String testToken;
    private String userId;
    private CommentResponse commentResponse;
    private CreateCommentRequest createRequest;

    @BeforeEach
    void setUp() {
        testToken = "test-jwt-token";
        userId = "user123";
        
        Claims claims = new DefaultClaims();
        claims.put("userId", userId);
        when(jwtUtil.extractClaims(testToken)).thenReturn(claims);

        commentResponse = CommentResponse.builder()
                .id("comment1")
                .userId(userId)
                .username("testUser")
                .targetType("work")
                .targetId("work1")
                .content("测试评论")
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateCommentRequest.builder()
                .targetType("work")
                .targetId("work1")
                .content("测试评论")
                .build();
    }

    @Test
    void testGetComments_Success() throws Exception {
        List<CommentResponse> comments = Arrays.asList(commentResponse);
        when(commentService.getComments("work", "work1")).thenReturn(comments);

        mockMvc.perform(get("/api/comments/work/work1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("comment1"))
                .andExpect(jsonPath("$.data[0].content").value("测试评论"));
    }

    @Test
    void testGetComments_EmptyList() throws Exception {
        when(commentService.getComments("work", "work1")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/comments/work/work1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testCreateComment_Success() throws Exception {
        when(commentService.createComment(any(CreateCommentRequest.class), eq(userId)))
                .thenReturn(commentResponse);

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("comment1"))
                .andExpect(jsonPath("$.data.content").value("测试评论"));
    }

    @Test
    void testCreateComment_InvalidRequest() throws Exception {
        CreateCommentRequest invalidRequest = CreateCommentRequest.builder()
                .targetType("")
                .targetId("")
                .content("")
                .build();

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteComment_Success() throws Exception {
        mockMvc.perform(delete("/api/comments/comment1")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("评论已删除"));
    }

    @Test
    void testDeleteComment_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/comments/comment1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
