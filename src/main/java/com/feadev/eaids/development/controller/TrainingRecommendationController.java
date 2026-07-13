package com.feadev.eaids.development.controller;

import com.feadev.eaids.development.dto.request.CreateTrainingRecommendationRequest;
import com.feadev.eaids.development.dto.response.TrainingRecommendationResponse;
import com.feadev.eaids.development.model.TrainingRecommendation;
import com.feadev.eaids.development.repository.TrainingRecommendationRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/training-recommendations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Training Recommendations", description = "Admin CRUD for the training recommendation bank")
public class TrainingRecommendationController {

    private final TrainingRecommendationRepository repository;

    @PostMapping
    @Operation(summary = "Create a training recommendation")
    public ResponseEntity<TrainingRecommendationResponse> create(@Valid @RequestBody CreateTrainingRecommendationRequest request) {
        TrainingRecommendation rec = TrainingRecommendation.builder()
                .position(request.getPosition()).category(request.getCategory())
                .title(request.getTitle()).description(request.getDescription())
                .difficultyLevel(request.getDifficultyLevel()).isActive(true).build();
        TrainingRecommendation saved = repository.save(rec);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    @GetMapping
    @Operation(summary = "List training recommendations", description = "Filter by position and/or category")
    public ResponseEntity<List<TrainingRecommendationResponse>> getAll(
            @RequestParam(required = false) FootballPosition position,
            @RequestParam(required = false) String category) {
        List<TrainingRecommendation> results;
        if (position != null && category != null) {
            results = repository.findByPositionAndCategoryAndIsActiveTrue(position, category);
        } else if (position != null) {
            results = repository.findByPositionAndIsActiveTrue(position);
        } else if (category != null) {
            results = repository.findByCategoryAndIsActiveTrue(category);
        } else {
            results = repository.findByIsActiveTrue();
        }
        return ResponseEntity.ok(results.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a training recommendation")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        TrainingRecommendation rec = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found: " + id));
        rec.setActive(false);
        repository.save(rec);
        return ResponseEntity.noContent().build();
    }

    private TrainingRecommendationResponse mapToResponse(TrainingRecommendation r) {
        return TrainingRecommendationResponse.builder()
                .id(r.getId()).position(r.getPosition().name())
                .category(r.getCategory()).title(r.getTitle())
                .description(r.getDescription()).difficultyLevel(r.getDifficultyLevel())
                .active(r.isActive()).build();
    }
}
