package com.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "private_gallery", indexes = {
    @Index(name = "idx_pg_token", columnList = "access_token", unique = true),
    @Index(name = "idx_pg_event", columnList = "event_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien vers l'événement dont les photos sont partagées
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Token unique dans l'URL : /galerie-privee/{token}
    @Column(name = "access_token", nullable = false, unique = true, length = 64)
    private String accessToken;

    // Mot de passe optionnel (null = accès par lien uniquement)
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    // Nom du client (pour l'admin)
    @Column(name = "client_name", nullable = false, length = 200)
    private String clientName;

    // Email du client (pour lui envoyer le lien)
    @Column(name = "client_email", length = 200)
    private String clientEmail;

    // Date d'expiration (null = permanent)
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Nombre de téléchargements effectués
    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;

    // Nombre de vues
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }
}
