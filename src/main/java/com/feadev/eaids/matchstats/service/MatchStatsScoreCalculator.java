package com.feadev.eaids.matchstats.service;

import com.feadev.eaids.matchstats.model.MatchStatEntry;
import com.feadev.eaids.matchstats.repository.MatchStatEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates a normalized 0-100 match stats score for an enrollment
 * relative to other athletes in the same camp (percentile-based).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MatchStatsScoreCalculator {

    private final MatchStatEntryRepository entryRepository;

    /**
     * Calculate a match stats score for a given enrollment in a camp.
     * Uses percentile-based normalization against camp peers.
     *
     * @return normalized score 0-100, or null if no match stats exist for this enrollment
     */
    public BigDecimal calculateScore(UUID enrollmentId, UUID campId) {
        List<MatchStatEntry> athleteEntries = entryRepository.findByCampIdAndEnrollmentId(campId, enrollmentId);

        if (athleteEntries.isEmpty()) {
            return null;
        }

        // Get all entries for the camp to establish peer context
        List<MatchStatEntry> allCampEntries = entryRepository.findByCampId(campId);

        // Group all camp entries by enrollment
        Map<UUID, List<MatchStatEntry>> byEnrollment = allCampEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getEnrollment().getId()));

        if (byEnrollment.size() <= 1) {
            // Only one athlete has stats; give a baseline score of 50
            return new BigDecimal("50.00");
        }

        // Calculate a composite "total stat value" per athlete (sum of all stat values)
        Map<UUID, BigDecimal> totals = new HashMap<>();
        for (Map.Entry<UUID, List<MatchStatEntry>> entry : byEnrollment.entrySet()) {
            BigDecimal total = entry.getValue().stream()
                    .map(MatchStatEntry::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totals.put(entry.getKey(), total);
        }

        // Calculate percentile rank for this athlete
        BigDecimal athleteTotal = totals.getOrDefault(enrollmentId, BigDecimal.ZERO);
        long belowCount = totals.values().stream()
                .filter(t -> t.compareTo(athleteTotal) < 0)
                .count();

        BigDecimal percentile = BigDecimal.valueOf(belowCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totals.size() - 1), 2, RoundingMode.HALF_UP);

        // Clamp to 0-100
        if (percentile.compareTo(BigDecimal.ZERO) < 0) {
            percentile = BigDecimal.ZERO;
        }
        if (percentile.compareTo(new BigDecimal("100.00")) > 0) {
            percentile = new BigDecimal("100.00");
        }

        return percentile.setScale(2, RoundingMode.HALF_UP);
    }
}
