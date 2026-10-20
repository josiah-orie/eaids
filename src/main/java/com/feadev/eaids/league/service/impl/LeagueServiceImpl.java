package com.feadev.eaids.league.service.impl;

import com.feadev.eaids.league.dto.request.CreateLeagueRequest;
import com.feadev.eaids.league.dto.request.UpdateLeagueRequest;
import com.feadev.eaids.league.dto.response.LeagueResponse;
import com.feadev.eaids.league.model.League;
import com.feadev.eaids.league.model.enums.LeagueStatus;
import com.feadev.eaids.league.repository.LeagueRepository;
import com.feadev.eaids.league.service.LeagueService;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;

    private static final Map<LeagueStatus, Set<LeagueStatus>> VALID_TRANSITIONS = Map.of(
            LeagueStatus.DRAFT, Set.of(LeagueStatus.ACTIVE),
            LeagueStatus.ACTIVE, Set.of(LeagueStatus.COMPLETED),
            LeagueStatus.COMPLETED, Set.of(LeagueStatus.ARCHIVED),
            LeagueStatus.ARCHIVED, Set.of()
    );

    @Override
    public LeagueResponse createLeague(CreateLeagueRequest request) {
        League league = League.builder()
                .name(request.getName())
                .description(request.getDescription())
                .seasonYear(request.getSeasonYear())
                .country(request.getCountry())
                .region(request.getRegion())
                .status(LeagueStatus.DRAFT)
                .build();
        League saved = leagueRepository.save(league);
        log.info("League created: {} ({})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueResponse getLeague(UUID leagueId) {
        return mapToResponse(findLeagueOrThrow(leagueId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueResponse> getAllLeagues() {
        return leagueRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueResponse> getLeaguesByStatus(LeagueStatus status) {
        return leagueRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueResponse> getLeaguesByCountry(String country) {
        return leagueRepository.findByCountry(country).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LeagueResponse updateLeague(UUID leagueId, UpdateLeagueRequest request) {
        League league = findLeagueOrThrow(leagueId);
        if (league.getStatus() != LeagueStatus.DRAFT) {
            throw new InvalidStateTransitionException("League can only be updated in DRAFT status");
        }
        if (request.getName() != null) league.setName(request.getName());
        if (request.getDescription() != null) league.setDescription(request.getDescription());
        if (request.getSeasonYear() != null) league.setSeasonYear(request.getSeasonYear());
        if (request.getCountry() != null) league.setCountry(request.getCountry());
        if (request.getRegion() != null) league.setRegion(request.getRegion());
        League saved = leagueRepository.save(league);
        log.info("League updated: {} ({})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public void deleteLeague(UUID leagueId) {
        League league = findLeagueOrThrow(leagueId);
        if (league.getStatus() != LeagueStatus.DRAFT) {
            throw new InvalidStateTransitionException("Only DRAFT leagues can be deleted");
        }
        leagueRepository.delete(league);
        log.info("League deleted: {}", leagueId);
    }

    @Override
    public LeagueResponse transitionStatus(UUID leagueId, LeagueStatus newStatus) {
        League league = findLeagueOrThrow(leagueId);
        LeagueStatus current = league.getStatus();
        Set<LeagueStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition league from %s to %s. Allowed transitions: %s",
                            current, newStatus, allowed));
        }
        league.setStatus(newStatus);
        League saved = leagueRepository.save(league);
        log.info("League {} transitioned from {} to {}", leagueId, current, newStatus);
        return mapToResponse(saved);
    }

    private League findLeagueOrThrow(UUID leagueId) {
        return leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));
    }

    private LeagueResponse mapToResponse(League league) {
        return LeagueResponse.builder()
                .id(league.getId())
                .name(league.getName())
                .description(league.getDescription())
                .seasonYear(league.getSeasonYear())
                .country(league.getCountry())
                .region(league.getRegion())
                .status(league.getStatus().name())
                .createdDate(league.getCreatedDate())
                .lastModifiedDate(league.getLastModifiedDate())
                .createdBy(league.getCreatedBy())
                .lastModifiedBy(league.getLastModifiedBy())
                .build();
    }
}
