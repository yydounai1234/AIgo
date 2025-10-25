package com.aigo.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpisodeTest {

    @Test
    void testEpisodeBuilder() {
        Episode episode = Episode.builder()
                .workId("work123")
                .episodeNumber(1)
                .title("Episode 1")
                .novelText("Novel text")
                .isFree(true)
                .coinPrice(0)
                .status("COMPLETED")
                .isPublished(true)
                .style("Anime")
                .targetAudience("Teen")
                .build();

        assertEquals("work123", episode.getWorkId());
        assertEquals(1, episode.getEpisodeNumber());
        assertEquals("Episode 1", episode.getTitle());
        assertEquals("Novel text", episode.getNovelText());
        assertTrue(episode.getIsFree());
        assertEquals(0, episode.getCoinPrice());
        assertEquals("COMPLETED", episode.getStatus());
        assertTrue(episode.getIsPublished());
        assertEquals("Anime", episode.getStyle());
        assertEquals("Teen", episode.getTargetAudience());
    }

    @Test
    void testEpisodeSettersAndGetters() {
        Episode episode = new Episode();
        episode.setId("ep123");
        episode.setWorkId("work123");
        episode.setEpisodeNumber(2);
        episode.setTitle("Title");
        episode.setNovelText("Text");
        episode.setIsFree(false);
        episode.setCoinPrice(10);
        episode.setStatus("PENDING");
        episode.setIsPublished(false);

        assertEquals("ep123", episode.getId());
        assertEquals("work123", episode.getWorkId());
        assertEquals(2, episode.getEpisodeNumber());
        assertEquals("Title", episode.getTitle());
        assertEquals("Text", episode.getNovelText());
        assertFalse(episode.getIsFree());
        assertEquals(10, episode.getCoinPrice());
        assertEquals("PENDING", episode.getStatus());
        assertFalse(episode.getIsPublished());
    }
}
