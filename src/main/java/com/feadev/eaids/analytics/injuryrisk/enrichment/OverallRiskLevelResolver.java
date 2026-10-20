package com.feadev.eaids.analytics.injuryrisk.enrichment;

import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
import com.feadev.eaids.medical.risk.InjuryRiskEngine;
import org.springframework.stereotype.Component;

/**
 * Stateless component that derives a single {@code overallRiskLevel} from the
 * training-based risk level and the medical-history-based {@link MedicalRiskSnapshot}.
 *
 * <h3>Resolution logic (in order)</h3>
 * <ol>
 *   <li>Take the higher of {@code trainingLevel} and {@code medicalSnapshot.medicalRiskLevel()}
 *       by ordinal comparison.</li>
 *   <li>If the medical flags contain {@code CLEARANCE_NOT_CLEARED} or
 *       {@code CLEARANCE_REVOKED}, floor the result at {@code HIGH}.</li>
 *   <li>If the result is {@code HIGH} and either the training level is also {@code HIGH}/{@code CRITICAL}
 *       or the medical risk score is ≥ 4, escalate to {@code CRITICAL}.</li>
 * </ol>
 */
@Component
public class OverallRiskLevelResolver {

    /**
     * Resolves the overall risk level for an athlete from both sub-scores.
     *
     * @param trainingLevel   risk level derived from training load / attendance / performance
     * @param medicalSnapshot risk snapshot derived from medical history and clearance status
     * @return the combined overall {@link RiskLevel}
     */
    public RiskLevel resolve(RiskLevel trainingLevel, MedicalRiskSnapshot medicalSnapshot) {

        // Step 1: take the higher ordinal
        RiskLevel resolved = trainingLevel.ordinal() >= medicalSnapshot.medicalRiskLevel().ordinal()
                ? trainingLevel
                : medicalSnapshot.medicalRiskLevel();

        // Step 2: floor at HIGH if a critical clearance flag is present
        boolean hasClearanceFlag =
                medicalSnapshot.medicalFlags().contains(InjuryRiskEngine.FLAG_CLEARANCE_NOT_CLEARED)
                || medicalSnapshot.medicalFlags().contains(InjuryRiskEngine.FLAG_CLEARANCE_REVOKED);

        if (hasClearanceFlag && resolved.ordinal() < RiskLevel.HIGH.ordinal()) {
            resolved = RiskLevel.HIGH;
        }

        // Step 3: escalate HIGH → CRITICAL if an additional serious factor is present
        if (resolved == RiskLevel.HIGH) {
            boolean trainingSerious = trainingLevel == RiskLevel.HIGH
                    || trainingLevel == RiskLevel.CRITICAL;
            boolean medicalSerious  = medicalSnapshot.medicalRiskScore() >= 4;
            if (trainingSerious || medicalSerious) {
                resolved = RiskLevel.CRITICAL;
            }
        }

        return resolved;
    }
}
