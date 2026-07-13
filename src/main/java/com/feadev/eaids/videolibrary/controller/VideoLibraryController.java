package com.feadev.eaids.videolibrary.controller;

import com.feadev.eaids.registrations.model.enums.VideoType;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.videolibrary.dto.request.CreateVideoRequest;
import com.feadev.eaids.videolibrary.dto.request.UpdateVideoRequest;
import com.feadev.eaids.videolibrary.dto.response.VideoLibraryResponse;
import com.feadev.eaids.videolibrary.dto.response.VideoLibraryStatsResponse;
import com.feadev.eaids.videolibrary.model.enums.VideoCategory;
import com.feadev.eaids.videolibrary.service.VideoLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/video-library")
@RequiredArgsConstructor
@Tag(name = "Video Library", description = "Centralized video library with search and categorization")
public class VideoLibraryController {

    private final VideoLibraryService videoLibraryService;

    @PostMapping
    @Operation(summary = "Upload a video", description = "Creates a video library entry for the authenticated user")
    @ApiResponse(responseCode = "201", description = "Video created successfully")
    public ResponseEntity<VideoLibraryResponse> create(
            @Valid @RequestBody CreateVideoRequest request, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(videoLibraryService.create(request, userId));
    }

    @GetMapping
    @Operation(summary = "Search/browse videos", description = "Paginated search with filters by category, position, camp, athlete, video type, and text search")
    @ApiResponse(responseCode = "200", description = "Videos retrieved successfully")
    public ResponseEntity<Page<VideoLibraryResponse>> search(
            @RequestParam(required = false) VideoCategory category,
            @RequestParam(required = false) FootballPosition position,
            @RequestParam(required = false) UUID campId,
            @RequestParam(required = false) UUID athleteId,
            @RequestParam(required = false) VideoType videoType,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(videoLibraryService.search(category, position, campId, athleteId, videoType, searchTerm, page, size, sort));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get video by ID")
    public ResponseEntity<VideoLibraryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(videoLibraryService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a video")
    public ResponseEntity<VideoLibraryResponse> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateVideoRequest request, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(videoLibraryService.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a video")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        videoLibraryService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get video library statistics")
    public ResponseEntity<VideoLibraryStatsResponse> getStats() {
        return ResponseEntity.ok(videoLibraryService.getStats());
    }

    @GetMapping("/athletes/{athleteId}")
    @Operation(summary = "Get all videos for an athlete")
    public ResponseEntity<Page<VideoLibraryResponse>> getByAthlete(
            @PathVariable UUID athleteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(videoLibraryService.search(null, null, null, athleteId, null, null, page, size, "newest"));
    }

    @GetMapping("/camps/{campId}")
    @Operation(summary = "Get all videos for a camp")
    public ResponseEntity<Page<VideoLibraryResponse>> getByCamp(
            @PathVariable UUID campId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(videoLibraryService.search(null, null, campId, null, null, null, page, size, "newest"));
    }

    @PostMapping("/admin/migrate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Migrate existing athlete videos to library", description = "Admin-only idempotent migration from athlete_videos table")
    public ResponseEntity<Map<String, Object>> migrate() {
        return ResponseEntity.ok(videoLibraryService.migrateFromAthleteVideos());
    }
}
