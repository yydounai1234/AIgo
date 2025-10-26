package com.aigo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scene_number", nullable = false)
    private Integer sceneNumber;
    
    @Column(length = 100)
    private String character;
    
    @Column(columnDefinition = "TEXT")
    private String dialogue;
    
    @Column(name = "visual_description", columnDefinition = "TEXT")
    private String visualDescription;
    
    @Column(columnDefinition = "TEXT")
    private String atmosphere;
    
    @Column(columnDefinition = "TEXT")
    private String action;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
