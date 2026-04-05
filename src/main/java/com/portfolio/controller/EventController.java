package com.portfolio.controller;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<EventSummaryDTO>>> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES).cachePublic())
        .body(ApiResponse.ok(eventService.getEvents(page, size, category)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDetailDTO>> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok()
                // .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.ok(eventService.getEventById(id)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<EventSummaryDTO>>> getFeatured() {
         return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.ok(eventService.getFeaturedEvents()));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getCategories()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<EventSummaryDTO>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.searchEvents(q, page, size)));
    }
}
