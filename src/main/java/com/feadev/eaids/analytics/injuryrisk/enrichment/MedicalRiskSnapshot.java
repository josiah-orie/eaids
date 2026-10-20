package com.feadev.eaids.analytics.injuryrisk.enrichment;

import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;

import java.util.List;

/**
 * Immutable snapshot of the medical-history-based injury risk for an athlete,
 * expressed in the analytics module's {@link RiskLevel} vocabulary.
 * <p>
 * Produced by {@link MedicalRiskEnricher} and consumed by
 * {@link OverallRiskLevelResolver} and the analytics injury risk service.
 *
 * @param medicalRiskScore  raw additive heuristic score from {@code InjuryRiskEngine}
 * @param medicalRiskLevel  classified risk level mapped to analytics enum
 *                          (medical MEDIUM is mapped to analytics MODERATE)
 * @param medicalFlags      list of flag codes explaining the score
 * @param medicalSummary    plain-English one-sentence summary
 */
public record MedicalRiskSnapshot(
        int medicalRiskScore,
        RiskLevel medicalRiskLevel,
        List<String> medicalFlags,
        String medicalSummary
) {}
