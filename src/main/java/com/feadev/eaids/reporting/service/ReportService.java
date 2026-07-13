package com.feadev.eaids.reporting.service;

import com.feadev.eaids.reporting.model.ReportFormat;

import java.util.UUID;

public interface ReportService {

    byte[] generateCampSummary(UUID campId, ReportFormat format);

    byte[] generateAthleteEvaluation(UUID athleteId, UUID campId, ReportFormat format);

    byte[] generateCoachActivity(UUID coachId, ReportFormat format);

    byte[] generateSelectionReport(UUID campId, ReportFormat format);

    byte[] generateRankingsReport(UUID campId, String rankingType, ReportFormat format);
}
