package com.portfolio.repository;

import com.portfolio.model.GalleryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GalleryPhotoRepository extends JpaRepository<GalleryPhoto, Long> {
    List<GalleryPhoto> findAllByOrderByCreatedAtDesc();
    Optional<GalleryPhoto> findBySourcePhotoId(Long sourcePhotoId);
    boolean existsBySourcePhotoId(Long sourcePhotoId);
    boolean existsByFilePath(String filePath);
    void deleteBySourcePhotoId(Long sourcePhotoId);
}
