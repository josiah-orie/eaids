package com.feadev.eaids.assessmenttemplate.controller;

import com.feadev.eaids.assessmenttemplate.dto.request.CreateAssessmentTemplateRequest;
import com.feadev.eaids.assessmenttemplate.dto.request.UpdateAssessmentTemplateRequest;
import com.feadev.eaids.assessmenttemplate.dto.response.AssessmentTemplateResponse;
import com.feadev.eaids.assessmenttemplate.service.AssessmentTemplateService;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/assessment-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Assessment Templates", description = "Reusable assessment templates linked to position profiles")
public class AssessmentTemplateController {

    private final AssessmentTemplateService templateService;

    @PostMapping
    @Operation(summary = "Create an assessment template", description = "Creates a new assessment template linked to a position profile")
    @ApiResponse(responseCode = "201", description = "Template created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Position profile not found")
    public ResponseEntity<AssessmentTemplateResponse> createTemplate(
            @Valid @RequestBody CreateAssessmentTemplateRequest request) {
        AssessmentTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List assessment templates", description = "Lists templates with optional filtering by position profile, position, or age category")
    @ApiResponse(responseCode = "200", description = "Templates retrieved successfully")
    public ResponseEntity<List<AssessmentTemplateResponse>> getAllTemplates(
            @RequestParam(required = false) UUID positionProfileId,
            @RequestParam(required = false) FootballPosition position,
            @RequestParam(required = false) AgeCategory ageCategory) {
        return ResponseEntity.ok(templateService.getAllTemplates(positionProfileId, position, ageCategory));
    }

    @GetMapping("/{templateId}")
    @Operation(summary = "Get assessment template by ID", description = "Retrieves an assessment template with all metrics")
    @ApiResponse(responseCode = "200", description = "Template retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Template not found")
    public ResponseEntity<AssessmentTemplateResponse> getTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(templateService.getTemplate(templateId));
    }

    @PutMapping("/{templateId}")
    @Operation(summary = "Update an assessment template", description = "Updates template details and/or metrics")
    @ApiResponse(responseCode = "200", description = "Template updated successfully")
    @ApiResponse(responseCode = "404", description = "Template not found")
    public ResponseEntity<AssessmentTemplateResponse> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateAssessmentTemplateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(templateId, request));
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "Deactivate an assessment template", description = "Soft-deletes a template by setting isActive to false")
    @ApiResponse(responseCode = "204", description = "Template deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Template not found")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
