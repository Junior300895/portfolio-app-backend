package com.portfolio.config;

import com.portfolio.model.GalleryPhoto;
import com.portfolio.repository.GalleryPhotoRepository;
import com.portfolio.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Migration ponctuelle : au démarrage, crée une entrée {@link GalleryPhoto}
 * pour chaque photo déjà marquée best-of (isGalleryBest = true) qui n'en a pas
 * encore. Garantit que les best-of existants restent visibles après le passage
 * à la galerie indépendante. Idempotent — ne fait rien aux démarrages suivants.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GalleryBackfillRunner implements ApplicationRunner {

    private final PhotoRepository photoRepository;
    private final GalleryPhotoRepository galleryPhotoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int created = 0;
        for (var photo : photoRepository.findByIsGalleryBestTrueOrderByUploadedAtDesc()) {
            if (!galleryPhotoRepository.existsBySourcePhotoId(photo.getId())) {
                galleryPhotoRepository.save(GalleryPhoto.builder()
                        .sourcePhotoId(photo.getId())
                        .filePath(photo.getFilePath())
                        .thumbnailPath(photo.getThumbnailPath())
                        .caption(photo.getCaption())
                        .sortOrder(photo.getSortOrder())
                        .build());
                created++;
            }
        }
        if (created > 0) {
            log.info("Galerie best-of : {} photo(s) existante(s) migrée(s) vers gallery_photo", created);
        }
    }
}
