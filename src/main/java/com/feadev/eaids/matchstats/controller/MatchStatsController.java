package com.feadev.eaids.matchstats.controller;

import com.feadev.eaids.matchstats.dto.request.RecordMatchStatsRequest;
import com.feadev.eaids.matchstats.dto.response.MatchStatDefinitionResponse;
import com.feadev.eaids.matchstats.dto.response.MatchStatEntryResponse;
import com.feadev.eaids.matchstats.dto.response.MatchStatsSummaryResponse;
import com.feadev.eaids.matchstats.model.enums.PositionGroup;
import com.feadev.eaids.matchstats.service.MatchStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/match-stats")
@RequiredArgsConstructor
@Tag(name = "Match Statistics", description = "Game-day match statistics recording and querying")
public class MatchStatsController {

    private final MatchStatsService matchStatsService;

    @GetMapping("/definitions")
    @Operation(summary = "List stat definitions", description = "Returns all active match stat definitions, optionally filtered by position group")
    @ApiResponse(responseCode = "200", description = "Definitions retrieved successfully")
    public ResponseEntity<List<MatchStatDefinitionResponse>> getDefinitions(
            @PathVariable UUID campId,
            @RequestParam(required = false) PositionGroup positionGroup) {
        return ResponseEntity.ok(matchStatsService.getDefinitions(positionGroup));
    }

    @PostMapping
    @Operation(summary = "Record match stats", description = "Records a batch of match statistics for an enrollment")
    @ApiResponse(responseCode = "201", description = "Stats recorded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Enrollment or definition not found")
    public ResponseEntity<List<MatchStatEntryResponse>> recordStats(
            @PathVariable UUID campId,
            @Valid @RequestBody RecordMatchStatsRequest request,
            Authentication authentication) {
        UUID recordedBy = UUID.fromString(authentication.getName());
        List<MatchStatEntryResponse> responses = matchStatsService.recordStats(campId, request, recordedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/enrollments/{enrollmentId}")
    @Operation(summary = "Get stats by enrollment", description = "Returns match stats for a specific enrollment, optionally filtered by match date")
    @ApiResponse(responseCode = "200", description = "Stats retrieved successfully")
    public ResponseEntity<List<MatchStatEntryResponse>> getEnrollmentStats(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId,
            @RequestParam(required = false) LocalDate matchDate) {
        return ResponseEntity.ok(matchStatsService.getEnrollmentStats(campId, enrollmentId, matchDate));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get camp stats summary", description = "Returns aggregated match stats (totals and averages) for all athletes in the camp")
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    public ResponseEntity<MatchStatsSummaryResponse> getCampSummary(@PathVariable UUID campId) {
        return ResponseEntity.ok(matchStatsService.getCampSummary(campId));
    }
}
