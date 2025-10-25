package com.aigo.repository;

import com.aigo.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, String> {
    
    Optional<Purchase> findByUserIdAndEpisodeId(String userId, String episodeId);
    
    boolean existsByUserIdAndEpisodeId(String userId, String episodeId);
}
