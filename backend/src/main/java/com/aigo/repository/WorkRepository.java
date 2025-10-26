package com.aigo.repository;

import com.aigo.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkRepository extends JpaRepository<Work, String> {
    
    List<Work> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Work> findByIsPublicTrueOrderByCreatedAtDesc();
    
    List<Work> findByIsPublicTrueOrderByLikesCountDesc();
    
    Optional<Work> findByIdAndUserId(String id, String userId);
    
    @Query("SELECT w FROM Work w LEFT JOIN FETCH w.episodes WHERE w.id = :id")
    Optional<Work> findByIdWithEpisodes(String id);
    
    @Query("SELECT w FROM Work w WHERE w.id IN (SELECT l.workId FROM Like l WHERE l.userId = :userId) ORDER BY w.createdAt DESC")
    List<Work> findLikedWorksByUserId(String userId);
}
