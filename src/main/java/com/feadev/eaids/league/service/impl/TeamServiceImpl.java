package com.feadev.eaids.league.service.impl;

import com.feadev.eaids.league.dto.request.AssignTeamCoachRequest;
import com.feadev.eaids.league.dto.request.CreateTeamRequest;
import com.feadev.eaids.league.dto.request.UpdateTeamRequest;
import com.feadev.eaids.league.dto.response.TeamCoachResponse;
import com.feadev.eaids.league.dto.response.TeamPlayerResponse;
import com.feadev.eaids.league.dto.response.TeamResponse;
import com.feadev.eaids.league.model.League;
import com.feadev.eaids.league.model.LeagueDivision;
import com.feadev.eaids.league.model.Team;
import com.feadev.eaids.league.model.TeamCoach;
import com.feadev.eaids.league.model.TeamPlayer;
import com.feadev.eaids.league.model.enums.PlayerStatus;
import com.feadev.eaids.league.model.enums.TeamCoachRole;
import com.feadev.eaids.league.repository.LeagueDivisionRepository;
import com.feadev.eaids.league.repository.LeagueRepository;
import com.feadev.eaids.league.repository.TeamCoachRepository;
import com.feadev.eaids.league.repository.TeamPlayerRepository;
import com.feadev.eaids.league.repository.TeamRepository;
import com.feadev.eaids.league.service.TeamService;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeamServiceImpl implements TeamService {

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/jpg", "image/png");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueDivisionRepository divisionRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final TeamCoachRepository teamCoachRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public TeamResponse createTeam(CreateTeamRequest request) {
        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + request.getLeagueId()));

        LeagueDivision division = null;
        if (request.getDivisionId() != null) {
            division = divisionRepository.findById(request.getDivisionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + request.getDivisionId()));
            if (!division.getLeague().getId().equals(request.getLeagueId())) {
                throw new InvalidStateTransitionException(
                        "Division does not belong to the specified league");
            }
        }

        if (teamRepository.existsByNameAndLeagueId(request.getName(), request.getLeagueId())) {
            throw new DuplicateResourceException(
                    "A team named '" + request.getName() + "' already exists in this league");
        }

        Team team = Team.builder()
                .name(request.getName())
                .league(league)
                .division(division)
                .homeVenue(request.getHomeVenue())
                .country(request.getCountry())
                .state(request.getState())
                .build();

        Team saved = teamRepository.save(team);
        log.info("Team created: '{}' ({}) in league {}", saved.getName(), saved.getId(), league.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getTeam(UUID teamId) {
        return mapToResponse(findTeamOrThrow(teamId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByLeague(UUID leagueId) {
        if (!leagueRepository.existsById(leagueId)) {
            throw new ResourceNotFoundException("League not found with id: " + leagueId);
        }
        return teamRepository.findByLeagueId(leagueId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByDivision(UUID divisionId) {
        if (!divisionRepository.existsById(divisionId)) {
            throw new ResourceNotFoundException("Division not found with id: " + divisionId);
        }
        return teamRepository.findByDivisionId(divisionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request) {
        Team team = findTeamOrThrow(teamId);

        if (request.getName() != null) {
            if (teamRepository.existsByNameAndLeagueIdAndIdNot(request.getName(), team.getLeague().getId(), teamId)) {
                throw new DuplicateResourceException(
                        "A team named '" + request.getName() + "' already exists in this league");
            }
            team.setName(request.getName());
        }

        if (request.getDivisionId() != null) {
            LeagueDivision newDivision = divisionRepository.findById(request.getDivisionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Division not found with id: " + request.getDivisionId()));
            if (!newDivision.getLeague().getId().equals(team.getLeague().getId())) {
                throw new InvalidStateTransitionException(
                        "Division does not belong to the team's league");
            }
            team.setDivision(newDivision);
        }

        if (request.getHomeVenue() != null) team.setHomeVenue(request.getHomeVenue());
        if (request.getCountry() != null) team.setCountry(request.getCountry());
        if (request.getState() != null) team.setState(request.getState());
        if (request.getStatus() != null) team.setStatus(request.getStatus());

        Team saved = teamRepository.save(team);
        log.info("Team updated: '{}' ({})", saved.getName(), teamId);
        return mapToResponse(saved);
    }

    @Override
    public void deleteTeam(UUID teamId) {
        Team team = findTeamOrThrow(teamId);

        if (teamPlayerRepository.existsByTeamIdAndStatus(teamId, PlayerStatus.ACTIVE)) {
            throw new InvalidStateTransitionException(
                    "Cannot delete a team that has active players on the roster");
        }

        // Clean up logo file if present
        if (team.getLogoUrl() != null) {
            storageService.delete(extractRelativePath(team.getLogoUrl()));
        }

        teamRepository.delete(team);
        log.info("Team deleted: {}", teamId);
    }

    @Override
    public TeamResponse uploadTeamLogo(UUID teamId, MultipartFile file) {
        validateImageFile(file);

        Team team = findTeamOrThrow(teamId);

        if (team.getLogoUrl() != null) {
            storageService.delete(extractRelativePath(team.getLogoUrl()));
        }

        String storedPath = storageService.store(file, "teams/" + teamId + "/logo");
        team.setLogoUrl(buildAbsoluteImageUrl(storedPath));
        Team saved = teamRepository.save(team);

        log.info("Logo uploaded for team {}", teamId);
        return mapToResponse(saved);
    }

    @Override
    public TeamResponse deleteTeamLogo(UUID teamId) {
        Team team = findTeamOrThrow(teamId);

        if (team.getLogoUrl() != null) {
            storageService.delete(extractRelativePath(team.getLogoUrl()));
            team.setLogoUrl(null);
            teamRepository.save(team);
            log.info("Logo deleted for team {}", teamId);
        }

        return mapToResponse(team);
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPG, JPEG, and PNG image files are allowed");
        }
    }

    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
    }

    // ── Roster ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TeamPlayerResponse> getTeamPlayers(UUID leagueId, UUID teamId) {
        Team team = findTeamOrThrow(teamId);
        if (!team.getLeague().getId().equals(leagueId)) {
            throw new ResourceNotFoundException(
                    "Team " + teamId + " does not belong to league " + leagueId);
        }
        return teamPlayerRepository.findByTeamId(teamId).stream()
                .map(this::mapToPlayerResponse)
                .collect(Collectors.toList());
    }

    private TeamPlayerResponse mapToPlayerResponse(TeamPlayer player) {
        return TeamPlayerResponse.builder()
                .teamPlayerId(player.getId())
                .athleteId(player.getAthlete().getId())
                .athleteEmail(player.getAthlete().getEmail())
                .jerseyNumber(player.getJerseyNumber())
                .position(player.getPosition().name())
                .status(player.getStatus().name())
                .draftedDate(player.getDraftedDate())
                .releasedDate(player.getReleasedDate())
                .build();
    }

    // ── Coach assignment ──────────────────────────────────────────────────────

    @Override
    public TeamCoachResponse assignCoach(UUID teamId, AssignTeamCoachRequest request) {
        Team team = findTeamOrThrow(teamId);

        User coach = userRepository.findById(request.getCoachUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getCoachUserId()));

        if (coach.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidStateTransitionException(
                    "Coach must be ACTIVE to be assigned to a team. Current status: " + coach.getStatus());
        }

        if (teamCoachRepository.existsByTeamIdAndRole(teamId, request.getRole())) {
            throw new DuplicateResourceException(
                    "A " + request.getRole().name().replace("_", " ").toLowerCase()
                    + " is already assigned to this team. Use PUT to replace them.");
        }

        TeamCoach teamCoach = TeamCoach.builder()
                .team(team)
                .coach(coach)
                .role(request.getRole())
                .notes(request.getNotes())
                .assignedDate(java.time.LocalDateTime.now())
                .build();

        TeamCoach saved = teamCoachRepository.save(teamCoach);
        log.info("Coach {} assigned as {} to team {}", coach.getEmail(), request.getRole(), teamId);
        return mapToCoachResponse(saved);
    }

    @Override
    public TeamCoachResponse updateCoach(UUID teamId, TeamCoachRole role, AssignTeamCoachRequest request) {
        findTeamOrThrow(teamId); // ensure team exists

        User newCoach = userRepository.findById(request.getCoachUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getCoachUserId()));

        if (newCoach.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidStateTransitionException(
                    "Coach must be ACTIVE to be assigned to a team. Current status: " + newCoach.getStatus());
        }

        TeamCoach existing = teamCoachRepository.findByTeamIdAndRole(teamId, role)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No " + role.name().replace("_", " ").toLowerCase()
                        + " is currently assigned to this team"));

        existing.setCoach(newCoach);
        existing.setNotes(request.getNotes());
        existing.setAssignedDate(java.time.LocalDateTime.now());

        TeamCoach saved = teamCoachRepository.save(existing);
        log.info("Coach role {} on team {} changed to {}", role, teamId, newCoach.getEmail());
        return mapToCoachResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamCoachResponse> getCoaches(UUID teamId) {
        findTeamOrThrow(teamId);
        return teamCoachRepository.findByTeamId(teamId).stream()
                .map(this::mapToCoachResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void removeCoach(UUID teamId, TeamCoachRole role) {
        findTeamOrThrow(teamId);
        TeamCoach existing = teamCoachRepository.findByTeamIdAndRole(teamId, role)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No " + role.name().replace("_", " ").toLowerCase()
                        + " is currently assigned to this team"));
        teamCoachRepository.delete(existing);
        log.info("Coach role {} removed from team {}", role, teamId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private TeamCoachResponse mapToCoachResponse(TeamCoach tc) {
        return TeamCoachResponse.builder()
                .id(tc.getId())
                .teamId(tc.getTeam().getId())
                .teamName(tc.getTeam().getName())
                .coachUserId(tc.getCoach().getId())
                .coachEmail(tc.getCoach().getEmail())
                .role(tc.getRole().name())
                .notes(tc.getNotes())
                .assignedDate(tc.getAssignedDate())
                .assignedBy(tc.getAssignedBy())
                .build();
    }

    private TeamResponse mapToResponse(Team team) {
        // Look up current coaching staff
        TeamCoachResponse headCoach = teamCoachRepository
                .findByTeamIdAndRole(team.getId(), TeamCoachRole.HEAD_COACH)
                .map(this::mapToCoachResponse)
                .orElse(null);

        TeamCoachResponse assistantCoach = teamCoachRepository
                .findByTeamIdAndRole(team.getId(), TeamCoachRole.ASSISTANT_COACH)
                .map(this::mapToCoachResponse)
                .orElse(null);

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .leagueId(team.getLeague().getId())
                .leagueName(team.getLeague().getName())
                .divisionId(team.getDivision() != null ? team.getDivision().getId() : null)
                .divisionName(team.getDivision() != null ? team.getDivision().getName() : null)
                .homeVenue(team.getHomeVenue())
                .country(team.getCountry())
                .state(team.getState())
                .logoUrl(team.getLogoUrl())
                .status(team.getStatus().name())
                .headCoach(headCoach)
                .assistantCoach(assistantCoach)
                .createdDate(team.getCreatedDate())
                .lastModifiedDate(team.getLastModifiedDate())
                .createdBy(team.getCreatedBy())
                .lastModifiedBy(team.getLastModifiedBy())
                .build();
    }

    private String buildAbsoluteImageUrl(String relativePath) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return base + "/storage/" + path;
    }

    private String extractRelativePath(String absoluteUrl) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String storagePrefix = base + "storage/";
        if (absoluteUrl.startsWith(storagePrefix)) {
            return absoluteUrl.substring(storagePrefix.length());
        }
        if (absoluteUrl.startsWith(base)) {
            return absoluteUrl.substring(base.length());
        }
        return absoluteUrl; // fallback: already a relative path
    }
}
