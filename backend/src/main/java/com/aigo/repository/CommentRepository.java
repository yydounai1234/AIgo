package com.aigo.repository;

import com.aigo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    
    List<Comment> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, String targetId);
    
    Long countByTargetTypeAndTargetId(String targetType, String targetId);
}
