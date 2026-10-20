package com.feadev.eaids.analytics.injuryrisk.enrichment;

import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
import com.feadev.eaids.medical.model.AthleteInjury;
import com.feadev.eaids.medical.model.AthleteMedicalProfile;
import com.feadev.eaids.medical.model.MedicalClearance;
import com.feadev.eaids.medical.repository.AthleteMedicalProfileRepository;
import com.feadev.eaids.medical.repository.AthleteInjuryRepository;
import com.feadev.eaids.medical.repository.MedicalClearanceRepository;
import com.feadev.eaids.medical.risk.InjuryRiskEngine;
import com.feadev.eaids.medical.risk.InjuryRiskResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Bridge component that fetches all medical data for an athlete and delegates to
 * {@link InjuryRiskEngine} to produce a {@link MedicalRiskSnapshot} expressed in
 * the analytics module's {@link RiskLevel} vocabulary.
 *
 * <p>This class intentionally lives in the analytics module so that the analytics
 * services never depend on medical service internals. Repositories are accessed
 * directly rather than through the medical service layer.
 *
 * <p>The key enum mapping applied here:
 * <ul>
 *   <li>{@code medical.risk.RiskLevel.MEDIUM} → {@code analytics.RiskLevel.MODERATE}</li>
 *   <li>All other values map by name (LOW, HIGH, CRITICAL)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalRiskEnricher {

    private final AthleteInjuryRepository injuryRepository;
    private final AthleteMedicalProfileRepository profileRepository;
    private final MedicalClearanceRepository clearanceRepository;
    private final InjuryRiskEngine injuryRiskEngine;

    /**
     * Computes the medical-history-based injury risk for the given athlete.
     *
     * @param athleteUserId the athlete's user ID
     * @return a fully populated {@link MedicalRiskSnapshot}; never {@code null}
     */
    public MedicalRiskSnapshot enrich(UUID athleteUserId) {
        List<AthleteInjury> injuries = injuryRepository.findByAthleteUserId(athleteUserId);

        AthleteMedicalProfile profile = profileRepository
                .findByAthleteUserId(athleteUserId)
                .orElse(null);

        List<MedicalClearance> clearances = clearanceRepository.findAllByAthleteUserId(athleteUserId);

        InjuryRiskResult result = injuryRiskEngine.compute(athleteUserId, injuries, profile, clearances);

        RiskLevel analyticsLevel = mapRiskLevel(result.riskLevel());

        log.debug("Medical risk enriched for athlete {}: medicalLevel={} score={} flags={}",
                athleteUserId, analyticsLevel, result.score(), result.flags());

        return new MedicalRiskSnapshot(
                result.score(),
                analyticsLevel,
                result.flags(),
                result.summary()
        );
    }

    // -----------------------------------------------------------------------
    // Enum mapping — medical MEDIUM → analytics MODERATE
    // -----------------------------------------------------------------------

    private RiskLevel mapRiskLevel(com.feadev.eaids.medical.risk.RiskLevel medicalLevel) {
        return switch (medicalLevel) {
            case LOW      -> RiskLevel.LOW;
            case MEDIUM   -> RiskLevel.MODERATE;
            case HIGH     -> RiskLevel.HIGH;
            case CRITICAL -> RiskLevel.CRITICAL;
        };
    }
}
