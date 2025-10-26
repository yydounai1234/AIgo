package com.aigo.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkTest {

    @Test
    void testWorkBuilder() {
        Work work = Work.builder()
                .userId("user123")
                .title("Test Work")
                .description("Description")
                .isPublic(true)
                .likesCount(5)
                .coverImage("image.jpg")
                .build();

        assertEquals("user123", work.getUserId());
        assertEquals("Test Work", work.getTitle());
        assertEquals("Description", work.getDescription());
        assertTrue(work.getIsPublic());
        assertEquals(5, work.getLikesCount());
        assertEquals("image.jpg", work.getCoverImage());
    }

    @Test
    void testWorkSettersAndGetters() {
        Work work = new Work();
        work.setId("work123");
        work.setUserId("user123");
        work.setTitle("Title");
        work.setDescription("Desc");
        work.setIsPublic(false);
        work.setLikesCount(10);
        work.setCoverImage("cover.jpg");

        assertEquals("work123", work.getId());
        assertEquals("user123", work.getUserId());
        assertEquals("Title", work.getTitle());
        assertEquals("Desc", work.getDescription());
        assertFalse(work.getIsPublic());
        assertEquals(10, work.getLikesCount());
        assertEquals("cover.jpg", work.getCoverImage());
    }
}
