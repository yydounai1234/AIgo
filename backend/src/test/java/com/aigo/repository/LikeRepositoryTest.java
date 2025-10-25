package com.aigo.repository;

import com.aigo.entity.Like;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    private Like testLike;
    private String userId;
    private String workId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        workId = "work456";
        testLike = Like.builder()
                .userId(userId)
                .workId(workId)
                .build();
    }

    @Test
    void testSaveLike() {
        Like saved = likeRepository.save(testLike);

        assertNotNull(saved.getId());
        assertEquals(userId, saved.getUserId());
        assertEquals(workId, saved.getWorkId());
    }

    @Test
    void testExistsByUserIdAndWorkId() {
        likeRepository.save(testLike);

        assertTrue(likeRepository.existsByUserIdAndWorkId(userId, workId));
        assertFalse(likeRepository.existsByUserIdAndWorkId("otheruser", workId));
        assertFalse(likeRepository.existsByUserIdAndWorkId(userId, "otherwork"));
    }

    @Test
    void testDeleteByUserIdAndWorkId() {
        likeRepository.save(testLike);

        assertTrue(likeRepository.existsByUserIdAndWorkId(userId, workId));

        likeRepository.deleteByUserIdAndWorkId(userId, workId);

        assertFalse(likeRepository.existsByUserIdAndWorkId(userId, workId));
    }

    @Test
    void testDeleteByUserIdAndWorkIdNotExisting() {
        likeRepository.deleteByUserIdAndWorkId("nonexistent", "nonexistent");

        assertFalse(likeRepository.existsByUserIdAndWorkId("nonexistent", "nonexistent"));
    }
}
