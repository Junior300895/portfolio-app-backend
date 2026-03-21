package com.portfolio.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service de stockage Cloudinary.
 *
 * Cloudinary gère automatiquement :
 *  - La génération de miniatures via les transformations d'URL
 *  - La compression et l'optimisation des fichiers
 *  - La diffusion CDN mondiale
 *
 * Structure des dossiers dans Cloudinary :
 *  - portfolio/photos/event_{id}/  → images
 *  - portfolio/videos/event_{id}/  → vidéos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final Cloudinary cloudinary;

    // ── Validations ───────────────────────────────────────────────

    private static final List<String> ALLOWED_IMAGE_TYPES =
        Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");
    private static final List<String> ALLOWED_VIDEO_TYPES =
        Arrays.asList("video/mp4", "video/quicktime", "video/x-msvideo", "video/mpeg", "video/webm");
    private static final long MAX_IMAGE_SIZE = 50L  * 1024 * 1024; // 50 MB
    private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024; // 500 MB

    // ── Upload photo ──────────────────────────────────────────────

    /**
     * Upload une photo vers Cloudinary.
     *
     * @return URL sécurisée Cloudinary de la photo originale
     */
    @SuppressWarnings("unchecked")
    public String storePhoto(MultipartFile file, Long eventId) throws IOException {
        validateImage(file);

        String folder = "portfolio/photos/event_" + eventId;

        Map<String, Object> options = ObjectUtils.asMap(
                "folder",          folder,
                "resource_type",   "image",
                "use_filename",    false,
                "unique_filename", true,
                // Transformation auto : qualité optimisée + format WebP si supporté
                "transformation",  "q_auto,f_auto"
        );

        Map<String, Object> result = cloudinary.uploader()
                .upload(file.getBytes(), options);

        String secureUrl = (String) result.get("secure_url");
        log.info("Photo uploadée sur Cloudinary : {}", secureUrl);
        return secureUrl;
    }

    /**
     * Génère l'URL de la miniature via les transformations Cloudinary.
     * Aucun upload supplémentaire — Cloudinary transforme à la volée et met en cache.
     *
     * @param originalUrl URL Cloudinary de la photo originale
     * @return URL de la miniature (600x450, recadrage centré, format optimisé)
     */
    public String buildThumbnailUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) return null;

        // Insérer la transformation dans l'URL Cloudinary
        // Exemple : https://res.cloudinary.com/demo/image/upload/v123/photo.jpg
        //        → https://res.cloudinary.com/demo/image/upload/c_fill,w_600,h_450,q_auto,f_auto/v123/photo.jpg
        return originalUrl.replace(
                "/image/upload/",
                "/image/upload/c_fill,w_600,h_450,q_auto,f_auto/"
        );
    }

    // ── Upload vidéo ──────────────────────────────────────────────

    /**
     * Upload une vidéo vers Cloudinary.
     *
     * @return URL sécurisée Cloudinary de la vidéo
     */
    @SuppressWarnings("unchecked")
    public String storeVideo(MultipartFile file, Long eventId) throws IOException {
        validateVideo(file);

        String folder = "portfolio/videos/event_" + eventId;

        Map<String, Object> options = ObjectUtils.asMap(
                "folder",          folder,
                "resource_type",   "video",
                "use_filename",    false,
                "unique_filename", true,
                "chunk_size",      6 * 1024 * 1024 // Upload par morceaux de 6 MB
        );

        Map<String, Object> result = cloudinary.uploader()
                .uploadLarge(file.getInputStream(), options);

        String secureUrl = (String) result.get("secure_url");
        log.info("Vidéo uploadée sur Cloudinary : {}", secureUrl);
        return secureUrl;
    }

    /**
     * Génère l'URL de la vignette d'une vidéo (première frame).
     *
     * @param videoUrl URL Cloudinary de la vidéo
     * @return URL de l'image de vignette (JPG, 600x450)
     */
    public String buildVideoPosterUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) return null;

        // Remplacer /video/upload/ par /video/upload/<transformation>/
        // et changer l'extension en .jpg pour obtenir une image
        return videoUrl
                .replace("/video/upload/", "/video/upload/c_fill,w_600,h_450,q_auto,so_0/")
                .replaceAll("\\.(mp4|mov|avi|webm)$", ".jpg");
    }

    // ── Suppression ───────────────────────────────────────────────

    /**
     * Supprime un fichier de Cloudinary à partir de son URL.
     * Extrait automatiquement le public_id depuis l'URL.
     */
    @SuppressWarnings("unchecked")
    public void deleteFile(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) return;
        try {
            String publicId = extractPublicId(cloudinaryUrl);
            String resourceType = cloudinaryUrl.contains("/video/") ? "video" : "image";

            Map<String, Object> result = cloudinary.uploader()
                    .destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));

            String resultStr = String.valueOf(result.get("result"));
            if ("ok".equals(resultStr)) {
                log.info("Fichier supprimé de Cloudinary : {}", publicId);
            } else {
                log.warn("Cloudinary delete résultat inattendu : {} pour {}", resultStr, publicId);
            }
        } catch (Exception e) {
            log.warn("Impossible de supprimer le fichier Cloudinary : {} — {}", cloudinaryUrl, e.getMessage());
        }
    }

    // ── Méthode legacy (compatibilité MediaController) ───────────

    /**
     * Méthode conservée pour compatibilité.
     * Avec Cloudinary, les fichiers ne sont plus servis localement —
     * les URLs Cloudinary sont utilisées directement dans le frontend.
     *
     * @deprecated Les URLs Cloudinary sont retournées directement par l'API.
     */
    @Deprecated
    public java.nio.file.Path getFilePath(String urlPath) {
        throw new UnsupportedOperationException(
            "Les fichiers sont désormais sur Cloudinary. Utilisez directement les URLs Cloudinary."
        );
    }

    // ── Helpers privés ────────────────────────────────────────────

    /**
     * Extrait le public_id Cloudinary depuis une URL sécurisée.
     *
     * Exemple :
     * https://res.cloudinary.com/demo/image/upload/v1234567890/portfolio/photos/event_1/abc123.jpg
     * → portfolio/photos/event_1/abc123
     */
    private String extractPublicId(String url) {
        // Trouver la partie après /upload/ (ou /upload/vXXXXX/)
        String marker = "/upload/";
        int uploadIndex = url.indexOf(marker);
        if (uploadIndex == -1) return url;

        String afterUpload = url.substring(uploadIndex + marker.length());

        // Ignorer le numéro de version vXXXXXXXXXX/ si présent
        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }

        // Supprimer l'extension du fichier
        int dotIndex = afterUpload.lastIndexOf('.');
        return dotIndex != -1 ? afterUpload.substring(0, dotIndex) : afterUpload;
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty())
            throw new IllegalArgumentException("Le fichier est vide");
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException("Type non autorisé. Formats acceptés : JPG, PNG, WebP");
        if (file.getSize() > MAX_IMAGE_SIZE)
            throw new IllegalArgumentException("Fichier trop volumineux (max 50 MB)");
    }

    private void validateVideo(MultipartFile file) {
        if (file.isEmpty())
            throw new IllegalArgumentException("Le fichier est vide");
        if (!ALLOWED_VIDEO_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException("Type non autorisé. Formats acceptés : MP4, MOV, AVI, WebM");
        if (file.getSize() > MAX_VIDEO_SIZE)
            throw new IllegalArgumentException("Fichier trop volumineux (max 500 MB)");
    }
}
