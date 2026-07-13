package com.feadev.eaids.reporting.controller;

import com.feadev.eaids.reporting.model.ReportFormat;
import com.feadev.eaids.reporting.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
@Tag(name = "Reports", description = "PDF and Excel report generation endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/camp-summary/{campId}")
    @Operation(summary = "Generate camp summary report", description = "Returns a downloadable PDF or Excel camp summary report")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    @ApiResponse(responseCode = "404", description = "Camp not found")
    public ResponseEntity<byte[]> campSummary(
            @PathVariable UUID campId,
            @RequestParam(defaultValue = "PDF") ReportFormat format) {
        byte[] data = reportService.generateCampSummary(campId, format);
        return buildResponse(data, format, "camp-summary");
    }

    @GetMapping("/athlete-evaluation/{athleteId}")
    @Operation(summary = "Generate athlete evaluation report")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<byte[]> athleteEvaluation(
            @PathVariable UUID athleteId,
            @RequestParam(required = false) UUID campId,
            @RequestParam(defaultValue = "PDF") ReportFormat format) {
        byte[] data = reportService.generateAthleteEvaluation(athleteId, campId, format);
        return buildResponse(data, format, "athlete-evaluation");
    }

    @GetMapping("/coach-activity/{coachId}")
    @Operation(summary = "Generate coach activity report")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<byte[]> coachActivity(
            @PathVariable UUID coachId,
            @RequestParam(defaultValue = "PDF") ReportFormat format) {
        byte[] data = reportService.generateCoachActivity(coachId, format);
        return buildResponse(data, format, "coach-activity");
    }

    @GetMapping("/selections/{campId}")
    @Operation(summary = "Generate selection report")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<byte[]> selectionReport(
            @PathVariable UUID campId,
            @RequestParam(defaultValue = "PDF") ReportFormat format) {
        byte[] data = reportService.generateSelectionReport(campId, format);
        return buildResponse(data, format, "selection-report");
    }

    @GetMapping("/rankings/{campId}")
    @Operation(summary = "Generate rankings report")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<byte[]> rankingsReport(
            @PathVariable UUID campId,
            @RequestParam(defaultValue = "OVERALL") String type,
            @RequestParam(defaultValue = "PDF") ReportFormat format) {
        byte[] data = reportService.generateRankingsReport(campId, type, format);
        return buildResponse(data, format, "rankings-report");
    }

    private ResponseEntity<byte[]> buildResponse(byte[] data, ReportFormat format, String filePrefix) {
        String contentType;
        String extension;
        if (format == ReportFormat.EXCEL) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            extension = ".xlsx";
        } else {
            contentType = "application/pdf";
            extension = ".pdf";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePrefix + extension + "\"")
                .body(data);
    }
}
