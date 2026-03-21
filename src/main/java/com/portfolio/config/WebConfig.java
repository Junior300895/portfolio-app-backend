package com.portfolio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig — simplifié avec Cloudinary.
 *
 * Le service de fichiers locaux (/uploads/**) est supprimé.
 * Les médias sont désormais servis directement depuis le CDN Cloudinary.
 * Aucune configuration de ressources statiques nécessaire.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Configuration minimale — les médias sont sur Cloudinary CDN
}
