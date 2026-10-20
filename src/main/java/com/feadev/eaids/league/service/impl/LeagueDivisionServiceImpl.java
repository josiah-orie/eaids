package com.feadev.eaids.league.service.impl;

import com.feadev.eaids.league.dto.request.CreateDivisionRequest;
import com.feadev.eaids.league.dto.request.UpdateDivisionRequest;
import com.feadev.eaids.league.dto.response.DivisionResponse;
import com.feadev.eaids.league.model.League;
import com.feadev.eaids.league.model.LeagueDivision;
import com.feadev.eaids.league.repository.LeagueDivisionRepository;
import com.feadev.eaids.league.repository.LeagueRepository;
import com.feadev.eaids.league.repository.TeamRepository;
import com.feadev.eaids.league.service.LeagueDivisionService;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeagueDivisionServiceImpl implements LeagueDivisionService {

    private final LeagueDivisionRepository divisionRepository;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    @Override
    public DivisionResponse createDivision(UUID leagueId, CreateDivisionRequest request) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));

        if (divisionRepository.existsByLeagueIdAndName(leagueId, request.getName())) {
            throw new DuplicateResourceException(
                    "A division named '" + request.getName() + "' already exists in this league");
        }

        LeagueDivision division = LeagueDivision.builder()
                .league(league)
                .name(request.getName())
                .tierRank(request.getTierRank())
                .description(request.getDescription())
                .build();

        LeagueDivision saved = divisionRepository.save(division);
        log.info("Division created: '{}' (tier {}) in league {}", saved.getName(), saved.getTierRank(), leagueId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DivisionResponse> getDivisionsForLeague(UUID leagueId) {
        if (!leagueRepository.existsById(leagueId)) {
            throw new ResourceNotFoundException("League not found with id: " + leagueId);
        }
        return divisionRepository.findByLeagueIdOrderByTierRank(leagueId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DivisionResponse getDivision(UUID divisionId) {
        return mapToResponse(findDivisionOrThrow(divisionId));
    }

    @Override
    public DivisionResponse updateDivision(UUID divisionId, UpdateDivisionRequest request) {
        LeagueDivision division = findDivisionOrThrow(divisionId);

        if (request.getName() != null) {
            if (divisionRepository.existsByLeagueIdAndNameAndIdNot(
                    division.getLeague().getId(), request.getName(), divisionId)) {
                throw new DuplicateResourceException(
                        "A division named '" + request.getName() + "' already exists in this league");
            }
            division.setName(request.getName());
        }
        if (request.getTierRank() != null) division.setTierRank(request.getTierRank());
        if (request.getDescription() != null) division.setDescription(request.getDescription());

        LeagueDivision saved = divisionRepository.save(division);
        log.info("Division updated: '{}' ({})", saved.getName(), divisionId);
        return mapToResponse(saved);
    }

    @Override
    public void deleteDivision(UUID divisionId) {
        LeagueDivision division = findDivisionOrThrow(divisionId);

        if (teamRepository.existsByDivisionId(divisionId)) {
            throw new InvalidStateTransitionException(
                    "Cannot delete a division that has teams assigned to it");
        }

        divisionRepository.delete(division);
        log.info("Division deleted: {}", divisionId);
    }

    private LeagueDivision findDivisionOrThrow(UUID divisionId) {
        return divisionRepository.findById(divisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + divisionId));
    }

    private DivisionResponse mapToResponse(LeagueDivision d) {
        return DivisionResponse.builder()
                .id(d.getId())
                .leagueId(d.getLeague().getId())
                .leagueName(d.getLeague().getName())
                .name(d.getName())
                .tierRank(d.getTierRank())
                .description(d.getDescription())
                .createdDate(d.getCreatedDate())
                .lastModifiedDate(d.getLastModifiedDate())
                .build();
    }
}
