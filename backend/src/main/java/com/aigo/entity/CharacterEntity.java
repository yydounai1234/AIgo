package com.aigo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "characters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String appearance;
    
    @Column(columnDefinition = "TEXT")
    private String personality;
    
    @Column(name = "work_id", length = 36)
    private String workId;
    
    @Column(name = "is_protagonist")
    private Boolean isProtagonist = false;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(length = 50)
    private String gender;
    
    @Column(name = "body_type", columnDefinition = "TEXT")
    private String bodyType;
    
    @Column(name = "facial_features", columnDefinition = "TEXT")
    private String facialFeatures;
    
    @Column(name = "hair_type", length = 100)
    private String hairType;
    
    @Column(name = "hair_color", length = 100)
    private String hairColor;
    
    @Column(name = "face_shape", length = 100)
    private String faceShape;
    
    @Column(name = "eye_type", length = 100)
    private String eyeType;
    
    @Column(name = "eye_color", length = 100)
    private String eyeColor;
    
    @Column(name = "nose_type", length = 100)
    private String noseType;
    
    @Column(name = "mouth_type", length = 100)
    private String mouthType;
    
    @Column(name = "skin_tone", length = 100)
    private String skinTone;
    
    @Column(name = "height", length = 100)
    private String height;
    
    @Column(name = "build", length = 100)
    private String build;
    
    @Column(name = "clothing_style", columnDefinition = "TEXT")
    private String clothingStyle;
    
    @Column(name = "distinguishing_features", columnDefinition = "TEXT")
    private String distinguishingFeatures;
    
    @Column(name = "first_image_url", length = 500)
    private String firstImageUrl;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "character_embedding", columnDefinition = "JSON")
    private Map<String, Object> characterEmbedding;
    
    @Column(name = "is_placeholder_name")
    private Boolean isPlaceholderName = false;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nicknames", columnDefinition = "JSON")
    private List<String> nicknames = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
