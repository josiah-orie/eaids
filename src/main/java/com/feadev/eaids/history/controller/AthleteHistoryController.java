package com.feadev.eaids.history.controller;

import com.feadev.eaids.history.dto.response.AthleteHistoryResponse;
import com.feadev.eaids.history.service.AthleteHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Athlete History", description = "Unified athlete timeline aggregation across all modules")
public class AthleteHistoryController {

    private final AthleteHistoryService historyService;

    @GetMapping("/api/v1/athletes/{athleteId}/history")
    @Operation(summary = "Get athlete history", description = "Retrieves unified timeline for an athlete (coaches/admins)")
    @ApiResponse(responseCode = "200", description = "History retrieved successfully")
    public ResponseEntity<AthleteHistoryResponse> getAthleteHistory(
            @PathVariable UUID athleteId,
            @RequestParam(required = false) UUID campId,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(historyService.getHistory(athleteId, campId, type, fromDate, toDate, page, size));
    }

    @GetMapping("/api/v1/athletes/me/history")
    @Operation(summary = "Get own athlete history", description = "Retrieves unified timeline for the authenticated athlete")
    @ApiResponse(responseCode = "200", description = "History retrieved successfully")
    public ResponseEntity<AthleteHistoryResponse> getOwnHistory(
            Authentication authentication,
            @RequestParam(required = false) UUID campId,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID athleteId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(historyService.getHistory(athleteId, campId, type, fromDate, toDate, page, size));
    }
}
