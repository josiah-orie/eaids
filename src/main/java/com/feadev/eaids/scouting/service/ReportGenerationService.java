package com.feadev.eaids.scouting.service;

import com.feadev.eaids.scouting.dto.response.AthleteReportResponse;

import java.util.UUID;

public interface ReportGenerationService {

    AthleteReportResponse generateJsonReport(UUID scoutUserId, UUID athleteUserId);

    byte[] generatePdfReport(UUID scoutUserId, UUID athleteUserId);
}
