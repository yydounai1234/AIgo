package com.aigo.service;

import com.aigo.dto.work.CreateWorkRequest;
import com.aigo.dto.work.GalleryItemResponse;
import com.aigo.dto.work.UpdateWorkRequest;
import com.aigo.dto.work.WorkResponse;
import com.aigo.entity.Episode;
import com.aigo.entity.Work;
import com.aigo.exception.BusinessException;
import com.aigo.repository.EpisodeRepository;
import com.aigo.repository.LikeRepository;
import com.aigo.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    @InjectMocks
    private WorkService workService;

    private Work testWork;
    private CreateWorkRequest createRequest;
    private UpdateWorkRequest updateRequest;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        
        testWork = Work.builder()
                .id("work123")
                .userId(userId)
                .title("Test Work")
                .description("Test Description")
                .isPublic(false)
                .likesCount(0)
                .build();

        createRequest = new CreateWorkRequest();
        createRequest.setTitle("New Work");
        createRequest.setDescription("New Description");
        createRequest.setIsPublic(true);

        updateRequest = new UpdateWorkRequest();
        updateRequest.setTitle("Updated Work");
        updateRequest.setDescription("Updated Description");
    }

    @Test
    void testCreateWork() {
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        WorkResponse response = workService.createWork(userId, createRequest);

        assertNotNull(response);
        assertEquals("work123", response.getId());
        assertEquals("Test Work", response.getTitle());

        verify(workRepository).save(any(Work.class));
    }

    @Test
    void testGetWork() {
        when(workRepository.findById(anyString())).thenReturn(Optional.of(testWork));
        when(episodeRepository.findByWorkIdAndIsPublishedTrueOrderByEpisodeNumberAsc(anyString()))
                .thenReturn(Arrays.asList());
        when(likeRepository.existsByUserIdAndWorkId(anyString(), anyString())).thenReturn(false);

        WorkResponse response = workService.getWork("work123", userId);

        assertNotNull(response);
        assertEquals("work123", response.getId());
        assertFalse(response.getIsLiked());

        verify(workRepository).findById("work123");
        verify(likeRepository).existsByUserIdAndWorkId(userId, "work123");
    }

    @Test
    void testGetWorkNotFound() {
        when(workRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> workService.getWork("invalid", userId));

        verify(workRepository).findById("invalid");
    }

    @Test
    void testUpdateWork() {
        when(workRepository.findByIdAndUserId(anyString(), anyString())).thenReturn(Optional.of(testWork));
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        WorkResponse response = workService.updateWork(userId, "work123", updateRequest);

        assertNotNull(response);
        verify(workRepository).findByIdAndUserId("work123", userId);
        verify(workRepository).save(testWork);
    }

    @Test
    void testUpdateWorkUnauthorized() {
        when(workRepository.findByIdAndUserId(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> 
            workService.updateWork(userId, "work123", updateRequest));

        verify(workRepository).findByIdAndUserId("work123", userId);
        verify(workRepository, never()).save(any());
    }

    @Test
    void testDeleteWork() {
        when(workRepository.findByIdAndUserId(anyString(), anyString())).thenReturn(Optional.of(testWork));

        workService.deleteWork(userId, "work123");

        verify(workRepository).findByIdAndUserId("work123", userId);
        verify(workRepository).delete(testWork);
    }

    @Test
    void testDeleteWorkUnauthorized() {
        when(workRepository.findByIdAndUserId(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> workService.deleteWork(userId, "work123"));

        verify(workRepository).findByIdAndUserId("work123", userId);
        verify(workRepository, never()).delete(any());
    }

    @Test
    void testGetMyWorks() {
        when(workRepository.findByUserIdOrderByCreatedAtDesc(anyString()))
                .thenReturn(Arrays.asList(testWork));
        when(episodeRepository.findByWorkIdOrderByEpisodeNumberAsc(anyString()))
                .thenReturn(Arrays.asList());

        List<WorkResponse> responses = workService.getMyWorks(userId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("work123", responses.get(0).getId());

        verify(workRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void testGetGalleryLatest() {
        when(workRepository.findByIsPublicTrueOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(testWork));
        when(likeRepository.existsByUserIdAndWorkId(anyString(), anyString())).thenReturn(false);
        when(episodeRepository.countByWorkIdAndIsPublishedTrue(anyString())).thenReturn(5L);

        List<GalleryItemResponse> responses = workService.getGallery(userId, "latest");

        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(workRepository).findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @Test
    void testGetGalleryByLikes() {
        when(workRepository.findByIsPublicTrueOrderByLikesCountDesc())
                .thenReturn(Arrays.asList(testWork));
        when(likeRepository.existsByUserIdAndWorkId(anyString(), anyString())).thenReturn(false);
        when(episodeRepository.countByWorkIdAndIsPublishedTrue(anyString())).thenReturn(5L);

        List<GalleryItemResponse> responses = workService.getGallery(userId, "likes");

        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(workRepository).findByIsPublicTrueOrderByLikesCountDesc();
    }

    @Test
    void testLikeWork() {
        when(workRepository.findById(anyString())).thenReturn(Optional.of(testWork));
        when(likeRepository.existsByUserIdAndWorkId(anyString(), anyString())).thenReturn(false);
        when(likeRepository.save(any())).thenReturn(null);
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        workService.likeWork(userId, "work123");

        verify(likeRepository).existsByUserIdAndWorkId(userId, "work123");
        verify(likeRepository).save(any());
        verify(workRepository).save(testWork);
    }

    @Test
    void testLikeWorkAlreadyLiked() {
        when(workRepository.findById(anyString())).thenReturn(Optional.of(testWork));
        when(likeRepository.existsByUserIdAndWorkId(anyString(), anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> workService.likeWork(userId, "work123"));

        verify(likeRepository).existsByUserIdAndWorkId(userId, "work123");
        verify(likeRepository, never()).save(any());
    }

    @Test
    void testUnlikeWork() {
        testWork.setLikesCount(5);
        when(workRepository.findById(anyString())).thenReturn(Optional.of(testWork));
        when(likeRepository.existsByUserIdAndWorkId(anyString(), anyString())).thenReturn(true);
        when(workRepository.save(any(Work.class))).thenReturn(testWork);

        workService.unlikeWork(userId, "work123");

        verify(likeRepository).existsByUserIdAndWorkId(userId, "work123");
        verify(likeRepository).deleteByUserIdAndWorkId(userId, "work123");
        verify(workRepository).save(testWork);
    }

    @Test
    void testUnlikeWorkNotLiked() {
        when(workRepository.findById(anyString())).thenReturn(Optional.of(testWork));
        when(likeRepository.existsByUserIdAndWorkId(anyString(), anyString())).thenReturn(false);

        assertThrows(BusinessException.class, () -> workService.unlikeWork(userId, "work123"));

        verify(likeRepository).existsByUserIdAndWorkId(userId, "work123");
        verify(likeRepository, never()).deleteByUserIdAndWorkId(anyString(), anyString());
    }
}
