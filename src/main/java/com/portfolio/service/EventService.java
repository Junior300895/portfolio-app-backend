package com.portfolio.service;

import com.portfolio.dto.DtoClasses.*;
import com.portfolio.model.Event;
import com.portfolio.model.Photo;
import com.portfolio.model.Video;
import com.portfolio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final PhotoRepository photoRepository;
    private final VideoRepository videoRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public PagedResponse<EventSummaryDTO> getEvents(int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        Page<Event> result;

        if (category != null && !category.isBlank()) {
            Event.EventCategory cat = Event.EventCategory.valueOf(category.toUpperCase());
            result = eventRepository.findByCategoryOrderByEventDateDesc(cat, pageable);
        } else {
            result = eventRepository.findAllByOrderByEventDateDesc(pageable);
        }

        List<EventSummaryDTO> dtos = result.getContent().stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());

        return PagedResponse.<EventSummaryDTO>builder()
                .content(dtos)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public EventDetailDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Événement non trouvé: " + id));
        return toDetailDTO(event);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryDTO> getFeaturedEvents() {
        return eventRepository.findByFeaturedTrueOrderByEventDateDesc()
                .stream().map(this::toSummaryDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return Arrays.stream(Event.EventCategory.values())
                .map(Enum::name).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<EventSummaryDTO> searchEvents(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> result = eventRepository.search(query, pageable);
        return PagedResponse.<EventSummaryDTO>builder()
                .content(result.getContent().stream().map(this::toSummaryDTO).collect(Collectors.toList()))
                .page(result.getNumber()).size(result.getSize())
                .totalElements(result.getTotalElements()).totalPages(result.getTotalPages())
                .last(result.isLast()).build();
    }

    public EventDetailDTO createEvent(EventCreateRequest req) {
        Event event = Event.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .eventDate(req.getEventDate())
                .location(req.getLocation())
                .category(req.getCategory())
                .featured(req.getFeatured() != null ? req.getFeatured() : false)
                .build();
        return toDetailDTO(eventRepository.save(event));
    }

    public EventDetailDTO updateEvent(Long id, EventCreateRequest req) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Événement non trouvé: " + id));
        event.setTitle(req.getTitle());
        if (req.getDescription() != null) event.setDescription(req.getDescription());
        if (req.getEventDate() != null) event.setEventDate(req.getEventDate());
        if (req.getLocation() != null) event.setLocation(req.getLocation());
        if (req.getCategory() != null) event.setCategory(req.getCategory());
        if (req.getFeatured() != null) event.setFeatured(req.getFeatured());
        return toDetailDTO(eventRepository.save(event));
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Événement non trouvé: " + id));
        // Delete associated files
        event.getPhotos().forEach(p -> {
            storageService.deleteFile(p.getFilePath());
            storageService.deleteFile(p.getThumbnailPath());
        });
        event.getVideos().forEach(v -> storageService.deleteFile(v.getFilePath()));
        eventRepository.delete(event);
    }

    public PhotoDTO addPhoto(Long eventId, MultipartFile file, String caption) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Événement non trouvé: " + eventId));

        String filePath = storageService.storePhoto(file, eventId);
        // Cloudinary génère la miniature via transformation d'URL (pas d'upload supplémentaire)
        String thumbPath = storageService.buildThumbnailUrl(filePath);

        long count = photoRepository.countByEventId(eventId);
        Photo photo = Photo.builder()
                .event(event)
                .filePath(filePath)
                .thumbnailPath(thumbPath)
                .originalFilename(file.getOriginalFilename())
                .caption(caption)
                .fileSize(file.getSize())
                .sortOrder((int) count)
                .build();

        // Set as cover if first photo
        if (count == 0 && event.getCoverPhoto() == null) {
            event.setCoverPhoto(thumbPath);
            eventRepository.save(event);
        }

        return toPhotoDTO(photoRepository.save(photo));
    }

    public VideoDTO addVideo(Long eventId, MultipartFile file, String title, String description) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Événement non trouvé: " + eventId));

        String filePath = storageService.storeVideo(file, eventId);
        // Cloudinary génère la vignette vidéo via transformation d'URL
        String posterPath = storageService.buildVideoPosterUrl(filePath);

        Video video = Video.builder()
                .event(event)
                .filePath(filePath)
                .thumbnailPath(posterPath)
                .title(title != null ? title : file.getOriginalFilename())
                .description(description)
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .build();

        return toVideoDTO(videoRepository.save(video));
    }

    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Photo non trouvée: " + photoId));
        storageService.deleteFile(photo.getFilePath());
        storageService.deleteFile(photo.getThumbnailPath());
        photoRepository.delete(photo);
    }

    public void deleteVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Vidéo non trouvée: " + videoId));
        storageService.deleteFile(video.getFilePath());
        videoRepository.delete(video);
    }

    public PhotoDTO toggleGalleryBest(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Photo non trouvée: " + photoId));
        photo.setIsGalleryBest(!photo.getIsGalleryBest());
        return toPhotoDTO(photoRepository.save(photo));
    }

    @Transactional(readOnly = true)
    public List<PhotoDTO> getGalleryBestPhotos() {
        return photoRepository.findByIsGalleryBestTrueOrderByUploadedAtDesc()
                .stream().map(this::toPhotoDTO).collect(Collectors.toList());
    }

    // ── Mappers ──────────────────────────────────────────────

    private EventSummaryDTO toSummaryDTO(Event e) {
        return EventSummaryDTO.builder()
                .id(e.getId()).title(e.getTitle()).description(e.getDescription())
                .eventDate(e.getEventDate()).location(e.getLocation())
                .category(e.getCategory()).featured(e.getFeatured())
                .coverPhoto(e.getCoverPhoto())
                .photoCount(photoRepository.countByEventId(e.getId()))
                .videoCount(videoRepository.countByEventId(e.getId()))
                .createdAt(e.getCreatedAt())
                .build();
    }

    private EventDetailDTO toDetailDTO(Event e) {
        return EventDetailDTO.builder()
                .id(e.getId()).title(e.getTitle()).description(e.getDescription())
                .eventDate(e.getEventDate()).location(e.getLocation())
                .category(e.getCategory()).featured(e.getFeatured())
                .coverPhoto(e.getCoverPhoto())
                .photos(e.getPhotos().stream().map(this::toPhotoDTO).collect(Collectors.toList()))
                .videos(e.getVideos().stream().map(this::toVideoDTO).collect(Collectors.toList()))
                .createdAt(e.getCreatedAt())
                .build();
    }

    private PhotoDTO toPhotoDTO(Photo p) {
        return PhotoDTO.builder()
                .id(p.getId()).filePath(p.getFilePath()).thumbnailPath(p.getThumbnailPath())
                .caption(p.getCaption()).isGalleryBest(p.getIsGalleryBest()).sortOrder(p.getSortOrder())
                .build();
    }

    private VideoDTO toVideoDTO(Video v) {
        return VideoDTO.builder()
                .id(v.getId()).filePath(v.getFilePath()).thumbnailPath(v.getThumbnailPath())
                .title(v.getTitle()).description(v.getDescription()).durationSeconds(v.getDurationSeconds())
                .build();
    }
}
