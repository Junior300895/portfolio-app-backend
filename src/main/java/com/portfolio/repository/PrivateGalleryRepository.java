package com.portfolio.repository;

import com.portfolio.model.PrivateGallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PrivateGalleryRepository extends JpaRepository<PrivateGallery, Long> {

    Optional<PrivateGallery> findByAccessToken(String accessToken);

    Page<PrivateGallery> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(g) FROM PrivateGallery g WHERE g.active = true")
    long countActive();
}
