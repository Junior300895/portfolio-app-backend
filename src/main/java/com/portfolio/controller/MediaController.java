package com.portfolio.controller;

import com.portfolio.dto.DtoClasses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * MediaController — obsolète avec Cloudinary.
 *
 * Les URLs des médias sont désormais des URLs Cloudinary directes (CDN mondial).
 * Ce controller est conservé pour la compatibilité mais n'est plus utilisé
 * pour servir des fichiers locaux.
 *
 * Les URLs retournées par l'API sont du type :
 *   https://res.cloudinary.com/{cloud_name}/image/upload/...
 *   https://res.cloudinary.com/{cloud_name}/video/upload/...
 *
 * Les miniatures sont générées via transformations Cloudinary dans l'URL :
 *   https://res.cloudinary.com/{cloud_name}/image/upload/c_fill,w_600,h_450,q_auto,f_auto/...
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("Cloudinary storage actif — les médias sont servis via CDN Cloudinary."));
    }
}
