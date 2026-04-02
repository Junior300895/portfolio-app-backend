package com.portfolio.controller;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<EventSummaryDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvents(page, size, null)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDetailDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEventByIdAdmin(id)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getStats() {
        return ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .body(ApiResponse.ok(eventService.getDashboardStats()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventDetailDTO>> create(
            @Valid @RequestBody EventCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Événement créé", eventService.createEvent(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody EventCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Événement modifié", eventService.updateEvent(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.ok("Événement supprimé", null));
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<ApiResponse<PhotoDTO>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String caption) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Photo ajoutée", eventService.addPhoto(id, file, caption)));
    }

    @PostMapping("/{id}/videos")
    public ResponseEntity<ApiResponse<VideoDTO>> uploadVideo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Vidéo ajoutée", eventService.addVideo(id, file, title, description)));
    }
}
