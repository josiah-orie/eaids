package com.feadev.eaids.development.controller;

import com.feadev.eaids.development.dto.request.GeneratePlanRequest;
import com.feadev.eaids.development.dto.response.DevelopmentPlanResponse;
import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;
import com.feadev.eaids.development.service.DevelopmentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/development-plans")
@RequiredArgsConstructor
@Tag(name = "Development Plans", description = "Development plan generation, management, and progress tracking")
public class DevelopmentPlanController {

    private final DevelopmentPlanService developmentPlanService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a development plan", description = "Auto-generates a development plan by analyzing an athlete's scores in a camp")
    @ApiResponse(responseCode = "201", description = "Plan generated successfully")
    @ApiResponse(responseCode = "404", description = "Enrollment or score not found")
    public ResponseEntity<DevelopmentPlanResponse> generatePlan(@Valid @RequestBody GeneratePlanRequest request) {
        DevelopmentPlanResponse response = developmentPlanService.generatePlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List development plans", description = "Lists development plans with optional filtering by athlete and status")
    @ApiResponse(responseCode = "200", description = "Plans retrieved successfully")
    public ResponseEntity<List<DevelopmentPlanResponse>> getPlans(
            @RequestParam(required = false) UUID athleteId,
            @RequestParam(required = false) DevelopmentPlanStatus status) {
        return ResponseEntity.ok(developmentPlanService.getPlans(athleteId, status));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get development plan by ID", description = "Retrieves a development plan with all nested data")
    @ApiResponse(responseCode = "200", description = "Plan retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Plan not found")
    public ResponseEntity<DevelopmentPlanResponse> getPlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(developmentPlanService.getPlan(planId));
    }
}
