package com.portfolio.repository;

import com.portfolio.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByEventIdOrderBySortOrderAsc(Long eventId);
    List<Photo> findByIsGalleryBestTrueOrderByUploadedAtDesc();
    void deleteByEventId(Long eventId);
    long countByEventId(Long eventId);
}
