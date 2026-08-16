package com.portfolio.controller;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.service.EventService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final EventService eventService;

    @GetMapping("/best")
    public ResponseEntity<ApiResponse<List<PhotoDTO>>> getBestPhotos() {
        // Cache court : la galerie best-of évolue quand l'admin l'édite, il faut
        // que les changements apparaissent rapidement côté public.
        return ResponseEntity.ok()
        // .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
        .body(ApiResponse.ok(eventService.getGalleryBestPhotos()));
    }
}
