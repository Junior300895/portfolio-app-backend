package com.portfolio.service;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.model.*;
import com.portfolio.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrivateGalleryService {

    private final PrivateGalleryRepository galleryRepository;
    private final PhotoFavoriteRepository favoriteRepository;
    private final EventRepository eventRepository;
    private final PhotoRepository photoRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Admin : créer une galerie privée ─────────────────────────

    public PrivateGalleryDTO createGallery(CreatePrivateGalleryRequest req) {
        Event event = eventRepository.findById(req.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Événement non trouvé: " + req.getEventId()));

        String token = generateToken();
        String hash = (req.getPassword() != null && !req.getPassword().isBlank())
                ? passwordEncoder.encode(req.getPassword())
                : null;

        LocalDateTime expiresAt = (req.getExpirationDays() != null && req.getExpirationDays() > 0)
                ? LocalDateTime.now().plusDays(req.getExpirationDays())
                : null;

        PrivateGallery gallery = PrivateGallery.builder()
                .event(event)
                .accessToken(token)
                .passwordHash(hash)
                .clientName(req.getClientName())
                .clientEmail(req.getClientEmail())
                .expiresAt(expiresAt)
                .build();

        gallery = galleryRepository.save(gallery);
        log.info("Galerie privée créée pour {} — token: {}", req.getClientName(), token);
        return toDTO(gallery);
    }

    // ── Admin : liste de toutes les galeries ─────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<PrivateGalleryDTO> getAllGalleries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PrivateGallery> result = galleryRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<PrivateGalleryDTO> dtos = result.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return PagedResponse.<PrivateGalleryDTO>builder()
                .content(dtos)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    // ── Admin : désactiver/supprimer une galerie ─────────────────

    public void deactivateGallery(Long id) {
        PrivateGallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Galerie non trouvée: " + id));
        gallery.setActive(false);
        galleryRepository.save(gallery);
    }

    public void deleteGallery(Long id) {
        if (!galleryRepository.existsById(id))
            throw new EntityNotFoundException("Galerie non trouvée: " + id);
        galleryRepository.deleteById(id);
    }

    // ── Client : vérifier accès par token ────────────────────────

    @Transactional(readOnly = true)
    public boolean requiresPassword(String token) {
        PrivateGallery gallery = findActiveGallery(token);
        return gallery.hasPassword();
    }

    // ── Client : accéder au contenu ──────────────────────────────

    public PrivateGalleryContentDTO accessGallery(String token, String password) {
        PrivateGallery gallery = findActiveGallery(token);

        if (gallery.hasPassword()) {
            if (password == null || !passwordEncoder.matches(password, gallery.getPasswordHash())) {
                throw new SecurityException("Mot de passe incorrect");
            }
        }

        // Incrémenter le compteur de vues
        gallery.setViewCount(gallery.getViewCount() + 1);
        galleryRepository.save(gallery);

        Event event = gallery.getEvent();
        List<PhotoDTO> photos = event.getPhotos().stream()
                .map(p -> PhotoDTO.builder()
                        .id(p.getId())
                        .filePath(p.getFilePath())
                        .thumbnailPath(p.getThumbnailPath())
                        .caption(p.getCaption())
                        .isGalleryBest(p.getIsGalleryBest())
                        .sortOrder(p.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        List<Long> favoriteIds = favoriteRepository.findByGallery(gallery)
                .stream().map(f -> f.getPhoto().getId()).collect(Collectors.toList());

        return PrivateGalleryContentDTO.builder()
                .galleryId(gallery.getId())
                .clientName(gallery.getClientName())
                .eventTitle(event.getTitle())
                .eventDate(event.getEventDate())
                .eventLocation(event.getLocation())
                .expiresAt(gallery.getExpiresAt())
                .photos(photos)
                .favoritePhotoIds(favoriteIds)
                .totalPhotos(photos.size())
                .build();
    }

    // ── Client : toggle favori ────────────────────────────────────

    public boolean toggleFavorite(String token, Long photoId) {
        PrivateGallery gallery = findActiveGallery(token);
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Photo non trouvée: " + photoId));

        if (favoriteRepository.existsByGalleryAndPhoto(gallery, photo)) {
            favoriteRepository.deleteByGalleryAndPhoto(gallery, photo);
            return false; // retiré des favoris
        } else {
            PhotoFavorite fav = PhotoFavorite.builder()
                    .gallery(gallery)
                    .photo(photo)
                    .build();
            favoriteRepository.save(fav);
            return true; // ajouté aux favoris
        }
    }

    // ── Client : incrémenter téléchargement ──────────────────────

    public void incrementDownload(String token) {
        PrivateGallery gallery = findActiveGallery(token);
        gallery.setDownloadCount(gallery.getDownloadCount() + 1);
        galleryRepository.save(gallery);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private PrivateGallery findActiveGallery(String token) {
        PrivateGallery gallery = galleryRepository.findByAccessToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Galerie introuvable ou lien invalide"));
        if (!gallery.getActive())
            throw new SecurityException("Cette galerie a été désactivée");
        if (gallery.isExpired())
            throw new SecurityException("Ce lien a expiré");
        return gallery;
    }

    private String generateToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "")
                  + UUID.randomUUID().toString().replace("-", "");
            // 2x UUID sans tirets = exactement 64 chars, on prend les 48 premiers
            if (token.length() >= 48) {
                token = token.substring(0, 48);
            }
            // sinon on garde les 64 chars complets — ne devrait jamais arriver
        } while (galleryRepository.findByAccessToken(token).isPresent());
        return token;
    }

    private PrivateGalleryDTO toDTO(PrivateGallery g) {
        return PrivateGalleryDTO.builder()
                .id(g.getId())
                .eventId(g.getEvent().getId())
                .eventTitle(g.getEvent().getTitle())
                .accessToken(g.getAccessToken())
                .clientName(g.getClientName())
                .clientEmail(g.getClientEmail())
                .hasPassword(g.hasPassword())
                .expiresAt(g.getExpiresAt())
                .createdAt(g.getCreatedAt())
                .downloadCount(g.getDownloadCount())
                .viewCount(g.getViewCount())
                .active(g.getActive())
                .expired(g.isExpired())
                .build();
    }
}
