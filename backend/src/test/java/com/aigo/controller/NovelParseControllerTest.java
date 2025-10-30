package com.aigo.controller;

import com.aigo.model.AnimeSegment;
import com.aigo.model.Character;
import com.aigo.model.NovelParseRequest;
import com.aigo.model.Scene;
import com.aigo.service.NovelParseService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NovelParseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NovelParseService novelParseService;

    @Autowired
    private ObjectMapper objectMapper;

    private NovelParseRequest parseRequest;
    private AnimeSegment animeSegment;

    @BeforeEach
    void setUp() {
        parseRequest = new NovelParseRequest();
        parseRequest.setText("小明和小红在公园里散步。");
        parseRequest.setStyle("动漫风格");
        parseRequest.setTargetAudience("青少年");

        Character character1 = new Character();
        character1.setName("小明");
        character1.setGender("男");
        character1.setDescription("一个阳光的少年");

        Character character2 = new Character();
        character2.setName("小红");
        character2.setGender("女");
        character2.setDescription("一个可爱的女孩");

        Scene scene = new Scene();
        scene.setSceneNumber(1);
        scene.setLocation("公园");
        scene.setDialogue("小明：今天天气真好！");
        scene.setDescription("小明和小红在公园里散步");

        animeSegment = new AnimeSegment();
        animeSegment.setCharacters(Arrays.asList(character1, character2));
        animeSegment.setScenes(Arrays.asList(scene));
    }

    @Test
    void testParseNovel_Success() throws Exception {
        when(novelParseService.parseNovelText(anyString(), anyString(), anyString()))
                .thenReturn(animeSegment);

        mockMvc.perform(post("/api/novel/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characters").isArray())
                .andExpect(jsonPath("$.characters[0].name").value("小明"))
                .andExpect(jsonPath("$.characters[1].name").value("小红"))
                .andExpect(jsonPath("$.scenes").isArray())
                .andExpect(jsonPath("$.scenes[0].sceneNumber").value(1));
    }

    @Test
    void testParseNovel_EmptyText() throws Exception {
        NovelParseRequest emptyRequest = new NovelParseRequest();
        emptyRequest.setText("");
        emptyRequest.setStyle("动漫风格");
        emptyRequest.setTargetAudience("青少年");

        mockMvc.perform(post("/api/novel/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testParseNovel_ServiceException() throws Exception {
        when(novelParseService.parseNovelText(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("AI服务不可用"));

        mockMvc.perform(post("/api/novel/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parseRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("解析失败"));
    }

    @Test
    void testParseNovel_WithDifferentStyle() throws Exception {
        parseRequest.setStyle("写实风格");
        when(novelParseService.parseNovelText(anyString(), anyString(), anyString()))
                .thenReturn(animeSegment);

        mockMvc.perform(post("/api/novel/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characters").isArray());
    }

    @Test
    void testParseNovel_WithDifferentAudience() throws Exception {
        parseRequest.setTargetAudience("成人");
        when(novelParseService.parseNovelText(anyString(), anyString(), anyString()))
                .thenReturn(animeSegment);

        mockMvc.perform(post("/api/novel/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenes").isArray());
    }
}
