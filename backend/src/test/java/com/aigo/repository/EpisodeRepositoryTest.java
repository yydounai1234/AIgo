package com.aigo.repository;

import com.aigo.entity.Episode;
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
class EpisodeRepositoryTest {

    @Autowired
    private EpisodeRepository episodeRepository;

    private Episode testEpisode;
    private String workId;

    @BeforeEach
    void setUp() {
        workId = "work123";
        testEpisode = Episode.builder()
                .workId(workId)
                .episodeNumber(1)
                .title("Episode 1")
                .novelText("Test novel text")
                .isFree(true)
                .coinPrice(0)
                .status("COMPLETED")
                .isPublished(true)
                .build();
    }

    @Test
    void testSaveEpisode() {
        Episode saved = episodeRepository.save(testEpisode);

        assertNotNull(saved.getId());
        assertEquals("Episode 1", saved.getTitle());
        assertEquals(workId, saved.getWorkId());
    }

    @Test
    void testFindByWorkIdOrderByEpisodeNumberAsc() {
        episodeRepository.save(testEpisode);

        Episode episode2 = Episode.builder()
                .workId(workId)
                .episodeNumber(2)
                .title("Episode 2")
                .novelText("Text 2")
                .isFree(false)
                .coinPrice(10)
                .status("COMPLETED")
                .isPublished(true)
                .build();
        episodeRepository.save(episode2);

        List<Episode> episodes = episodeRepository.findByWorkIdOrderByEpisodeNumberAsc(workId);

        assertEquals(2, episodes.size());
        assertEquals(1, episodes.get(0).getEpisodeNumber());
        assertEquals(2, episodes.get(1).getEpisodeNumber());
    }

    @Test
    void testFindByWorkIdAndIsPublishedTrueOrderByEpisodeNumberAsc() {
        testEpisode.setIsPublished(true);
        episodeRepository.save(testEpisode);

        Episode unpublished = Episode.builder()
                .workId(workId)
                .episodeNumber(2)
                .title("Episode 2")
                .novelText("Text 2")
                .isFree(false)
                .coinPrice(10)
                .status("PENDING")
                .isPublished(false)
                .build();
        episodeRepository.save(unpublished);

        List<Episode> episodes = episodeRepository.findByWorkIdAndIsPublishedTrueOrderByEpisodeNumberAsc(workId);

        assertEquals(1, episodes.size());
        assertTrue(episodes.get(0).getIsPublished());
    }

    @Test
    void testCountByWorkIdAndIsPublishedTrue() {
        testEpisode.setIsPublished(true);
        episodeRepository.save(testEpisode);

        Episode unpublished = Episode.builder()
                .workId(workId)
                .episodeNumber(2)
                .title("Episode 2")
                .novelText("Text 2")
                .isFree(false)
                .coinPrice(10)
                .status("PENDING")
                .isPublished(false)
                .build();
        episodeRepository.save(unpublished);

        Long count = episodeRepository.countByWorkIdAndIsPublishedTrue(workId);

        assertEquals(1L, count);
    }

    @Test
    void testFindMaxEpisodeNumberByWorkId() {
        episodeRepository.save(testEpisode);

        Episode episode2 = Episode.builder()
                .workId(workId)
                .episodeNumber(5)
                .title("Episode 5")
                .novelText("Text")
                .isFree(true)
                .coinPrice(0)
                .status("COMPLETED")
                .isPublished(true)
                .build();
        episodeRepository.save(episode2);

        Optional<Integer> maxNumber = episodeRepository.findMaxEpisodeNumberByWorkId(workId);

        assertTrue(maxNumber.isPresent());
        assertEquals(5, maxNumber.get());
    }

    @Test
    void testFindMaxEpisodeNumberByWorkIdNoEpisodes() {
        Optional<Integer> maxNumber = episodeRepository.findMaxEpisodeNumberByWorkId("nonexistent");

        assertFalse(maxNumber.isPresent());
    }
}
