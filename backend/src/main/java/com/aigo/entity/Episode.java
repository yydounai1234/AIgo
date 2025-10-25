package com.aigo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "episodes", 
    indexes = {
        @Index(name = "idx_work_id", columnList = "work_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_work_episode", columnNames = {"work_id", "episode_number"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Episode {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;
    
    @Column(name = "work_id", nullable = false, length = 36)
    private String workId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", insertable = false, updatable = false)
    private Work work;
    
    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(name = "novel_text", nullable = false, columnDefinition = "TEXT")
    private String novelText;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<SceneData> scenes;
    
    @Column(name = "is_free", nullable = false)
    @Builder.Default
    private Boolean isFree = true;
    
    @Column(name = "coin_price", nullable = false)
    @Builder.Default
    private Integer coinPrice = 0;
    
    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Purchase> purchases = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneData {
        private Integer id;
        private String text;
        private String imageUrl;
    }
}
