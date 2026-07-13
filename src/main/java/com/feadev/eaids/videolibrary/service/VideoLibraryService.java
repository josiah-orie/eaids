package com.feadev.eaids.videolibrary.service;

import com.feadev.eaids.registrations.model.enums.VideoType;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.videolibrary.dto.request.CreateVideoRequest;
import com.feadev.eaids.videolibrary.dto.request.UpdateVideoRequest;
import com.feadev.eaids.videolibrary.dto.response.VideoLibraryResponse;
import com.feadev.eaids.videolibrary.dto.response.VideoLibraryStatsResponse;
import com.feadev.eaids.videolibrary.model.enums.VideoCategory;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

public interface VideoLibraryService {

    VideoLibraryResponse create(CreateVideoRequest request, UUID authenticatedUserId);

    Page<VideoLibraryResponse> search(VideoCategory category, FootballPosition position, UUID campId,
                                       UUID athleteId, VideoType videoType, String searchTerm,
                                       int page, int size, String sort);

    VideoLibraryResponse getById(UUID id);

    VideoLibraryResponse update(UUID id, UpdateVideoRequest request, UUID authenticatedUserId);

    void delete(UUID id, UUID authenticatedUserId);

    VideoLibraryStatsResponse getStats();

    Map<String, Object> migrateFromAthleteVideos();
}
