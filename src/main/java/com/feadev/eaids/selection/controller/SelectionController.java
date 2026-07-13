package com.feadev.eaids.selection.controller;

import com.feadev.eaids.selection.dto.request.ApproveSelectionRequest;
import com.feadev.eaids.selection.dto.response.SelectionResponse;
import com.feadev.eaids.selection.model.enums.ApprovalStatus;
import com.feadev.eaids.selection.service.SelectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/selections")
@RequiredArgsConstructor
public class SelectionController {

    private final SelectionService selectionService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SelectionResponse>> generateSelections(@PathVariable UUID campId) {
        List<SelectionResponse> responses = selectionService.generateSelections(campId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<List<SelectionResponse>> getSelections(
            @PathVariable UUID campId,
            @RequestParam(required = false) ApprovalStatus status) {
        return ResponseEntity.ok(selectionService.getSelections(campId, status));
    }

    @PostMapping("/{selectionId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SelectionResponse> approveSelection(
            @PathVariable UUID campId,
            @PathVariable UUID selectionId,
            @Valid @RequestBody ApproveSelectionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID reviewerId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(selectionService.approveSelection(selectionId, request, reviewerId));
    }

    @PostMapping("/{selectionId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SelectionResponse> rejectSelection(
            @PathVariable UUID campId,
            @PathVariable UUID selectionId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID reviewerId = UUID.fromString(userDetails.getUsername());
        String comments = body.get("comments");
        return ResponseEntity.ok(selectionService.rejectSelection(selectionId, comments, reviewerId));
    }
}
