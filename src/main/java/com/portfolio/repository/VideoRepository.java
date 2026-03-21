package com.portfolio.repository;

import com.portfolio.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByEventIdOrderByUploadedAtAsc(Long eventId);
    void deleteByEventId(Long eventId);
    long countByEventId(Long eventId);
}
