package com.aigo.repository;

import com.aigo.entity.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class WorkRepositoryTest {

    @Autowired
    private WorkRepository workRepository;

    private Work testWork;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        testWork = Work.builder()
                .userId(userId)
                .title("Test Work")
                .description("Test Description")
                .isPublic(true)
                .likesCount(0)
                .build();
    }

    @Test
    void testSaveWork() {
        Work saved = workRepository.save(testWork);

        assertNotNull(saved.getId());
        assertEquals("Test Work", saved.getTitle());
        assertEquals(userId, saved.getUserId());
    }

    @Test
    void testFindByIdAndUserId() {
        Work saved = workRepository.save(testWork);

        Optional<Work> found = workRepository.findByIdAndUserId(saved.getId(), userId);

        assertTrue(found.isPresent());
        assertEquals("Test Work", found.get().getTitle());
    }

    @Test
    void testFindByIdAndUserIdNotFound() {
        Work saved = workRepository.save(testWork);

        Optional<Work> found = workRepository.findByIdAndUserId(saved.getId(), "wronguser");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() {
        workRepository.save(testWork);

        Work work2 = Work.builder()
                .userId(userId)
                .title("Second Work")
                .description("Description 2")
                .isPublic(false)
                .likesCount(0)
                .build();
        workRepository.save(work2);

        List<Work> works = workRepository.findByUserIdOrderByCreatedAtDesc(userId);

        assertEquals(2, works.size());
    }

    @Test
    void testFindByIsPublicTrueOrderByCreatedAtDesc() {
        testWork.setIsPublic(true);
        workRepository.save(testWork);

        Work privateWork = Work.builder()
                .userId(userId)
                .title("Private Work")
                .description("Private")
                .isPublic(false)
                .likesCount(0)
                .build();
        workRepository.save(privateWork);

        List<Work> publicWorks = workRepository.findByIsPublicTrueOrderByCreatedAtDesc();

        assertEquals(1, publicWorks.size());
        assertTrue(publicWorks.get(0).getIsPublic());
    }

    @Test
    void testFindByIsPublicTrueOrderByLikesCountDesc() {
        testWork.setIsPublic(true);
        testWork.setLikesCount(10);
        workRepository.save(testWork);

        Work anotherWork = Work.builder()
                .userId(userId)
                .title("Another Work")
                .description("Description")
                .isPublic(true)
                .likesCount(5)
                .build();
        workRepository.save(anotherWork);

        List<Work> works = workRepository.findByIsPublicTrueOrderByLikesCountDesc();

        assertEquals(2, works.size());
        assertEquals(10, works.get(0).getLikesCount());
        assertEquals(5, works.get(1).getLikesCount());
    }

    @Test
    void testDeleteWork() {
        Work saved = workRepository.save(testWork);
        String workId = saved.getId();

        workRepository.deleteById(workId);

        assertFalse(workRepository.findById(workId).isPresent());
    }
}
