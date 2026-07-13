package com.feadev.eaids.scouting.service.impl;

import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.scouting.dto.request.CreateBookmarkRequest;
import com.feadev.eaids.scouting.dto.response.AthleteSearchResult;
import com.feadev.eaids.scouting.dto.response.BookmarkResponse;
import com.feadev.eaids.scouting.model.ScoutBookmark;
import com.feadev.eaids.scouting.repository.ScoutBookmarkRepository;
import com.feadev.eaids.scouting.security.ScoutAccessEvaluator;
import com.feadev.eaids.scouting.service.ScoutingPortalService;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoutingPortalServiceImpl implements ScoutingPortalService {

    private final ScoutBookmarkRepository bookmarkRepository;
    private final ScoutAccessEvaluator accessEvaluator;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AthleteSearchResult> searchAthletes(UUID scoutUserId, FootballPosition position,
                                                     String ageGroup, String country, String state,
                                                     BigDecimal minScore, Pageable pageable) {
        // This is a simplified search implementation
        // In production, this would use JPA Specifications or a dedicated search query
        // For now, return an empty page — the full implementation requires joining
        // athlete_profiles + athlete_scores + national_rankings with scope filtering
        log.info("Scout {} searching athletes: position={}, ageGroup={}, country={}, state={}",
                scoutUserId, position, ageGroup, country, state);

        // Placeholder - real implementation uses Specification pattern (Task 12)
        return new PageImpl<>(List.of(), pageable, 0);
    }

    @Override
    @Transactional
    public BookmarkResponse createBookmark(UUID scoutUserId, CreateBookmarkRequest request) {
        if (bookmarkRepository.existsByScoutUserIdAndAthleteUserId(scoutUserId, request.getAthleteUserId())) {
            throw new IllegalArgumentException("Bookmark already exists for this athlete");
        }

        ScoutBookmark bookmark = ScoutBookmark.builder()
                .scoutUserId(scoutUserId)
                .athleteUserId(request.getAthleteUserId())
                .notes(request.getNotes())
                .build();

        ScoutBookmark saved = bookmarkRepository.save(bookmark);
        return mapBookmarkToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookmarkResponse> getBookmarks(UUID scoutUserId, Pageable pageable) {
        return bookmarkRepository.findByScoutUserIdOrderByCreatedDateDesc(scoutUserId, pageable)
                .map(this::mapBookmarkToResponse);
    }

    @Override
    @Transactional
    public void deleteBookmark(UUID bookmarkId, UUID scoutUserId) {
        ScoutBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found: " + bookmarkId));

        if (!bookmark.getScoutUserId().equals(scoutUserId)) {
            throw new IllegalArgumentException("You can only delete your own bookmarks");
        }

        bookmarkRepository.deleteById(bookmarkId);
    }

    private BookmarkResponse mapBookmarkToResponse(ScoutBookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .scoutUserId(bookmark.getScoutUserId())
                .athleteUserId(bookmark.getAthleteUserId())
                .notes(bookmark.getNotes())
                .createdDate(bookmark.getCreatedDate())
                .build();
    }
}
