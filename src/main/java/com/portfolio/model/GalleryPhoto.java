package com.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Photo de la galerie « best-of ».
 * <p>
 * Contrairement à {@link Photo}, cette entité n'est PAS liée au cycle de vie
 * d'un événement : elle stocke sa propre copie des URLs Cloudinary. Ainsi, la
 * suppression d'un événement ne fait plus disparaître les best-of.
 * {@code sourcePhotoId} garde une trace de la photo d'origine (sans clé
 * étrangère) pour pouvoir synchroniser l'état lors du toggle depuis l'admin.
 */
@Entity
@Table(name = "gallery_photo", indexes = {
    @Index(name = "idx_gallery_source", columnList = "source_photo_id"),
    @Index(name = "idx_gallery_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id de la photo d'événement d'origine (peut devenir orphelin après suppression). */
    @Column(name = "source_photo_id")
    private Long sourcePhotoId;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(length = 500)
    private String caption;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
