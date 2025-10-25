package com.aigo.repository;

import com.aigo.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {
    
    Optional<Like> findByUserIdAndWorkId(String userId, String workId);
    
    boolean existsByUserIdAndWorkId(String userId, String workId);
    
    void deleteByUserIdAndWorkId(String userId, String workId);
}
