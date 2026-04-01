package com.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "photo_favorite", indexes = {
    @Index(name = "idx_fav_gallery", columnList = "gallery_id"),
    @Index(name = "idx_fav_unique", columnList = "gallery_id,photo_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gallery_id", nullable = false)
    private PrivateGallery gallery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
