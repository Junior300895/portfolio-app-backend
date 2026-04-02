package com.portfolio.repository;

import com.portfolio.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // ── Routes publiques : exclure les événements privés ──
    Page<Event> findByIsPrivateFalseOrderByEventDateDesc(Pageable pageable);

    Page<Event> findByCategoryAndIsPrivateFalseOrderByEventDateDesc(Event.EventCategory category, Pageable pageable);

    List<Event> findByFeaturedTrueAndIsPrivateFalseOrderByEventDateDesc();

    @Query("SELECT e FROM Event e WHERE e.isPrivate = false AND (" +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.location) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Event> searchPublic(@Param("q") String query, Pageable pageable);

    // ── Routes admin : tous les événements ──
    Page<Event> findAllByOrderByEventDateDesc(Pageable pageable);

    Page<Event> findByCategoryOrderByEventDateDesc(Event.EventCategory category, Pageable pageable);

    List<Event> findByFeaturedTrueOrderByEventDateDesc();

    @Query("SELECT e FROM Event e WHERE " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.location) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Event> search(@Param("q") String query, Pageable pageable);

    long countByCategory(Event.EventCategory category);
}
