package com.feadev.eaids.scouting.controller;

import com.feadev.eaids.scouting.dto.request.GrantScopeRequest;
import com.feadev.eaids.scouting.dto.response.ScoutProfileResponse;
import com.feadev.eaids.scouting.dto.response.ScopeResponse;
import com.feadev.eaids.scouting.service.ScoutProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/scouts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminScoutController {

    private final ScoutProfileService scoutProfileService;

    @GetMapping("/{scoutUserId}")
    public ResponseEntity<ScoutProfileResponse> getScoutProfile(@PathVariable UUID scoutUserId) {
        return ResponseEntity.ok(scoutProfileService.getProfile(scoutUserId));
    }

    @PostMapping("/{scoutUserId}/scopes")
    public ResponseEntity<ScopeResponse> grantScope(
            @PathVariable UUID scoutUserId,
            @Valid @RequestBody GrantScopeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scoutProfileService.grantScope(scoutUserId, request));
    }

    @GetMapping("/{scoutUserId}/scopes")
    public ResponseEntity<List<ScopeResponse>> getScopes(@PathVariable UUID scoutUserId) {
        return ResponseEntity.ok(scoutProfileService.getScopes(scoutUserId));
    }

    @DeleteMapping("/scopes/{scopeId}")
    public ResponseEntity<Void> revokeScope(@PathVariable UUID scopeId) {
        scoutProfileService.revokeScope(scopeId);
        return ResponseEntity.noContent().build();
    }
}
