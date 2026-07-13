package com.feadev.eaids.matchstats.service.impl;

import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.matchstats.dto.request.RecordMatchStatsRequest;
import com.feadev.eaids.matchstats.dto.request.StatEntryRequest;
import com.feadev.eaids.matchstats.dto.response.MatchStatDefinitionResponse;
import com.feadev.eaids.matchstats.dto.response.MatchStatEntryResponse;
import com.feadev.eaids.matchstats.dto.response.MatchStatsSummaryResponse;
import com.feadev.eaids.matchstats.model.MatchStatDefinition;
import com.feadev.eaids.matchstats.model.MatchStatEntry;
import com.feadev.eaids.matchstats.model.enums.PositionGroup;
import com.feadev.eaids.matchstats.repository.MatchStatDefinitionRepository;
import com.feadev.eaids.matchstats.repository.MatchStatEntryRepository;
import com.feadev.eaids.matchstats.service.MatchStatsService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchStatsServiceImpl implements MatchStatsService {

    private final MatchStatDefinitionRepository definitionRepository;
    private final MatchStatEntryRepository entryRepository;
    private final CampEnrollmentRepository enrollmentRepository;
    private final AthleteProfileRepository athleteProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MatchStatDefinitionResponse> getDefinitions(PositionGroup positionGroup) {
        List<MatchStatDefinition> definitions;
        if (positionGroup != null) {
            definitions = definitionRepository.findByPositionGroupAndIsActiveTrue(positionGroup);
        } else {
            definitions = definitionRepository.findByIsActiveTrue();
        }
        return definitions.stream().map(this::mapDefinitionToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MatchStatEntryResponse> recordStats(UUID campId, RecordMatchStatsRequest request, UUID recordedBy) {
        CampEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + request.getEnrollmentId()));

        if (!enrollment.getCamp().getId().equals(campId)) {
            throw new IllegalArgumentException("Enrollment does not belong to camp: " + campId);
        }

        List<MatchStatEntry> entries = new ArrayList<>();
        for (StatEntryRequest stat : request.getStats()) {
            MatchStatDefinition definition = definitionRepository.findById(stat.getDefinitionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stat definition not found: " + stat.getDefinitionId()));

            MatchStatEntry entry = MatchStatEntry.builder()
                    .enrollment(enrollment)
                    .campId(campId)
                    .definition(definition)
                    .value(stat.getValue())
                    .matchDate(request.getMatchDate())
                    .opponent(request.getOpponent())
                    .recordedBy(recordedBy)
                    .build();
            entries.add(entry);
        }

        List<MatchStatEntry> saved = entryRepository.saveAll(entries);
        log.info("Recorded {} match stats for enrollment {} in camp {}", saved.size(), request.getEnrollmentId(), campId);
        return saved.stream().map(this::mapEntryToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchStatEntryResponse> getEnrollmentStats(UUID campId, UUID enrollmentId, LocalDate matchDate) {
        List<MatchStatEntry> entries;
        if (matchDate != null) {
            entries = entryRepository.findByEnrollmentIdAndMatchDate(enrollmentId, matchDate);
        } else {
            entries = entryRepository.findByCampIdAndEnrollmentId(campId, enrollmentId);
        }
        return entries.stream().map(this::mapEntryToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MatchStatsSummaryResponse getCampSummary(UUID campId) {
        List<MatchStatEntry> allEntries = entryRepository.findByCampId(campId);

        // Group by enrollment
        Map<UUID, List<MatchStatEntry>> byEnrollment = allEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getEnrollment().getId()));

        List<MatchStatsSummaryResponse.AthleteStatSummary> athletes = new ArrayList<>();

        for (Map.Entry<UUID, List<MatchStatEntry>> entry : byEnrollment.entrySet()) {
            UUID enrollmentId = entry.getKey();
            List<MatchStatEntry> entries = entry.getValue();

            String athleteName = entries.stream().findFirst()
                    .map(e -> athleteProfileRepository.findByUser_Id(e.getEnrollment().getUser().getId())
                            .map(p -> p.getFullName()).orElse("Unknown"))
                    .orElse("Unknown");

            int gamesPlayed = (int) entries.stream()
                    .map(MatchStatEntry::getMatchDate)
                    .distinct()
                    .count();

            // Group by stat definition
            Map<UUID, List<MatchStatEntry>> byDefinition = entries.stream()
                    .collect(Collectors.groupingBy(e -> e.getDefinition().getId()));

            List<MatchStatsSummaryResponse.StatAggregate> stats = new ArrayList<>();
            for (Map.Entry<UUID, List<MatchStatEntry>> defEntry : byDefinition.entrySet()) {
                List<MatchStatEntry> defEntries = defEntry.getValue();
                MatchStatDefinition def = defEntries.get(0).getDefinition();

                BigDecimal total = defEntries.stream()
                        .map(MatchStatEntry::getValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal average = gamesPlayed > 0
                        ? total.divide(BigDecimal.valueOf(gamesPlayed), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                stats.add(MatchStatsSummaryResponse.StatAggregate.builder()
                        .statName(def.getStatName())
                        .unit(def.getUnit())
                        .total(total)
                        .average(average)
                        .build());
            }

            athletes.add(MatchStatsSummaryResponse.AthleteStatSummary.builder()
                    .enrollmentId(enrollmentId)
                    .athleteName(athleteName)
                    .gamesPlayed(gamesPlayed)
                    .stats(stats)
                    .build());
        }

        return MatchStatsSummaryResponse.builder()
                .campId(campId)
                .athletes(athletes)
                .build();
    }

    private MatchStatDefinitionResponse mapDefinitionToResponse(MatchStatDefinition def) {
        return MatchStatDefinitionResponse.builder()
                .id(def.getId())
                .positionGroup(def.getPositionGroup().name())
                .statName(def.getStatName())
                .unit(def.getUnit())
                .displayOrder(def.getDisplayOrder())
                .active(def.isActive())
                .build();
    }

    private MatchStatEntryResponse mapEntryToResponse(MatchStatEntry entry) {
        return MatchStatEntryResponse.builder()
                .id(entry.getId())
                .enrollmentId(entry.getEnrollment().getId())
                .campId(entry.getCampId())
                .definitionId(entry.getDefinition().getId())
                .statName(entry.getDefinition().getStatName())
                .positionGroup(entry.getDefinition().getPositionGroup().name())
                .value(entry.getValue())
                .unit(entry.getDefinition().getUnit())
                .matchDate(entry.getMatchDate())
                .opponent(entry.getOpponent())
                .createdDate(entry.getCreatedDate())
                .build();
    }
}
