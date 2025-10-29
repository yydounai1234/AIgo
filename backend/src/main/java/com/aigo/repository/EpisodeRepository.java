package com.aigo.repository;

import com.aigo.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, String> {
    
    List<Episode> findByWorkIdOrderByEpisodeNumberAsc(String workId);
    
    List<Episode> findByWorkIdAndIsPublishedTrueOrderByEpisodeNumberAsc(String workId);
    
    Optional<Episode> findByWorkIdAndEpisodeNumber(String workId, Integer episodeNumber);
    
    @Query("SELECT MAX(e.episodeNumber) FROM Episode e WHERE e.workId = :workId")
    Optional<Integer> findMaxEpisodeNumberByWorkId(String workId);
    
    Long countByWorkIdAndIsPublishedTrue(String workId);
    
    @Query("SELECT e FROM Episode e LEFT JOIN FETCH e.work w LEFT JOIN FETCH w.user WHERE e.id = :id")
    Optional<Episode> findByIdWithWorkAndUser(String id);
}
