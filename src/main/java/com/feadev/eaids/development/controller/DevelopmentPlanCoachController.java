package com.feadev.eaids.development.controller;

import com.feadev.eaids.development.dto.request.*;
import com.feadev.eaids.development.dto.response.DevelopmentPlanResponse;
import com.feadev.eaids.development.dto.response.ProgressResponse;
import com.feadev.eaids.development.service.DevelopmentPlanCoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/development-plans/{planId}")
@RequiredArgsConstructor
@Tag(name = "Development Plans", description = "Coach override and progress tracking for development plans")
public class DevelopmentPlanCoachController {

    private final DevelopmentPlanCoachService coachService;

    @PutMapping
    @Operation(summary = "Update development plan", description = "Coach updates plan details, assigns themselves, activates plan")
    public ResponseEntity<DevelopmentPlanResponse> updatePlan(
            @PathVariable UUID planId, @Valid @RequestBody UpdatePlanRequest request) {
        return ResponseEntity.ok(coachService.updatePlan(planId, request));
    }

    @PostMapping("/strengths")
    @Operation(summary = "Add a strength manually")
    public ResponseEntity<DevelopmentPlanResponse> addStrength(
            @PathVariable UUID planId, @Valid @RequestBody AddStrengthRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coachService.addStrength(planId, request));
    }

    @DeleteMapping("/strengths/{strengthId}")
    @Operation(summary = "Remove a strength")
    public ResponseEntity<Void> removeStrength(@PathVariable UUID planId, @PathVariable UUID strengthId) {
        coachService.removeStrength(planId, strengthId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/weaknesses")
    @Operation(summary = "Add a weakness manually")
    public ResponseEntity<DevelopmentPlanResponse> addWeakness(
            @PathVariable UUID planId, @Valid @RequestBody AddWeaknessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coachService.addWeakness(planId, request));
    }

    @DeleteMapping("/weaknesses/{weaknessId}")
    @Operation(summary = "Remove a weakness")
    public ResponseEntity<Void> removeWeakness(@PathVariable UUID planId, @PathVariable UUID weaknessId) {
        coachService.removeWeakness(planId, weaknessId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recommendations")
    @Operation(summary = "Assign a training recommendation to the plan")
    public ResponseEntity<DevelopmentPlanResponse> assignRecommendation(
            @PathVariable UUID planId, @Valid @RequestBody AssignRecommendationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coachService.assignRecommendation(planId, request));
    }

    @DeleteMapping("/recommendations/{planRecommendationId}")
    @Operation(summary = "Remove a recommendation from the plan")
    public ResponseEntity<Void> removeRecommendation(@PathVariable UUID planId, @PathVariable UUID planRecommendationId) {
        coachService.removeRecommendation(planId, planRecommendationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/goals")
    @Operation(summary = "Add a goal to the plan")
    public ResponseEntity<DevelopmentPlanResponse> addGoal(
            @PathVariable UUID planId, @Valid @RequestBody AddGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coachService.addGoal(planId, request));
    }

    @PutMapping("/goals/{goalId}")
    @Operation(summary = "Update a goal")
    public ResponseEntity<DevelopmentPlanResponse> updateGoal(
            @PathVariable UUID planId, @PathVariable UUID goalId, @Valid @RequestBody UpdateGoalRequest request) {
        return ResponseEntity.ok(coachService.updateGoal(planId, goalId, request));
    }

    @PostMapping("/progress")
    @Operation(summary = "Record a progress entry")
    public ResponseEntity<ProgressResponse> recordProgress(
            @PathVariable UUID planId, @Valid @RequestBody RecordProgressRequest request, Authentication authentication) {
        UUID recordedBy = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(coachService.recordProgress(planId, request, recordedBy));
    }

    @GetMapping("/progress")
    @Operation(summary = "List progress entries for the plan")
    public ResponseEntity<List<ProgressResponse>> getProgress(@PathVariable UUID planId) {
        return ResponseEntity.ok(coachService.getProgress(planId));
    }
}
