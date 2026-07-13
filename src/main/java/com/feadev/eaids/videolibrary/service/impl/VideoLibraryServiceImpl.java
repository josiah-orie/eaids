package com.feadev.eaids.videolibrary.service.impl;

import com.feadev.eaids.registrations.model.AthleteProfile;
import com.feadev.eaids.registrations.model.AthleteVideo;
import com.feadev.eaids.registrations.model.enums.VideoType;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.AthleteVideoRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.videolibrary.dto.request.CreateVideoRequest;
import com.feadev.eaids.videolibrary.dto.request.UpdateVideoRequest;
import com.feadev.eaids.videolibrary.dto.response.VideoLibraryResponse;
import com.feadev.eaids.videolibrary.dto.response.VideoLibraryStatsResponse;
import com.feadev.eaids.videolibrary.model.VideoLibraryItem;
import com.feadev.eaids.videolibrary.model.enums.VideoCategory;
import com.feadev.eaids.videolibrary.repository.VideoLibraryRepository;
import com.feadev.eaids.videolibrary.service.VideoLibraryService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoLibraryServiceImpl implements VideoLibraryService {

    private final VideoLibraryRepository videoRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final AthleteVideoRepository athleteVideoRepository;

    @Override
    public VideoLibraryResponse create(CreateVideoRequest request, UUID authenticatedUserId) {
        VideoLibraryItem item = VideoLibraryItem.builder()
                .athleteUserId(authenticatedUserId)
                .title(request.getTitle())
                .description(request.getDescription())
                .url(request.getUrl())
                .videoType(request.getVideoType())
                .category(request.getCategory())
                .position(request.getPosition())
                .campId(request.getCampId())
                .uploadedBy(authenticatedUserId)
                .isPublic(true)
                .build();

        VideoLibraryItem saved = videoRepository.save(item);
        log.info("Video library item created: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VideoLibraryResponse> search(VideoCategory category, FootballPosition position, UUID campId,
                                              UUID athleteId, VideoType videoType, String searchTerm,
                                              int page, int size, String sort) {
        Sort sortOrder = switch (sort != null ? sort : "newest") {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdDate");
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            default -> Sort.by(Sort.Direction.DESC, "createdDate");
        };
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Specification<VideoLibraryItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (position != null) predicates.add(cb.equal(root.get("position"), position));
            if (campId != null) predicates.add(cb.equal(root.get("campId"), campId));
            if (athleteId != null) predicates.add(cb.equal(root.get("athleteUserId"), athleteId));
            if (videoType != null) predicates.add(cb.equal(root.get("videoType"), videoType));
            if (searchTerm != null && !searchTerm.isBlank()) {
                String like = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return videoRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoLibraryResponse getById(UUID id) {
        VideoLibraryItem item = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));
        return mapToResponse(item);
    }

    @Override
    public VideoLibraryResponse update(UUID id, UpdateVideoRequest request, UUID authenticatedUserId) {
        VideoLibraryItem item = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));

        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getPosition() != null) item.setPosition(request.getPosition());
        if (request.getCampId() != null) item.setCampId(request.getCampId());
        if (request.getIsPublic() != null) item.setPublic(request.getIsPublic());

        VideoLibraryItem saved = videoRepository.save(item);
        return mapToResponse(saved);
    }

    @Override
    public void delete(UUID id, UUID authenticatedUserId) {
        VideoLibraryItem item = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));
        videoRepository.delete(item);
        log.info("Video library item deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoLibraryStatsResponse getStats() {
        long total = videoRepository.count();
        List<VideoLibraryStatsResponse.CategoryCount> byCategory = Arrays.stream(VideoCategory.values())
                .map(cat -> VideoLibraryStatsResponse.CategoryCount.builder()
                        .category(cat.name())
                        .count(videoRepository.countByCategory(cat))
                        .build())
                .filter(c -> c.getCount() > 0)
                .collect(Collectors.toList());

        return VideoLibraryStatsResponse.builder()
                .totalVideos(total)
                .byCategory(byCategory)
                .build();
    }

    @Override
    public Map<String, Object> migrateFromAthleteVideos() {
        List<AthleteVideo> allVideos = athleteVideoRepository.findAll();
        int migrated = 0;
        int skipped = 0;

        for (AthleteVideo video : allVideos) {
            AthleteProfile profile = video.getAthleteProfile();
            UUID userId = profile.getUser().getId();

            if (videoRepository.existsByUrlAndAthleteUserId(video.getUrl(), userId)) {
                skipped++;
                continue;
            }

            VideoLibraryItem item = VideoLibraryItem.builder()
                    .athleteUserId(userId)
                    .title("Athlete Video")
                    .url(video.getUrl())
                    .videoType(video.getVideoType())
                    .category(VideoCategory.CAMP_HIGHLIGHT)
                    .uploadedBy(userId)
                    .isPublic(true)
                    .build();

            videoRepository.save(item);
            migrated++;
        }

        log.info("Video migration complete: migrated={}, skipped={}", migrated, skipped);
        return Map.of("migrated", migrated, "skipped", skipped, "total", migrated + skipped);
    }

    private VideoLibraryResponse mapToResponse(VideoLibraryItem item) {
        String athleteName = athleteProfileRepository.findByUser_Id(item.getAthleteUserId())
                .map(p -> p.getFullName()).orElse("Unknown");

        return VideoLibraryResponse.builder()
                .id(item.getId())
                .athleteUserId(item.getAthleteUserId())
                .athleteName(athleteName)
                .title(item.getTitle())
                .description(item.getDescription())
                .url(item.getUrl())
                .videoType(item.getVideoType().name())
                .category(item.getCategory().name())
                .position(item.getPosition() != null ? item.getPosition().name() : null)
                .campId(item.getCampId())
                .uploadedBy(item.getUploadedBy())
                .isPublic(item.isPublic())
                .createdDate(item.getCreatedDate())
                .lastModifiedDate(item.getLastModifiedDate())
                .build();
    }
}
