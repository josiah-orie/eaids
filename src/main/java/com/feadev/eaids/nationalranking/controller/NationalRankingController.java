package com.feadev.eaids.nationalranking.controller;

import com.feadev.eaids.nationalranking.dto.request.CreateRankingConfigRequest;
import com.feadev.eaids.nationalranking.dto.response.NationalRankingResponse;
import com.feadev.eaids.nationalranking.dto.response.RankingConfigResponse;
import com.feadev.eaids.nationalranking.model.enums.RankingScope;
import com.feadev.eaids.nationalranking.service.NationalRankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NationalRankingController {

    private final NationalRankingService nationalRankingService;

    // Admin config management
    @PostMapping("/api/v1/admin/national-rankings/configs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RankingConfigResponse> createConfig(
            @Valid @RequestBody CreateRankingConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nationalRankingService.createConfig(request));
    }

    @GetMapping("/api/v1/admin/national-rankings/configs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RankingConfigResponse>> getAllConfigs() {
        return ResponseEntity.ok(nationalRankingService.getAllConfigs());
    }

    // Generate rankings
    @PostMapping("/api/v1/admin/national-rankings/generate/{configId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateRankings(@PathVariable UUID configId) {
        int count = nationalRankingService.generateRankings(configId);
        return ResponseEntity.ok(Map.of(
                "message", "National rankings generated successfully",
                "totalRankings", count,
                "configId", configId.toString()
        ));
    }

    // Public query endpoints
    @GetMapping("/api/v1/national-rankings/top")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NationalRankingResponse>> getTopRankings(
            @RequestParam RankingScope scope,
            @RequestParam(required = false) String scopeValue,
            Pageable pageable) {
        return ResponseEntity.ok(nationalRankingService.getTopRankings(scope, scopeValue, pageable));
    }

    @GetMapping("/api/v1/national-rankings/athlete/{athleteUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NationalRankingResponse>> getAthleteRankings(
            @PathVariable UUID athleteUserId) {
        return ResponseEntity.ok(nationalRankingService.getAthleteRankings(athleteUserId));
    }
}
