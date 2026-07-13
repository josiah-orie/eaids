package com.feadev.eaids.ranking.controller;

import com.feadev.eaids.ranking.dto.response.RankingResponse;
import com.feadev.eaids.ranking.model.enums.RankingType;
import com.feadev.eaids.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RankingResponse>> generateRankings(@PathVariable UUID campId) {
        List<RankingResponse> responses = rankingService.generateRankings(campId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<List<RankingResponse>> getRankings(
            @PathVariable UUID campId,
            @RequestParam(defaultValue = "OVERALL") RankingType type,
            @RequestParam(required = false) String filterValue) {
        List<RankingResponse> responses;
        if (filterValue != null && !filterValue.isBlank()) {
            responses = rankingService.getRankingsByTypeAndFilter(campId, type, filterValue);
        } else {
            responses = rankingService.getRankingsByType(campId, type);
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<List<RankingResponse>> getRankingsByEnrollment(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(rankingService.getRankingsByEnrollment(enrollmentId));
    }
}
