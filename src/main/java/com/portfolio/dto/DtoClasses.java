package com.portfolio.dto;

import com.portfolio.model.Event;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DtoClasses {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EventSummaryDTO {
        private Long id;
        private String title;
        private String description;
        private LocalDate eventDate;
        private String location;
        private Event.EventCategory category;
        private Boolean featured;
        private Boolean isPrivate;
        private String coverPhoto;
        private long photoCount;
        private long videoCount;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EventDetailDTO {
        private Long id;
        private String title;
        private String description;
        private LocalDate eventDate;
        private String location;
        private Event.EventCategory category;
        private Boolean featured;
        private Boolean isPrivate;
        private String coverPhoto;
        private List<PhotoDTO> photos;
        private List<VideoDTO> videos;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PhotoDTO {
        private Long id;
        private String filePath;
        private String thumbnailPath;
        private String caption;
        private Boolean isGalleryBest;
        private Integer sortOrder;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VideoDTO {
        private Long id;
        private String filePath;
        private String thumbnailPath;
        private String title;
        private String description;
        private Integer durationSeconds;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class EventCreateRequest {
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 200)
        private String title;
        private String description;
        private LocalDate eventDate;
        @Size(max = 300)
        private String location;
        private Event.EventCategory category;
        private Boolean featured = false;
        private Boolean isPrivate = false;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ContactFormRequest {
        @NotBlank(message = "Votre nom est obligatoire")
        private String name;
        @NotBlank @Email(message = "Email invalide")
        private String email;
        private String phone;
        @NotBlank @Size(min = 10, max = 2000)
        private String message;
        private String eventType;
        private String eventDateRequested;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AuthRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private String email;
        private long expiresIn;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> ok(T data) {
            return ApiResponse.<T>builder().success(true).data(data).build();
        }
        public static <T> ApiResponse<T> ok(String message, T data) {
            return ApiResponse.<T>builder().success(true).message(message).data(data).build();
        }
        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder().success(false).message(message).build();
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PagedResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ContactMessageDTO {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String message;
        private String eventType;
        private String eventDateRequested;
        private Boolean read;
        private LocalDateTime sentAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DashboardStatsDTO {
        private long totalEvents;
        private long totalPhotos;
        private long totalVideos;
        private long unreadMessages;
        private List<CategoryStatDTO> categoryStats;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CategoryStatDTO {
        private Event.EventCategory category;
        private long count;
    }

    // ── Galerie Privée ────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreatePrivateGalleryRequest {
        @NotNull(message = "L'ID événement est obligatoire")
        private Long eventId;
        @NotBlank(message = "Le nom du client est obligatoire")
        private String clientName;
        private String clientEmail;
        private String password;          // null = pas de mot de passe
        private Integer expirationDays;   // null = permanent
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PrivateGalleryDTO {
        private Long id;
        private Long eventId;
        private String eventTitle;
        private String accessToken;
        private String clientName;
        private String clientEmail;
        private boolean hasPassword;
        private java.time.LocalDateTime expiresAt;
        private java.time.LocalDateTime createdAt;
        private Integer downloadCount;
        private Integer viewCount;
        private Boolean active;
        private boolean expired;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PrivateGalleryAccessRequest {
        private String password;   // null si pas de mot de passe requis
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PrivateGalleryContentDTO {
        private Long galleryId;
        private String clientName;
        private String eventTitle;
        private java.time.LocalDate eventDate;
        private String eventLocation;
        private java.time.LocalDateTime expiresAt;
        private List<PhotoDTO> photos;
        private List<Long> favoritePhotoIds;
        private Integer totalPhotos;
    }
}
