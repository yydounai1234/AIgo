package com.aigo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchases",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_episode_id", columnList = "episode_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_episode", columnNames = {"user_id", "episode_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;
    
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;
    
    @Column(name = "episode_id", nullable = false, length = 36)
    private String episodeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Episode episode;
    
    @Column(name = "coin_cost", nullable = false)
    private Integer coinCost;
    
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;
    
    @PrePersist
    protected void onCreate() {
        purchasedAt = LocalDateTime.now();
    }
}
