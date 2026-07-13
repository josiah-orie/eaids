package com.feadev.eaids.scouting.service.impl;

import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.scouting.dto.response.AthleteReportResponse;
import com.feadev.eaids.scouting.service.ReportGenerationService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;

    @Override
    public AthleteReportResponse generateJsonReport(UUID scoutUserId, UUID athleteUserId) {
        // Build report data (simplified - full implementation would pull from all modules)
        AthleteReportResponse report = AthleteReportResponse.builder()
                .athleteUserId(athleteUserId)
                .generatedAt(LocalDateTime.now())
                .videoUrls(List.of())
                .build();

        logReportDownload(scoutUserId, athleteUserId);
        return report;
    }

    @Override
    public byte[] generatePdfReport(UUID scoutUserId, UUID athleteUserId) {
        AthleteReportResponse reportData = generateJsonReport(scoutUserId, athleteUserId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle("Athlete Report");
        document.addAuthor("EAIDS Platform");

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        document.add(new Paragraph("EAIDS Athlete Scouting Report", titleFont));
        document.add(new Paragraph(" "));

        // Content
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("Athlete ID: " + athleteUserId, bodyFont));
        document.add(new Paragraph("Generated: " + reportData.getGeneratedAt(), bodyFont));
        document.add(new Paragraph(" "));

        if (reportData.getFullName() != null) {
            document.add(new Paragraph("Name: " + reportData.getFullName(), bodyFont));
        }
        if (reportData.getPosition() != null) {
            document.add(new Paragraph("Position: " + reportData.getPosition(), bodyFont));
        }
        if (reportData.getCountry() != null) {
            document.add(new Paragraph("Country: " + reportData.getCountry(), bodyFont));
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Performance Scores", headingFont));
        if (reportData.getLatestPhysicalScore() != null) {
            document.add(new Paragraph("Physical: " + reportData.getLatestPhysicalScore(), bodyFont));
        }
        if (reportData.getLatestTechnicalScore() != null) {
            document.add(new Paragraph("Technical: " + reportData.getLatestTechnicalScore(), bodyFont));
        }
        if (reportData.getLatestTacticalScore() != null) {
            document.add(new Paragraph("Tactical: " + reportData.getLatestTacticalScore(), bodyFont));
        }
        if (reportData.getLatestTotalWeightedScore() != null) {
            document.add(new Paragraph("Total Weighted: " + reportData.getLatestTotalWeightedScore(), bodyFont));
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("National Ranking", headingFont));
        if (reportData.getNationalRank() != null) {
            document.add(new Paragraph("Rank: #" + reportData.getNationalRank(), bodyFont));
        }
        if (reportData.getCompositeScore() != null) {
            document.add(new Paragraph("Composite Score: " + reportData.getCompositeScore(), bodyFont));
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Potential Assessment", headingFont));
        if (reportData.getPotentialTier() != null) {
            document.add(new Paragraph("Tier: " + reportData.getPotentialTier(), bodyFont));
        }
        if (reportData.getProjectedScore1yr() != null) {
            document.add(new Paragraph("Projected (1yr): " + reportData.getProjectedScore1yr(), bodyFont));
        }

        document.close();

        log.info("PDF report generated for athlete: {}", athleteUserId);
        return baos.toByteArray();
    }

    private void logReportDownload(UUID scoutUserId, UUID athleteUserId) {
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        auditService.log(AuditAction.REPORT_DOWNLOADED, scoutUserId, actor,
                auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());
        log.info("Report downloaded: scoutUserId={}, athleteUserId={}", scoutUserId, athleteUserId);
    }
}
