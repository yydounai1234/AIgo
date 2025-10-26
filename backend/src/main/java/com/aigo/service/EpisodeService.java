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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EpisodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EpisodeService.class);
    
    private final EpisodeRepository episodeRepository;
    private final WorkRepository workRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final EpisodeAsyncService episodeAsyncService;
    
    @Transactional
    public EpisodeResponse createEpisode(String userId, String workId, CreateEpisodeRequest request) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        
        if (!work.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限为此作品创建集数");
        }
        
        Integer maxEpisodeNumber = episodeRepository.findMaxEpisodeNumberByWorkId(workId).orElse(0);
        
        Episode episode = Episode.builder()
                .workId(workId)
                .episodeNumber(maxEpisodeNumber + 1)
                .title(request.getTitle())
                .novelText(request.getNovelText())
                .isFree(request.getIsFree())
                .coinPrice(request.getIsFree() ? 0 : request.getCoinPrice())
                .style(request.getStyle())
                .targetAudience(request.getTargetAudience())
                .status("PENDING")
                .build();
        
        episode = episodeRepository.save(episode);
        
        episodeAsyncService.processEpisodeAsync(episode.getId(), request.getNovelText());
        
        return EpisodeResponse.fromEntity(episode);
    }
    
    @Transactional
    public Object getEpisode(String userId, String episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "集数不存在"));
        
        Work work = workRepository.findById(episode.getWorkId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        
        if (!episode.getIsPublished() && !work.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "集数未发布");
        }
        
        if (episode.getIsFree() || work.getUserId().equals(userId) || 
            (userId != null && purchaseRepository.existsByUserIdAndEpisodeId(userId, episodeId))) {
            work.setViewsCount(work.getViewsCount() + 1);
            workRepository.save(work);
            
            return EpisodeResponse.fromEntity(episode);
        }
        
        Map<String, Object> needsPurchase = new HashMap<>();
        needsPurchase.put("success", false);
        needsPurchase.put("needsPurchase", true);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", episode.getId());
        data.put("episodeId", episode.getId());
        data.put("episodeNumber", episode.getEpisodeNumber());
        data.put("title", episode.getTitle());
        data.put("coinPrice", episode.getCoinPrice());
        data.put("workId", episode.getWorkId());
        data.put("isFree", episode.getIsFree());
        needsPurchase.put("data", data);
        
        return needsPurchase;
    }
    
    @Transactional
    public EpisodeResponse updateEpisode(String userId, String episodeId, UpdateEpisodeRequest request) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "集数不存在"));
        
        Work work = workRepository.findById(episode.getWorkId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        
        if (!work.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限修改此集数");
        }
        
        if (episode.getIsPublished()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已发布的集数不可修改");
        }
        
        if (request.getTitle() != null) {
            episode.setTitle(request.getTitle());
        }
        if (request.getNovelText() != null) {
            episode.setNovelText(request.getNovelText());
        }
        if (request.getIsFree() != null) {
            episode.setIsFree(request.getIsFree());
        }
        if (request.getCoinPrice() != null) {
            episode.setCoinPrice(request.getCoinPrice());
        }
        
        episode = episodeRepository.save(episode);
        return EpisodeResponse.fromEntity(episode);
    }
    
    @Transactional
    public EpisodeResponse publishEpisode(String userId, String episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "集数不存在"));
        
        Work work = workRepository.findById(episode.getWorkId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        
        if (!work.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限发布此集数");
        }
        
        if (episode.getIsPublished()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "集数已发布");
        }
        
        episode.setIsPublished(true);
        episode = episodeRepository.save(episode);
        
        return EpisodeResponse.fromEntity(episode);
    }
    
    @Transactional
    public PurchaseResponse purchaseEpisode(String userId, String episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "集数不存在"));
        
        Work work = workRepository.findById(episode.getWorkId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        
        if (episode.getIsFree()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "免费集数无需购买");
        }
        
        if (work.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无需购买自己的作品");
        }
        
        if (purchaseRepository.existsByUserIdAndEpisodeId(userId, episodeId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已购买过该集数");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        
        if (user.getCoinBalance() < episode.getCoinPrice()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, 
                String.format("金币不足，需要 %d 金币，当前余额 %d", episode.getCoinPrice(), user.getCoinBalance()));
        }
        
        user.setCoinBalance(user.getCoinBalance() - episode.getCoinPrice());
        userRepository.save(user);
        
        Purchase purchase = Purchase.builder()
                .userId(userId)
                .episodeId(episodeId)
                .coinCost(episode.getCoinPrice())
                .build();
        
        purchase = purchaseRepository.save(purchase);
        
        return PurchaseResponse.builder()
                .episodeId(episodeId)
                .coinCost(episode.getCoinPrice())
                .newBalance(user.getCoinBalance())
                .purchasedAt(purchase.getPurchasedAt())
                .build();
    }
    
    @Transactional
    public EpisodeResponse retryEpisode(String userId, String episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "集数不存在"));
        
        Work work = workRepository.findById(episode.getWorkId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "作品不存在"));
        
        if (!work.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限重试此集数");
        }
        
        if (!"FAILED".equals(episode.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能重试失败的集数");
        }
        
        episode.setStatus("PENDING");
        episode.setErrorMessage(null);
        episode = episodeRepository.save(episode);
        
        episodeAsyncService.processEpisodeAsync(episode.getId(), episode.getNovelText());
        
        return EpisodeResponse.fromEntity(episode);
    }
}
