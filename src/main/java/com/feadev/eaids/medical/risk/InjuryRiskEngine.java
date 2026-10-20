package com.feadev.eaids.medical.risk;

import com.feadev.eaids.medical.model.AthleteInjury;
import com.feadev.eaids.medical.model.AthleteMedicalProfile;
import com.feadev.eaids.medical.model.MedicalClearance;
import com.feadev.eaids.medical.model.enums.InjuryStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Stateless heuristic engine that computes an athlete's injury risk score
 * from their historical injury records, medical profile, and clearance status.
 *
 * <h3>Scoring rules</h3>
 * <ol>
 *   <li><b>HIGH_INJURY_FREQUENCY</b> — ≥3 injuries in the last 12 months → +2 pts</li>
 *   <li><b>RECURRENT_BODY_REGION:{region}</b> — ≥2 injuries on the same body region → +1 pt each</li>
 *   <li><b>CONCUSSION_HISTORY</b> — suspected concussion in last 24 months → +3 pts</li>
 *   <li><b>ACTIVE_INJURY</b> — any injury with an open (non-terminal) status → +1 pt</li>
 *   <li><b>RECENT_STRUCTURAL_INJURY</b> — FRACTURE or DISLOCATION in last 6 months → +2 pts</li>
 *   <li><b>CARDIAC_RISK_FLAG</b> — family cardiac history on medical profile → +1 pt</li>
 *   <li><b>PRE_EXISTING_CONDITIONS</b> — existing conditions noted on profile → +1 pt</li>
 *   <li><b>ONGOING_REHABILITATION</b> — any open injury has a rehab status recorded → +1 pt</li>
 *   <li><b>PAST_SURGERIES</b> — previous surgeries recorded on profile → +1 pt</li>
 *   <li><b>HOSPITAL_ADMISSIONS</b> — prior hospital admissions recorded on profile → +1 pt</li>
 *   <li><b>CURRENT_MEDICATIONS</b> — current medications noted on profile → +1 pt</li>
 *   <li><b>MUSCULOSKELETAL_RISK</b> — musculoskeletal notes recorded on profile → +1 pt</li>
 *   <li><b>CLEARANCE_REVOKED</b> — any clearance has status REVOKED → +2 pts</li>
 *   <li><b>CLEARANCE_NOT_CLEARED</b> — most recent clearance decision is NOT_CLEARED → +3 pts</li>
 *   <li><b>CLEARANCE_RESTRICTED</b> — most recent clearance decision is RESTRICTED → +2 pts</li>
 * </ol>
 *
 * <h3>Score → RiskLevel mapping</h3>
 * <ul>
 *   <li>CRITICAL — score ≥ 6, or (CONCUSSION_HISTORY present AND score ≥ 4)</li>
 *   <li>HIGH     — score 4–5</li>
 *   <li>MEDIUM   — score 2–3</li>
 *   <li>LOW      — score 0–1</li>
 * </ul>
 */
@Component
@Slf4j
public class InjuryRiskEngine {

    // -----------------------------------------------------------------------
    // Flag constants — original 8 rules
    // -----------------------------------------------------------------------
    public static final String FLAG_HIGH_INJURY_FREQUENCY    = "HIGH_INJURY_FREQUENCY";
    public static final String FLAG_CONCUSSION_HISTORY       = "CONCUSSION_HISTORY";
    public static final String FLAG_ACTIVE_INJURY            = "ACTIVE_INJURY";
    public static final String FLAG_RECENT_STRUCTURAL_INJURY = "RECENT_STRUCTURAL_INJURY";
    public static final String FLAG_CARDIAC_RISK             = "CARDIAC_RISK_FLAG";
    public static final String FLAG_PRE_EXISTING_CONDITIONS  = "PRE_EXISTING_CONDITIONS";
    public static final String FLAG_ONGOING_REHABILITATION   = "ONGOING_REHABILITATION";

    // -----------------------------------------------------------------------
    // Flag constants — new rules (medical profile + clearance)
    // -----------------------------------------------------------------------
    public static final String FLAG_PAST_SURGERIES          = "PAST_SURGERIES";
    public static final String FLAG_HOSPITAL_ADMISSIONS     = "HOSPITAL_ADMISSIONS";
    public static final String FLAG_CURRENT_MEDICATIONS     = "CURRENT_MEDICATIONS";
    public static final String FLAG_MUSCULOSKELETAL_RISK    = "MUSCULOSKELETAL_RISK";
    public static final String FLAG_CLEARANCE_NOT_CLEARED   = "CLEARANCE_NOT_CLEARED";
    public static final String FLAG_CLEARANCE_RESTRICTED    = "CLEARANCE_RESTRICTED";
    public static final String FLAG_CLEARANCE_REVOKED       = "CLEARANCE_REVOKED";

    private static final Set<String> OPEN_STATUSES = Set.of(
            InjuryStatus.REPORTED.name(),
            InjuryStatus.AWAITING_ASSESSMENT.name(),
            InjuryStatus.UNDER_TREATMENT.name(),
            InjuryStatus.REHABILITATION.name(),
            InjuryStatus.AWAITING_CLEARANCE.name()
    );

    private static final Set<String> STRUCTURAL_CATEGORIES = Set.of("FRACTURE", "DISLOCATION");

    // -----------------------------------------------------------------------
    // Backwards-compatible 3-arg overload — delegates with empty clearances
    // -----------------------------------------------------------------------

    /**
     * Computes the injury risk for an athlete using injury history and medical profile only.
     * Equivalent to calling {@link #compute(UUID, List, AthleteMedicalProfile, List)} with an
     * empty clearances list.
     */
    public InjuryRiskResult compute(UUID athleteUserId,
                                     List<AthleteInjury> injuries,
                                     AthleteMedicalProfile profile) {
        return compute(athleteUserId, injuries, profile, Collections.emptyList());
    }

    // -----------------------------------------------------------------------
    // Primary 4-arg method — includes clearance rules
    // -----------------------------------------------------------------------

    /**
     * Computes the injury risk for an athlete.
     *
     * @param athleteUserId UUID of the athlete (used only for logging)
     * @param injuries      full injury history for the athlete (may be empty or null)
     * @param profile       medical profile (may be {@code null})
     * @param clearances    all medical clearances for the athlete (may be empty or null)
     * @return a non-null {@link InjuryRiskResult}
     */
    public InjuryRiskResult compute(UUID athleteUserId,
                                     List<AthleteInjury> injuries,
                                     AthleteMedicalProfile profile,
                                     List<MedicalClearance> clearances) {
        List<String> flags = new ArrayList<>();
        int score = 0;
        LocalDateTime now = LocalDateTime.now();

        if (injuries == null) {
            injuries = Collections.emptyList();
        }
        if (clearances == null) {
            clearances = Collections.emptyList();
        }

        // --- Rule 1: High injury frequency (≥3 in last 12 months) ---
        long injuriesLast12Months = injuries.stream()
                .filter(i -> i.getIncidentDate() != null &&
                             i.getIncidentDate().isAfter(now.minusMonths(12)))
                .count();
        if (injuriesLast12Months >= 3) {
            flags.add(FLAG_HIGH_INJURY_FREQUENCY);
            score += 2;
        }

        // --- Rule 2: Recurring body region (≥2 injuries on same region) ---
        Map<String, Long> byRegion = injuries.stream()
                .filter(i -> StringUtils.hasText(i.getBodyRegion()))
                .collect(Collectors.groupingBy(i -> i.getBodyRegion().toUpperCase(), Collectors.counting()));

        for (Map.Entry<String, Long> entry : byRegion.entrySet()) {
            if (entry.getValue() >= 2) {
                flags.add("RECURRENT_BODY_REGION:" + entry.getKey());
                score += 1;
            }
        }

        // --- Rule 3: Concussion history (suspected concussion in last 24 months) ---
        boolean hasConcussionHistory = injuries.stream()
                .anyMatch(i -> i.isSuspectedConcussion() &&
                               i.getIncidentDate() != null &&
                               i.getIncidentDate().isAfter(now.minusMonths(24)));
        if (hasConcussionHistory) {
            flags.add(FLAG_CONCUSSION_HISTORY);
            score += 3;
        }

        // --- Rule 4: Active injury ---
        boolean hasActiveInjury = injuries.stream()
                .anyMatch(i -> i.getStatus() != null && OPEN_STATUSES.contains(i.getStatus()));
        if (hasActiveInjury) {
            flags.add(FLAG_ACTIVE_INJURY);
            score += 1;
        }

        // --- Rule 5: Recent structural injury (FRACTURE or DISLOCATION in last 6 months) ---
        boolean hasStructuralInjury = injuries.stream()
                .anyMatch(i -> i.getInjuryCategory() != null &&
                               STRUCTURAL_CATEGORIES.contains(i.getInjuryCategory().toUpperCase()) &&
                               i.getIncidentDate() != null &&
                               i.getIncidentDate().isAfter(now.minusMonths(6)));
        if (hasStructuralInjury) {
            flags.add(FLAG_RECENT_STRUCTURAL_INJURY);
            score += 2;
        }

        // --- Rule 6: Cardiac risk flag (from medical profile) ---
        if (profile != null && profile.isFamilyCardiacHistory()) {
            flags.add(FLAG_CARDIAC_RISK);
            score += 1;
        }

        // --- Rule 7: Pre-existing conditions (from medical profile) ---
        if (profile != null && StringUtils.hasText(profile.getExistingConditions())) {
            flags.add(FLAG_PRE_EXISTING_CONDITIONS);
            score += 1;
        }

        // --- Rule 8: Ongoing rehabilitation ---
        boolean hasOngoingRehab = injuries.stream()
                .anyMatch(i -> OPEN_STATUSES.contains(i.getStatus() != null ? i.getStatus() : "") &&
                               StringUtils.hasText(i.getRehabilitationStatus()));
        if (hasOngoingRehab) {
            flags.add(FLAG_ONGOING_REHABILITATION);
            score += 1;
        }

        // --- Rule 9: Past surgeries (from medical profile) ---
        if (profile != null && StringUtils.hasText(profile.getPreviousSurgeries())) {
            flags.add(FLAG_PAST_SURGERIES);
            score += 1;
        }

        // --- Rule 10: Hospital admissions (from medical profile) ---
        if (profile != null && StringUtils.hasText(profile.getHospitalAdmissions())) {
            flags.add(FLAG_HOSPITAL_ADMISSIONS);
            score += 1;
        }

        // --- Rule 11: Current medications (from medical profile) ---
        if (profile != null && StringUtils.hasText(profile.getCurrentMedications())) {
            flags.add(FLAG_CURRENT_MEDICATIONS);
            score += 1;
        }

        // --- Rule 12: Musculoskeletal risk (from medical profile) ---
        if (profile != null && StringUtils.hasText(profile.getMusculoskeletalNotes())) {
            flags.add(FLAG_MUSCULOSKELETAL_RISK);
            score += 1;
        }

        // --- Rule 13: Clearance revoked (any revoked clearance) ---
        boolean hasClearanceRevoked = clearances.stream()
                .anyMatch(c -> "REVOKED".equals(c.getStatus()));
        if (hasClearanceRevoked) {
            flags.add(FLAG_CLEARANCE_REVOKED);
            score += 2;
        }

        // --- Rules 14/15: Most recent clearance decision (NOT_CLEARED / RESTRICTED) ---
        clearances.stream()
                .filter(c -> c.getDecisionDate() != null)
                .max(Comparator.comparing(MedicalClearance::getDecisionDate))
                .ifPresent(latest -> {
                    if ("NOT_CLEARED".equals(latest.getStatus())) {
                        flags.add(FLAG_CLEARANCE_NOT_CLEARED);
                    } else if ("RESTRICTED".equals(latest.getStatus())) {
                        flags.add(FLAG_CLEARANCE_RESTRICTED);
                    }
                });

        // Add points after the stream (can't modify score inside lambda)
        if (flags.contains(FLAG_CLEARANCE_NOT_CLEARED)) {
            score += 3;
        } else if (flags.contains(FLAG_CLEARANCE_RESTRICTED)) {
            score += 2;
        }

        // --- Map score to RiskLevel ---
        RiskLevel level = toRiskLevel(score, hasConcussionHistory);

        String summary = buildSummary(level, flags, score);
        log.debug("Injury risk computed for athlete {}: level={} score={} flags={}",
                athleteUserId, level, score, flags);

        return new InjuryRiskResult(level, Collections.unmodifiableList(flags), summary, now, score);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private RiskLevel toRiskLevel(int score, boolean hasConcussion) {
        if (score >= 6 || (hasConcussion && score >= 4)) return RiskLevel.CRITICAL;
        if (score >= 4)                                   return RiskLevel.HIGH;
        if (score >= 2)                                   return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private String buildSummary(RiskLevel level, List<String> flags, int score) {
        if (flags.isEmpty()) {
            return "LOW risk: no significant injury risk indicators found.";
        }

        List<String> readable = flags.stream()
                .map(f -> switch (f) {
                    case FLAG_HIGH_INJURY_FREQUENCY    -> "high injury frequency";
                    case FLAG_CONCUSSION_HISTORY       -> "concussion history";
                    case FLAG_ACTIVE_INJURY            -> "active injury";
                    case FLAG_RECENT_STRUCTURAL_INJURY -> "recent structural injury";
                    case FLAG_CARDIAC_RISK             -> "cardiac risk factor";
                    case FLAG_PRE_EXISTING_CONDITIONS  -> "pre-existing conditions";
                    case FLAG_ONGOING_REHABILITATION   -> "ongoing rehabilitation";
                    case FLAG_PAST_SURGERIES           -> "previous surgeries on record";
                    case FLAG_HOSPITAL_ADMISSIONS      -> "prior hospital admissions";
                    case FLAG_CURRENT_MEDICATIONS      -> "current medications noted";
                    case FLAG_MUSCULOSKELETAL_RISK     -> "musculoskeletal concerns noted";
                    case FLAG_CLEARANCE_NOT_CLEARED    -> "medically not cleared for participation";
                    case FLAG_CLEARANCE_RESTRICTED     -> "participation restricted by medical clearance";
                    case FLAG_CLEARANCE_REVOKED        -> "medical clearance revoked";
                    default -> f.startsWith("RECURRENT_BODY_REGION:")
                            ? "recurring " + f.substring("RECURRENT_BODY_REGION:".length()).toLowerCase() + " injury"
                            : f.toLowerCase().replace('_', ' ');
                })
                .collect(Collectors.toList());

        String flagText = readable.size() <= 3
                ? String.join(", ", readable)
                : String.join(", ", readable.subList(0, 3)) + " and " + (readable.size() - 3) + " more";

        return level.name() + " risk (score " + score + "): " + flagText + ".";
    }
}
