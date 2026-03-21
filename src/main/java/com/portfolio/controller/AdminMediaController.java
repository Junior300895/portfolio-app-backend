package com.portfolio.controller;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
public class AdminMediaController {

    private final EventService eventService;

    @DeleteMapping("/photos/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(@PathVariable Long id) {
        eventService.deletePhoto(id);
        return ResponseEntity.ok(ApiResponse.ok("Photo supprimée", null));
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable Long id) {
        eventService.deleteVideo(id);
        return ResponseEntity.ok(ApiResponse.ok("Vidéo supprimée", null));
    }

    @PutMapping("/photos/{id}/toggle-best")
    public ResponseEntity<ApiResponse<PhotoDTO>> toggleBest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.toggleGalleryBest(id)));
    }
}
