package com.feadev.eaids.medical.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregated injury trend data for the admin/medical-officer dashboard.
 */
@Data
@Builder
public class InjuryTrendResponse {

    private int totalInjuriesInPeriod;

    /** Count of injuries per body region. */
    private List<BodyRegionCount> injuriesByBodyRegion;

    /** Count of injuries per category (FRACTURE, SPRAIN, etc.). */
    private List<CategoryCount> injuriesByCategory;

    /** Monthly injury counts (yyyy-MM label). */
    private List<MonthlyCount> monthlyInjuryCounts;

    /** Top 5 athletes with the most injuries in the period. */
    private List<AthleteInjuryCount> topInjuredAthletes;

    /**
     * Average number of days from incident to RESOLVED/CLOSED status, grouped by severity.
     * Keys are severity labels; values are averages (may be null for severities with no closed records).
     */
    private Map<String, Double> averageRecoveryDaysBySeverity;

    // -----------------------------------------------------------------------
    // Nested projection types
    // -----------------------------------------------------------------------

    public record BodyRegionCount(String bodyRegion, long count) {}

    public record CategoryCount(String category, long count) {}

    public record MonthlyCount(String yearMonth, long count) {}

    public record AthleteInjuryCount(UUID athleteUserId, String athleteName, long count) {}
}
