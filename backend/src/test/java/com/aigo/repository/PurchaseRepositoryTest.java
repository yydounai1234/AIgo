package com.aigo.repository;

import com.aigo.entity.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PurchaseRepositoryTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    private Purchase testPurchase;
    private String userId;
    private String episodeId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        episodeId = "episode456";
        testPurchase = Purchase.builder()
                .userId(userId)
                .episodeId(episodeId)
                .coinPrice(10)
                .build();
    }

    @Test
    void testSavePurchase() {
        Purchase saved = purchaseRepository.save(testPurchase);

        assertNotNull(saved.getId());
        assertEquals(userId, saved.getUserId());
        assertEquals(episodeId, saved.getEpisodeId());
        assertEquals(10, saved.getCoinPrice());
    }

    @Test
    void testExistsByUserIdAndEpisodeId() {
        purchaseRepository.save(testPurchase);

        assertTrue(purchaseRepository.existsByUserIdAndEpisodeId(userId, episodeId));
        assertFalse(purchaseRepository.existsByUserIdAndEpisodeId("otheruser", episodeId));
        assertFalse(purchaseRepository.existsByUserIdAndEpisodeId(userId, "otherepisode"));
    }

    @Test
    void testMultiplePurchases() {
        purchaseRepository.save(testPurchase);

        Purchase purchase2 = Purchase.builder()
                .userId(userId)
                .episodeId("episode789")
                .coinPrice(20)
                .build();
        purchaseRepository.save(purchase2);

        assertTrue(purchaseRepository.existsByUserIdAndEpisodeId(userId, episodeId));
        assertTrue(purchaseRepository.existsByUserIdAndEpisodeId(userId, "episode789"));
    }
}
