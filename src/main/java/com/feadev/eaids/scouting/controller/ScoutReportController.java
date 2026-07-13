package com.feadev.eaids.scouting.controller;

import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.scouting.dto.response.AthleteReportResponse;
import com.feadev.eaids.scouting.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scouting/athletes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SCOUT')")
public class ScoutReportController {

    private final ReportGenerationService reportGenerationService;
    private final UserRepository userRepository;

    @GetMapping("/{athleteUserId}/report")
    public ResponseEntity<?> getReport(
            @PathVariable UUID athleteUserId,
            @RequestParam(defaultValue = "json") String format) {
        UUID scoutUserId = getCurrentUserId();

        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfBytes = reportGenerationService.generatePdfReport(scoutUserId, athleteUserId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=athlete-report-" + athleteUserId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        }

        AthleteReportResponse report = reportGenerationService.generateJsonReport(scoutUserId, athleteUserId);
        return ResponseEntity.ok(report);
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return user.getId();
    }
}
