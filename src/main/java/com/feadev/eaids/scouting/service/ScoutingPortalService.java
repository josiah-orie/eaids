package com.feadev.eaids.scouting.service;

import com.feadev.eaids.scouting.dto.request.CreateBookmarkRequest;
import com.feadev.eaids.scouting.dto.response.AthleteSearchResult;
import com.feadev.eaids.scouting.dto.response.BookmarkResponse;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface ScoutingPortalService {

    Page<AthleteSearchResult> searchAthletes(UUID scoutUserId, FootballPosition position,
                                              String ageGroup, String country, String state,
                                              BigDecimal minScore, Pageable pageable);

    BookmarkResponse createBookmark(UUID scoutUserId, CreateBookmarkRequest request);

    Page<BookmarkResponse> getBookmarks(UUID scoutUserId, Pageable pageable);

    void deleteBookmark(UUID bookmarkId, UUID scoutUserId);
}
