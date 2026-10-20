package com.feadev.eaids.medical.controller;

import com.feadev.eaids.medical.dto.request.InjuryTrendQueryParams;
import com.feadev.eaids.medical.dto.response.InjuryTrendResponse;
import com.feadev.eaids.medical.model.enums.BodyRegion;
import com.feadev.eaids.medical.service.InjuryTrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Injury Trends", description = "Aggregated injury patterns for admin and medical staff dashboards")
public class InjuryTrendController {

    private final InjuryTrendService trendService;

    @GetMapping("/api/v1/injury-risk/trends")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICAL_OFFICER')")
    @Operation(
        summary = "Get injury trend dashboard data",
        description = "Returns injury counts by body region, category, monthly distribution, " +
                      "top injured athletes, and average recovery times. " +
                      "All filters are optional; default window is the last 12 months."
    )
    @ApiResponse(responseCode = "200", description = "Trend data returned")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public ResponseEntity<InjuryTrendResponse> getTrends(
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID leagueId,
            @RequestParam(required = false) BodyRegion bodyRegion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        InjuryTrendQueryParams params = new InjuryTrendQueryParams();
        params.setTeamId(teamId);
        params.setLeagueId(leagueId);
        params.setBodyRegion(bodyRegion);
        params.setStartDate(startDate);
        params.setEndDate(endDate);

        return ResponseEntity.ok(trendService.getTrends(params));
    }

    @GetMapping("/api/v1/injury-risk/trends/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Export injury trends as Excel workbook",
        description = "Generates a multi-sheet Excel report: " +
                      "Sheet 1 — injuries by body region; " +
                      "Sheet 2 — monthly counts; " +
                      "Sheet 3 — top injured athletes. " +
                      "Accepts the same filter parameters as the JSON trends endpoint."
    )
    @ApiResponse(responseCode = "200", description = "Excel file returned")
    public ResponseEntity<byte[]> exportTrends(
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID leagueId,
            @RequestParam(required = false) BodyRegion bodyRegion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
            throws IOException {

        InjuryTrendQueryParams params = new InjuryTrendQueryParams();
        params.setTeamId(teamId);
        params.setLeagueId(leagueId);
        params.setBodyRegion(bodyRegion);
        params.setStartDate(startDate);
        params.setEndDate(endDate);

        InjuryTrendResponse trends = trendService.getTrends(params);
        byte[] workbookBytes = buildExcelWorkbook(trends);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"injury-trends.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(workbookBytes);
    }

    // -----------------------------------------------------------------------
    // Excel workbook builder
    // -----------------------------------------------------------------------

    private byte[] buildExcelWorkbook(InjuryTrendResponse trends) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);

            // Sheet 1 — Injuries by body region
            Sheet regionSheet = workbook.createSheet("By Body Region");
            Row regionHeader = regionSheet.createRow(0);
            writeHeaderCell(regionHeader, 0, "Body Region", headerStyle);
            writeHeaderCell(regionHeader, 1, "Injury Count", headerStyle);
            int rowIdx = 1;
            for (InjuryTrendResponse.BodyRegionCount entry : trends.getInjuriesByBodyRegion()) {
                Row row = regionSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.bodyRegion());
                row.createCell(1).setCellValue(entry.count());
            }
            autoSizeColumns(regionSheet, 2);

            // Sheet 2 — Monthly injury counts
            Sheet monthlySheet = workbook.createSheet("Monthly Counts");
            Row monthlyHeader = monthlySheet.createRow(0);
            writeHeaderCell(monthlyHeader, 0, "Year-Month", headerStyle);
            writeHeaderCell(monthlyHeader, 1, "Injury Count", headerStyle);
            rowIdx = 1;
            for (InjuryTrendResponse.MonthlyCount entry : trends.getMonthlyInjuryCounts()) {
                Row row = monthlySheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.yearMonth());
                row.createCell(1).setCellValue(entry.count());
            }
            autoSizeColumns(monthlySheet, 2);

            // Sheet 3 — Top injured athletes
            Sheet athleteSheet = workbook.createSheet("Top Injured Athletes");
            Row athleteHeader = athleteSheet.createRow(0);
            writeHeaderCell(athleteHeader, 0, "Athlete", headerStyle);
            writeHeaderCell(athleteHeader, 1, "Athlete ID", headerStyle);
            writeHeaderCell(athleteHeader, 2, "Injury Count", headerStyle);
            rowIdx = 1;
            for (InjuryTrendResponse.AthleteInjuryCount entry : trends.getTopInjuredAthletes()) {
                Row row = athleteSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.athleteName());
                row.createCell(1).setCellValue(entry.athleteUserId().toString());
                row.createCell(2).setCellValue(entry.count());
            }
            autoSizeColumns(athleteSheet, 3);

            // Sheet 4 — Recovery days by severity
            Sheet recoverySheet = workbook.createSheet("Recovery by Severity");
            Row recoveryHeader = recoverySheet.createRow(0);
            writeHeaderCell(recoveryHeader, 0, "Severity", headerStyle);
            writeHeaderCell(recoveryHeader, 1, "Avg Recovery Days", headerStyle);
            rowIdx = 1;
            if (trends.getAverageRecoveryDaysBySeverity() != null) {
                for (Map.Entry<String, Double> entry : trends.getAverageRecoveryDaysBySeverity().entrySet()) {
                    Row row = recoverySheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue() != null ? entry.getValue() : 0.0);
                }
            }
            autoSizeColumns(recoverySheet, 2);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void writeHeaderCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
