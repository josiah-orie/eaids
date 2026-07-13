package com.feadev.eaids.reporting.service.impl;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.evaluation.model.CampEvaluator;
import com.feadev.eaids.evaluation.model.CoachEvaluation;
import com.feadev.eaids.evaluation.repository.CampEvaluatorRepository;
import com.feadev.eaids.evaluation.repository.CoachEvaluationRepository;
import com.feadev.eaids.ranking.model.AthleteRanking;
import com.feadev.eaids.ranking.repository.AthleteRankingRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.reporting.model.ReportFormat;
import com.feadev.eaids.reporting.service.ReportService;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import com.feadev.eaids.selection.model.SelectionDecision;
import com.feadev.eaids.selection.repository.SelectionDecisionRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final CampRepository campRepository;
    private final CampEnrollmentRepository enrollmentRepository;
    private final AthleteScoreRepository scoreRepository;
    private final SelectionDecisionRepository selectionRepository;
    private final AthleteRankingRepository rankingRepository;
    private final CoachEvaluationRepository evaluationRepository;
    private final CampEvaluatorRepository evaluatorRepository;
    private final AthleteProfileRepository athleteProfileRepository;

    @Override
    public byte[] generateCampSummary(UUID campId, ReportFormat format) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));

        List<AthleteScore> scores = scoreRepository.findByCampIdOrderByTotalWeightedScoreDesc(campId);

        if (format == ReportFormat.EXCEL) {
            return generateCampSummaryExcel(camp, scores);
        }
        return generateCampSummaryPdf(camp, scores);
    }

    @Override
    public byte[] generateAthleteEvaluation(UUID athleteId, UUID campId, ReportFormat format) {
        List<CampEnrollment> enrollments;
        if (campId != null) {
            enrollments = enrollmentRepository.findByCampIdAndUserId(campId, athleteId)
                    .map(List::of).orElse(List.of());
        } else {
            enrollments = enrollmentRepository.findByUserId(athleteId);
        }

        if (format == ReportFormat.EXCEL) {
            return generateAthleteEvaluationExcel(athleteId, enrollments);
        }
        return generateAthleteEvaluationPdf(athleteId, enrollments);
    }

    @Override
    public byte[] generateCoachActivity(UUID coachId, ReportFormat format) {
        List<CampEvaluator> evaluators = evaluatorRepository.findByUserId(coachId);
        if (format == ReportFormat.EXCEL) {
            return generateCoachActivityExcel(coachId, evaluators);
        }
        return generateCoachActivityPdf(coachId, evaluators);
    }

    @Override
    public byte[] generateSelectionReport(UUID campId, ReportFormat format) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));
        List<SelectionDecision> selections = selectionRepository.findByCampId(campId);

        if (format == ReportFormat.EXCEL) {
            return generateSelectionExcel(camp, selections);
        }
        return generateSelectionPdf(camp, selections);
    }

    @Override
    public byte[] generateRankingsReport(UUID campId, String rankingType, ReportFormat format) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));

        List<AthleteRanking> rankings = rankingRepository.findByCampId(campId);

        if (format == ReportFormat.EXCEL) {
            return generateRankingsExcel(camp, rankings, rankingType);
        }
        return generateRankingsPdf(camp, rankings, rankingType);
    }

    // --- Camp Summary PDF ---
    private byte[] generateCampSummaryPdf(Camp camp, List<AthleteScore> scores) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);

        doc.add(new Paragraph("Camp Summary Report", titleFont));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Camp: " + camp.getName()));
        doc.add(new Paragraph("Type: " + camp.getCampType().name()));
        doc.add(new Paragraph("Dates: " + camp.getStartDate() + " to " + camp.getEndDate()));
        doc.add(new Paragraph("Location: " + camp.getLocation() + ", " + camp.getCountry()));
        doc.add(new Paragraph("Status: " + camp.getStatus().name()));
        doc.add(new Paragraph("Capacity: " + camp.getCapacity() + " | Enrolled: " + camp.getCurrentEnrollment()));
        doc.add(new Paragraph(" "));

        if (!scores.isEmpty()) {
            doc.add(new Paragraph("Top Performers", headerFont));
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addPdfTableHeader(table, "Rank", "Athlete", "Score");

            int rank = 1;
            int limit = Math.min(10, scores.size());
            for (int i = 0; i < limit; i++) {
                AthleteScore s = scores.get(i);
                String name = getAthleteName(s.getEnrollment().getUser().getId());
                table.addCell(String.valueOf(rank++));
                table.addCell(name);
                table.addCell(s.getTotalWeightedScore() != null ? s.getTotalWeightedScore().toPlainString() : "N/A");
            }
            doc.add(table);
        } else {
            doc.add(new Paragraph("No scores available for this camp."));
        }

        doc.close();
        return out.toByteArray();
    }

    // --- Camp Summary Excel ---
    private byte[] generateCampSummaryExcel(Camp camp, List<AthleteScore> scores) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Camp Summary");
            int rowIdx = 0;

            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Camp Summary: " + camp.getName());
            rowIdx++;

            Row details = sheet.createRow(rowIdx++);
            details.createCell(0).setCellValue("Type"); details.createCell(1).setCellValue(camp.getCampType().name());
            Row dates = sheet.createRow(rowIdx++);
            dates.createCell(0).setCellValue("Dates"); dates.createCell(1).setCellValue(camp.getStartDate() + " to " + camp.getEndDate());
            Row loc = sheet.createRow(rowIdx++);
            loc.createCell(0).setCellValue("Location"); loc.createCell(1).setCellValue(camp.getLocation());
            rowIdx++;

            Row scoreHeader = sheet.createRow(rowIdx++);
            scoreHeader.createCell(0).setCellValue("Rank");
            scoreHeader.createCell(1).setCellValue("Athlete");
            scoreHeader.createCell(2).setCellValue("Total Score");

            int rank = 1;
            for (AthleteScore s : scores) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rank++);
                row.createCell(1).setCellValue(getAthleteName(s.getEnrollment().getUser().getId()));
                row.createCell(2).setCellValue(s.getTotalWeightedScore() != null ? s.getTotalWeightedScore().doubleValue() : 0);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // --- Athlete Evaluation PDF ---
    private byte[] generateAthleteEvaluationPdf(UUID athleteId, List<CampEnrollment> enrollments) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        String name = getAthleteName(athleteId);

        doc.add(new Paragraph("Athlete Evaluation Report", titleFont));
        doc.add(new Paragraph("Athlete: " + name));
        doc.add(new Paragraph(" "));

        for (CampEnrollment enrollment : enrollments) {
            doc.add(new Paragraph("Camp: " + enrollment.getCamp().getName(), new Font(Font.HELVETICA, 14, Font.BOLD)));
            scoreRepository.findByEnrollmentId(enrollment.getId()).ifPresent(score -> {
                doc.add(new Paragraph("Total Score: " + score.getTotalWeightedScore()));
                doc.add(new Paragraph("Physical: " + score.getPhysicalScore() +
                        " | Technical: " + score.getTechnicalScore() +
                        " | Tactical: " + score.getTacticalScore()));
            });
            doc.add(new Paragraph(" "));
        }

        if (enrollments.isEmpty()) {
            doc.add(new Paragraph("No camp history found."));
        }

        doc.close();
        return out.toByteArray();
    }

    private byte[] generateAthleteEvaluationExcel(UUID athleteId, List<CampEnrollment> enrollments) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Athlete Evaluation");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Athlete: " + getAthleteName(athleteId));
            rowIdx++;
            Row colHeader = sheet.createRow(rowIdx++);
            colHeader.createCell(0).setCellValue("Camp");
            colHeader.createCell(1).setCellValue("Total Score");
            colHeader.createCell(2).setCellValue("Physical");
            colHeader.createCell(3).setCellValue("Technical");
            colHeader.createCell(4).setCellValue("Tactical");

            for (CampEnrollment enrollment : enrollments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(enrollment.getCamp().getName());
                scoreRepository.findByEnrollmentId(enrollment.getId()).ifPresent(score -> {
                    row.createCell(1).setCellValue(score.getTotalWeightedScore() != null ? score.getTotalWeightedScore().doubleValue() : 0);
                    row.createCell(2).setCellValue(score.getPhysicalScore() != null ? score.getPhysicalScore().doubleValue() : 0);
                    row.createCell(3).setCellValue(score.getTechnicalScore() != null ? score.getTechnicalScore().doubleValue() : 0);
                    row.createCell(4).setCellValue(score.getTacticalScore() != null ? score.getTacticalScore().doubleValue() : 0);
                });
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // --- Coach Activity ---
    private byte[] generateCoachActivityPdf(UUID coachId, List<CampEvaluator> evaluators) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();
        doc.add(new Paragraph("Coach Activity Report", new Font(Font.HELVETICA, 18, Font.BOLD)));
        doc.add(new Paragraph("Camps participated: " + evaluators.size()));
        doc.add(new Paragraph(" "));

        for (CampEvaluator ev : evaluators) {
            doc.add(new Paragraph("Camp: " + ev.getCamp().getName() + " | Lead: " + ev.isLead()));
        }
        doc.close();
        return out.toByteArray();
    }

    private byte[] generateCoachActivityExcel(UUID coachId, List<CampEvaluator> evaluators) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Coach Activity");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Camp"); header.createCell(1).setCellValue("Lead"); header.createCell(2).setCellValue("Start Date");
            for (CampEvaluator ev : evaluators) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ev.getCamp().getName());
                row.createCell(1).setCellValue(ev.isLead() ? "Yes" : "No");
                row.createCell(2).setCellValue(ev.getCamp().getStartDate().toString());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // --- Selection Report ---
    private byte[] generateSelectionPdf(Camp camp, List<SelectionDecision> selections) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();
        doc.add(new Paragraph("Selection Report: " + camp.getName(), new Font(Font.HELVETICA, 18, Font.BOLD)));
        doc.add(new Paragraph("Total evaluated: " + selections.size()));
        doc.add(new Paragraph(" "));

        Map<String, List<SelectionDecision>> byTier = selections.stream()
                .filter(s -> s.getRecommendedTier() != null)
                .collect(Collectors.groupingBy(s -> s.getRecommendedTier().name()));

        for (Map.Entry<String, List<SelectionDecision>> entry : byTier.entrySet()) {
            doc.add(new Paragraph(entry.getKey() + " (" + entry.getValue().size() + " athletes)", new Font(Font.HELVETICA, 12, Font.BOLD)));
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addPdfTableHeader(table, "Athlete", "Approval Status", "Final Tier");
            for (SelectionDecision sel : entry.getValue()) {
                String name = getAthleteName(sel.getEnrollment().getUser().getId());
                table.addCell(name);
                table.addCell(sel.getApprovalStatus().name());
                table.addCell(sel.getFinalTier() != null ? sel.getFinalTier().name() : "Pending");
            }
            doc.add(table);
            doc.add(new Paragraph(" "));
        }
        doc.close();
        return out.toByteArray();
    }

    private byte[] generateSelectionExcel(Camp camp, List<SelectionDecision> selections) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Selections");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Athlete");
            header.createCell(1).setCellValue("Recommended Tier");
            header.createCell(2).setCellValue("Final Tier");
            header.createCell(3).setCellValue("Approval Status");
            for (SelectionDecision sel : selections) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(getAthleteName(sel.getEnrollment().getUser().getId()));
                row.createCell(1).setCellValue(sel.getRecommendedTier().name());
                row.createCell(2).setCellValue(sel.getFinalTier() != null ? sel.getFinalTier().name() : "Pending");
                row.createCell(3).setCellValue(sel.getApprovalStatus().name());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // --- Rankings Report ---
    private byte[] generateRankingsPdf(Camp camp, List<AthleteRanking> rankings, String rankingType) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();
        doc.add(new Paragraph("Rankings Report: " + camp.getName(), new Font(Font.HELVETICA, 18, Font.BOLD)));
        doc.add(new Paragraph("Type: " + rankingType));
        doc.add(new Paragraph(" "));

        List<AthleteRanking> filtered = rankings.stream()
                .filter(r -> r.getRankingType().name().equalsIgnoreCase(rankingType))
                .sorted(Comparator.comparingInt(AthleteRanking::getRankValue))
                .collect(Collectors.toList());

        if (!filtered.isEmpty()) {
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addPdfTableHeader(table, "Rank", "Athlete", "Score", "Percentile");
            for (AthleteRanking r : filtered) {
                String name = getAthleteName(r.getEnrollment().getUser().getId());
                table.addCell(String.valueOf(r.getRankValue()));
                table.addCell(name);
                table.addCell(r.getScore() != null ? r.getScore().toPlainString() : "N/A");
                table.addCell(r.getPercentile() != null ? r.getPercentile().toPlainString() + "%" : "N/A");
            }
            doc.add(table);
        } else {
            doc.add(new Paragraph("No rankings available for type: " + rankingType));
        }
        doc.close();
        return out.toByteArray();
    }

    private byte[] generateRankingsExcel(Camp camp, List<AthleteRanking> rankings, String rankingType) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Rankings - " + rankingType);
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Rank");
            header.createCell(1).setCellValue("Athlete");
            header.createCell(2).setCellValue("Score");
            header.createCell(3).setCellValue("Percentile");
            header.createCell(4).setCellValue("Type");

            List<AthleteRanking> filtered = rankings.stream()
                    .filter(r -> r.getRankingType().name().equalsIgnoreCase(rankingType))
                    .sorted(Comparator.comparingInt(AthleteRanking::getRankValue))
                    .collect(Collectors.toList());

            for (AthleteRanking r : filtered) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getRankValue());
                row.createCell(1).setCellValue(getAthleteName(r.getEnrollment().getUser().getId()));
                row.createCell(2).setCellValue(r.getScore() != null ? r.getScore().doubleValue() : 0);
                row.createCell(3).setCellValue(r.getPercentile() != null ? r.getPercentile().doubleValue() : 0);
                row.createCell(4).setCellValue(r.getRankingType().name());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // --- Helpers ---
    private void addPdfTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 10, Font.BOLD)));
            cell.setBackgroundColor(new Color(220, 220, 220));
            table.addCell(cell);
        }
    }

    private String getAthleteName(UUID userId) {
        return athleteProfileRepository.findByUser_Id(userId)
                .map(p -> p.getFullName())
                .orElse("Unknown");
    }
}
