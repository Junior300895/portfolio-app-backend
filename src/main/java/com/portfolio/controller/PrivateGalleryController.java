package com.portfolio.controller;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.service.PrivateGalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PrivateGalleryController {

    private final PrivateGalleryService privateGalleryService;

    // ════════════════════════════════════════════════════════════
    // ROUTES ADMIN (JWT requis — sécurisé par SecurityConfig)
    // ════════════════════════════════════════════════════════════

    /** Créer une galerie privée pour un client */
    @PostMapping("/api/admin/private-galleries")
    public ResponseEntity<ApiResponse<PrivateGalleryDTO>> create(
            @Valid @RequestBody CreatePrivateGalleryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok("Galerie privée créée", privateGalleryService.createGallery(req)));
    }

    /** Liste paginée de toutes les galeries privées */
    @GetMapping("/api/admin/private-galleries")
    public ResponseEntity<ApiResponse<PagedResponse<PrivateGalleryDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(privateGalleryService.getAllGalleries(page, size)));
    }

    /** Désactiver une galerie (sans la supprimer) */
    @PutMapping("/api/admin/private-galleries/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        privateGalleryService.deactivateGallery(id);
        return ResponseEntity.ok(ApiResponse.ok("Galerie désactivée", null));
    }

    /** Réactiver une galerie désactivée */
    @PutMapping("/api/admin/private-galleries/{id}/reactivate")
    public ResponseEntity<ApiResponse<Void>> reactivate(@PathVariable Long id) {
        privateGalleryService.reactivateGallery(id);
        return ResponseEntity.ok(ApiResponse.ok("Galerie réactivée", null));
    }

    /** Supprimer définitivement une galerie */
    @DeleteMapping("/api/admin/private-galleries/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        privateGalleryService.deleteGallery(id);
        return ResponseEntity.ok(ApiResponse.ok("Galerie supprimée", null));
    }

    // ════════════════════════════════════════════════════════════
    // ROUTES PUBLIQUES (accès client via token)
    // ════════════════════════════════════════════════════════════

    /** Vérifier si la galerie existe et si un mot de passe est requis */
    @GetMapping("/api/gallery/private/{token}/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfo(
            @PathVariable String token) {
        boolean requiresPassword = privateGalleryService.requiresPassword(token);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(Map.of("requiresPassword", requiresPassword)));
    }

    /** Accéder au contenu de la galerie (avec ou sans mot de passe) */
    @PostMapping("/api/gallery/private/{token}/access")
    public ResponseEntity<ApiResponse<PrivateGalleryContentDTO>> access(
            @PathVariable String token,
            @RequestBody(required = false) PrivateGalleryAccessRequest req) {
        String password = (req != null) ? req.getPassword() : null;
        PrivateGalleryContentDTO content = privateGalleryService.accessGallery(token, password);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(content));
    }

    /** Ajouter/retirer un favori */
    @PostMapping("/api/gallery/private/{token}/favorites/{photoId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFavorite(
            @PathVariable String token,
            @PathVariable Long photoId) {
        boolean added = privateGalleryService.toggleFavorite(token, photoId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("favorited", added)));
    }

    /** Incrémenter le compteur de téléchargement */
    @PostMapping("/api/gallery/private/{token}/download")
    public ResponseEntity<ApiResponse<Void>> trackDownload(@PathVariable String token) {
        privateGalleryService.incrementDownload(token);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
