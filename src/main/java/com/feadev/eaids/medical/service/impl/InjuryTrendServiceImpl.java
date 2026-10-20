package com.feadev.eaids.medical.service.impl;

import com.feadev.eaids.league.model.TeamPlayer;
import com.feadev.eaids.league.repository.TeamPlayerRepository;
import com.feadev.eaids.medical.dto.request.InjuryTrendQueryParams;
import com.feadev.eaids.medical.dto.response.InjuryTrendResponse;
import com.feadev.eaids.medical.dto.response.InjuryTrendResponse.*;
import com.feadev.eaids.medical.model.AthleteInjury;
import com.feadev.eaids.medical.repository.AthleteInjuryRepository;
import com.feadev.eaids.medical.service.InjuryTrendService;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InjuryTrendServiceImpl implements InjuryTrendService {

    private static final Set<String> CLOSED_STATUSES = Set.of("RESOLVED", "CLOSED");
    private static final DateTimeFormatter YEAR_MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AthleteInjuryRepository injuryRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final UserRepository userRepository;

    @Override
    public InjuryTrendResponse getTrends(InjuryTrendQueryParams params) {
        // Apply defaults
        LocalDate endDate   = params.getEndDate()   != null ? params.getEndDate()   : LocalDate.now();
        LocalDate startDate = params.getStartDate() != null ? params.getStartDate() : endDate.minusYears(1);

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt   = endDate.atTime(23, 59, 59);

        // Load injuries in the date window, optionally filtered by team/league
        List<AthleteInjury> injuries = loadInjuries(params, startDt, endDt);

        // Apply optional body region filter
        if (params.getBodyRegion() != null) {
            String regionName = params.getBodyRegion().name();
            injuries = injuries.stream()
                    .filter(i -> regionName.equalsIgnoreCase(i.getBodyRegion()))
                    .collect(Collectors.toList());
        }

        int total = injuries.size();

        // --- Injuries by body region ---
        List<BodyRegionCount> byRegion = injuries.stream()
                .filter(i -> StringUtils.hasText(i.getBodyRegion()))
                .collect(Collectors.groupingBy(i -> i.getBodyRegion().toUpperCase(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new BodyRegionCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // --- Injuries by category ---
        List<CategoryCount> byCategory = injuries.stream()
                .filter(i -> StringUtils.hasText(i.getInjuryCategory()))
                .collect(Collectors.groupingBy(i -> i.getInjuryCategory().toUpperCase(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // --- Monthly counts ---
        List<MonthlyCount> monthly = injuries.stream()
                .filter(i -> i.getIncidentDate() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getIncidentDate().format(YEAR_MONTH_FMT),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new MonthlyCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // --- Top 5 injured athletes ---
        Map<UUID, Long> countByAthlete = injuries.stream()
                .collect(Collectors.groupingBy(AthleteInjury::getAthleteUserId, Collectors.counting()));

        List<AthleteInjuryCount> topAthletes = countByAthlete.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    String name = userRepository.findById(e.getKey())
                            .map(User::getEmail).orElse("Unknown");
                    return new AthleteInjuryCount(e.getKey(), name, e.getValue());
                })
                .collect(Collectors.toList());

        // --- Average recovery days by severity ---
        Map<String, Double> avgRecovery = injuries.stream()
                .filter(i -> StringUtils.hasText(i.getSeverity())
                        && CLOSED_STATUSES.contains(i.getStatus() != null ? i.getStatus() : "")
                        && i.getIncidentDate() != null
                        && i.getLastModifiedDate() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getSeverity().toUpperCase(),
                        Collectors.averagingLong(i ->
                                ChronoUnit.DAYS.between(i.getIncidentDate(), i.getLastModifiedDate()))));

        log.debug("Injury trend computed: {} injuries in window [{} – {}]", total, startDate, endDate);

        return InjuryTrendResponse.builder()
                .totalInjuriesInPeriod(total)
                .injuriesByBodyRegion(byRegion)
                .injuriesByCategory(byCategory)
                .monthlyInjuryCounts(monthly)
                .topInjuredAthletes(topAthletes)
                .averageRecoveryDaysBySeverity(avgRecovery)
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private List<AthleteInjury> loadInjuries(InjuryTrendQueryParams params,
                                               LocalDateTime start,
                                               LocalDateTime end) {
        if (params.getTeamId() != null || params.getLeagueId() != null) {
            List<TeamPlayer> players;
            if (params.getTeamId() != null) {
                players = teamPlayerRepository.findByTeamId(params.getTeamId());
            } else {
                players = teamPlayerRepository.findByTeamLeagueId(params.getLeagueId());
            }
            List<UUID> athleteIds = players.stream()
                    .map(tp -> tp.getAthlete().getId())
                    .distinct()
                    .collect(Collectors.toList());
            if (athleteIds.isEmpty()) return Collections.emptyList();
            return injuryRepository.findByAthleteUserIdInAndIncidentDateBetween(athleteIds, start, end);
        }
        return injuryRepository.findByIncidentDateBetween(start, end);
    }
}
