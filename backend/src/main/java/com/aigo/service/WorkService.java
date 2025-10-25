package com.aigo.service;

import com.aigo.dto.ErrorCode;
import com.aigo.dto.work.CreateWorkRequest;
import com.aigo.dto.work.GalleryItemResponse;
import com.aigo.dto.work.UpdateWorkRequest;
import com.aigo.dto.work.WorkResponse;
import com.aigo.entity.Work;
import com.aigo.exception.BusinessException;
import com.aigo.repository.EpisodeRepository;
import com.aigo.repository.LikeRepository;
import com.aigo.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkService {
    
    private final WorkRepository workRepository;
    private final LikeRepository likeRepository;
    private final EpisodeRepository episodeRepository;
    
    @Transactional
    public WorkResponse createWork(String userId, CreateWorkRequest request) {
        Work work = Work.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .coverImage(request.getCoverImage())
                .build();
        
        work = workRepository.save(work);
        return WorkResponse.fromEntity(work);
    }
    
    @Transactional(readOnly = true)
    public WorkResponse getWork(String workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        return WorkResponse.fromEntity(work);
    }
    
    @Transactional
    public WorkResponse updateWork(String userId, String workId, UpdateWorkRequest request) {
        Work work = workRepository.findByIdAndUserId(workId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "无权限修改此作品"));
        
        if (request.getTitle() != null) {
            work.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            work.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            work.setIsPublic(request.getIsPublic());
        }
        if (request.getCoverImage() != null) {
            work.setCoverImage(request.getCoverImage());
        }
        
        work = workRepository.save(work);
        return WorkResponse.fromEntity(work);
    }
    
    @Transactional
    public void deleteWork(String userId, String workId) {
        Work work = workRepository.findByIdAndUserId(workId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "无权限删除此作品"));
        
        workRepository.delete(work);
    }
    
    @Transactional(readOnly = true)
    public List<WorkResponse> getMyWorks(String userId) {
        List<Work> works = workRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return works.stream()
                .map(work -> {
                    WorkResponse response = WorkResponse.fromEntity(work);
                    response.setEpisodes(
                        episodeRepository.findByWorkIdOrderByEpisodeNumberAsc(work.getId())
                            .stream()
                            .map(com.aigo.dto.episode.EpisodeListItem::fromEntity)
                            .collect(Collectors.toList())
                    );
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GalleryItemResponse> getGallery(String currentUserId, String sortBy) {
        List<Work> works;
        
        if ("likes".equals(sortBy)) {
            works = workRepository.findByIsPublicTrueOrderByLikesCountDesc();
        } else {
            works = workRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        }
        
        return works.stream()
                .map(work -> {
                    boolean isLiked = currentUserId != null && 
                            likeRepository.existsByUserIdAndWorkId(currentUserId, work.getId());
                    int episodeCount = episodeRepository.countByWorkIdAndIsPublishedTrue(work.getId()).intValue();
                    return GalleryItemResponse.fromEntity(work, isLiked, episodeCount);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void likeWork(String userId, String workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        
        if (likeRepository.existsByUserIdAndWorkId(userId, workId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已点赞过");
        }
        
        com.aigo.entity.Like like = com.aigo.entity.Like.builder()
                .userId(userId)
                .workId(workId)
                .build();
        
        likeRepository.save(like);
        
        work.setLikesCount(work.getLikesCount() + 1);
        workRepository.save(work);
    }
    
    @Transactional
    public void unlikeWork(String userId, String workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        
        if (!likeRepository.existsByUserIdAndWorkId(userId, workId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未点赞过");
        }
        
        likeRepository.deleteByUserIdAndWorkId(userId, workId);
        
        work.setLikesCount(Math.max(0, work.getLikesCount() - 1));
        workRepository.save(work);
    }
}
