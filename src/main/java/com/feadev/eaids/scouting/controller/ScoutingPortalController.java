package com.feadev.eaids.scouting.controller;

import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.scouting.dto.request.CreateBookmarkRequest;
import com.feadev.eaids.scouting.dto.response.AthleteSearchResult;
import com.feadev.eaids.scouting.dto.response.BookmarkResponse;
import com.feadev.eaids.scouting.service.ScoutingPortalService;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scouting")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SCOUT')")
public class ScoutingPortalController {

    private final ScoutingPortalService scoutingPortalService;
    private final UserRepository userRepository;

    @GetMapping("/athletes")
    public ResponseEntity<Page<AthleteSearchResult>> searchAthletes(
            @RequestParam(required = false) FootballPosition position,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) BigDecimal minScore,
            Pageable pageable) {
        UUID scoutUserId = getCurrentUserId();
        return ResponseEntity.ok(scoutingPortalService.searchAthletes(
                scoutUserId, position, ageGroup, country, state, minScore, pageable));
    }

    @PostMapping("/bookmarks")
    public ResponseEntity<BookmarkResponse> createBookmark(
            @Valid @RequestBody CreateBookmarkRequest request) {
        UUID scoutUserId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scoutingPortalService.createBookmark(scoutUserId, request));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<Page<BookmarkResponse>> getBookmarks(Pageable pageable) {
        UUID scoutUserId = getCurrentUserId();
        return ResponseEntity.ok(scoutingPortalService.getBookmarks(scoutUserId, pageable));
    }

    @DeleteMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable UUID bookmarkId) {
        UUID scoutUserId = getCurrentUserId();
        scoutingPortalService.deleteBookmark(bookmarkId, scoutUserId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return user.getId();
    }
}
