package com.portfolio.repository;

import com.portfolio.model.PhotoFavorite;
import com.portfolio.model.PrivateGallery;
import com.portfolio.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhotoFavoriteRepository extends JpaRepository<PhotoFavorite, Long> {

    List<PhotoFavorite> findByGallery(PrivateGallery gallery);

    Optional<PhotoFavorite> findByGalleryAndPhoto(PrivateGallery gallery, Photo photo);

    boolean existsByGalleryAndPhoto(PrivateGallery gallery, Photo photo);

    void deleteByGalleryAndPhoto(PrivateGallery gallery, Photo photo);
}
