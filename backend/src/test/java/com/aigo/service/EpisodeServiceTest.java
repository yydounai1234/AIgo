package com.aigo.service;

import com.aigo.dto.ErrorCode;
import com.aigo.dto.episode.CreateEpisodeRequest;
import com.aigo.dto.episode.EpisodeResponse;
import com.aigo.dto.episode.PurchaseResponse;
import com.aigo.dto.episode.UpdateEpisodeRequest;
import com.aigo.entity.Episode;
import com.aigo.entity.Purchase;
import com.aigo.entity.User;
import com.aigo.entity.Work;
import com.aigo.exception.BusinessException;
import com.aigo.repository.EpisodeRepository;
import com.aigo.repository.PurchaseRepository;
import com.aigo.repository.UserRepository;
import com.aigo.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EpisodeServiceTest {

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EpisodeAsyncService episodeAsyncService;

    @InjectMocks
    private EpisodeService episodeService;

    private Work testWork;
    private Episode testEpisode;
    private User testUser;
    private CreateEpisodeRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user1")
                .username("testUser")
                .coinBalance(1000)
                .build();

        testWork = Work.builder()
                .id("work1")
                .userId("user1")
                .title("测试作品")
                .description("测试描述")
                .viewsCount(0)
                .build();

        testEpisode = Episode.builder()
                .id("episode1")
                .workId("work1")
                .episodeNumber(1)
                .title("第一集")
                .novelText("小说内容")
                .isFree(false)
                .coinPrice(100)
                .status("PENDING")
                .isPublished(false)
                .build();

        createRequest = CreateEpisodeRequest.builder()
                .title("第一集")
                .novelText("小说内容")
                .isFree(false)
                .coinPrice(100)
                .style("动漫风格")
                .targetAudience("青少年")
                .build();
    }

    @Test
    void testCreateEpisode_Success() {
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));
        when(episodeRepository.findMaxEpisodeNumberByWorkId("work1")).thenReturn(Optional.of(0));
        when(episodeRepository.save(any(Episode.class))).thenReturn(testEpisode);

        EpisodeResponse response = episodeService.createEpisode("user1", "work1", createRequest);

        assertNotNull(response);
        assertEquals("episode1", response.getId());
        assertEquals("第一集", response.getTitle());
        verify(episodeAsyncService).processEpisodeAsync(anyString(), anyString());
    }

    @Test
    void testCreateEpisode_WorkNotFound() {
        when(workRepository.findById("work1")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> episodeService.createEpisode("user1", "work1", createRequest));

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("作品不存在", exception.getMessage());
    }

    @Test
    void testCreateEpisode_Forbidden() {
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> episodeService.createEpisode("user2", "work1", createRequest));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(episodeRepository, never()).save(any(Episode.class));
    }

    @Test
    void testUpdateEpisode_Success() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));
        when(episodeRepository.save(any(Episode.class))).thenReturn(testEpisode);

        UpdateEpisodeRequest updateRequest = UpdateEpisodeRequest.builder()
                .title("第一集（修改）")
                .build();

        EpisodeResponse response = episodeService.updateEpisode("user1", "episode1", updateRequest);

        assertNotNull(response);
        verify(episodeRepository).save(any(Episode.class));
    }

    @Test
    void testUpdateEpisode_AlreadyPublished() {
        testEpisode.setIsPublished(true);
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));

        UpdateEpisodeRequest updateRequest = UpdateEpisodeRequest.builder()
                .title("第一集（修改）")
                .build();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> episodeService.updateEpisode("user1", "episode1", updateRequest));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("已发布的集数不可修改", exception.getMessage());
    }

    @Test
    void testPublishEpisode_Success() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));
        when(episodeRepository.save(any(Episode.class))).thenReturn(testEpisode);

        EpisodeResponse response = episodeService.publishEpisode("user1", "episode1");

        assertNotNull(response);
        verify(episodeRepository).save(any(Episode.class));
    }

    @Test
    void testPublishEpisode_AlreadyPublished() {
        testEpisode.setIsPublished(true);
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> episodeService.publishEpisode("user1", "episode1"));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("集数已发布", exception.getMessage());
    }

    @Test
    void testPurchaseEpisode_Success() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));
        when(purchaseRepository.existsByUserIdAndEpisodeId("user2", "episode1")).thenReturn(false);
        when(userRepository.findById("user2")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(new Purchase());

        PurchaseResponse response = episodeService.purchaseEpisode("user2", "episode1");

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void testPurchaseEpisode_FreeEpisode() {
        testEpisode.setIsFree(true);
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> episodeService.purchaseEpisode("user2", "episode1"));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("免费集数无需购买", exception.getMessage());
    }

    @Test
    void testPurchaseEpisode_OwnWork() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> episodeService.purchaseEpisode("user1", "episode1"));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("无需购买自己的作品", exception.getMessage());
    }

    @Test
    void testPurchaseEpisode_AlreadyPurchased() {
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));
        when(purchaseRepository.existsByUserIdAndEpisodeId("user2", "episode1")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> episodeService.purchaseEpisode("user2", "episode1"));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("已购买过该集数", exception.getMessage());
    }

    @Test
    void testPurchaseEpisode_InsufficientCoins() {
        testUser.setCoinBalance(50);
        when(episodeRepository.findById("episode1")).thenReturn(Optional.of(testEpisode));
        when(workRepository.findById("work1")).thenReturn(Optional.of(testWork));
        when(purchaseRepository.existsByUserIdAndEpisodeId("user2", "episode1")).thenReturn(false);
        when(userRepository.findById("user2")).thenReturn(Optional.of(testUser));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> episodeService.purchaseEpisode("user2", "episode1"));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("金币不足"));
    }
}
