package com.feadev.eaids.medical.risk;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable result produced by {@link InjuryRiskEngine#compute}.
 *
 * @param riskLevel   computed risk classification
 * @param flags       human-readable flag codes explaining the score
 * @param summary     one-sentence plain-English summary
 * @param computedAt  timestamp of computation (used for cache staleness checks)
 * @param score       raw additive heuristic point total before level mapping
 */
public record InjuryRiskResult(
        RiskLevel riskLevel,
        List<String> flags,
        String summary,
        LocalDateTime computedAt,
        int score
) {}
