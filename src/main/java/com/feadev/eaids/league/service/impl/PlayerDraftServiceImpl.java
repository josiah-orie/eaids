package com.feadev.eaids.league.service.impl;

import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.league.dto.request.DraftPlayerRequest;
import com.feadev.eaids.league.dto.request.TransferPlayerRequest;
import com.feadev.eaids.league.dto.response.PlayerTransferResponse;
import com.feadev.eaids.league.dto.response.TeamPlayerResponse;
import com.feadev.eaids.league.model.PlayerTransfer;
import com.feadev.eaids.league.model.Team;
import com.feadev.eaids.league.model.TeamPlayer;
import com.feadev.eaids.league.model.enums.PlayerStatus;
import com.feadev.eaids.league.model.enums.TransferType;
import com.feadev.eaids.league.repository.PlayerTransferRepository;
import com.feadev.eaids.league.repository.TeamPlayerRepository;
import com.feadev.eaids.league.repository.TeamRepository;
import com.feadev.eaids.league.service.PlayerDraftService;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlayerDraftServiceImpl implements PlayerDraftService {

    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final PlayerTransferRepository transferRepository;
    private final UserRepository userRepository;
    private final CampEnrollmentRepository campEnrollmentRepository;

    @Override
    public TeamPlayerResponse draftPlayer(UUID teamId, DraftPlayerRequest request) {
        Team team = findTeamOrThrow(teamId);

        User athlete = userRepository.findById(request.getAthleteUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getAthleteUserId()));

        // Must be a registered athlete
        if (athlete.getRole() != UserRole.ROLE_ATHLETE) {
            throw new InvalidStateTransitionException(
                    "Only athletes (ROLE_ATHLETE) can be drafted to a team");
        }

        // Must have at least one confirmed camp enrollment
        boolean eligible = campEnrollmentRepository.findByUserId(request.getAthleteUserId())
                .stream()
                .anyMatch(e -> e.getStatus() == EnrollmentStatus.CONFIRMED);
        if (!eligible) {
            throw new InvalidStateTransitionException(
                    "Player must have at least one confirmed camp enrollment to be eligible for drafting");
        }

        // Must not already be active on any team
        if (teamPlayerRepository.findByAthleteIdAndStatus(request.getAthleteUserId(), PlayerStatus.ACTIVE).isPresent()) {
            throw new InvalidStateTransitionException(
                    "Player is already on an active team roster");
        }

        // Jersey number uniqueness on this team
        if (request.getJerseyNumber() != null &&
                teamPlayerRepository.existsByTeamIdAndJerseyNumber(teamId, request.getJerseyNumber())) {
            throw new DuplicateResourceException(
                    "Jersey number " + request.getJerseyNumber() + " is already taken on this team");
        }

        TeamPlayer teamPlayer = TeamPlayer.builder()
                .team(team)
                .athlete(athlete)
                .jerseyNumber(request.getJerseyNumber())
                .position(request.getPosition())
                .status(PlayerStatus.ACTIVE)
                .draftedDate(LocalDate.now())
                .build();
        TeamPlayer savedPlayer = teamPlayerRepository.save(teamPlayer);

        // Record the draft transfer
        PlayerTransfer transfer = PlayerTransfer.builder()
                .athleteUserId(athlete.getId())
                .fromTeam(null)
                .toTeam(team)
                .transferType(TransferType.DRAFT)
                .transferDate(LocalDate.now())
                .build();
        transferRepository.save(transfer);

        log.info("Player {} drafted to team {} (jersey={})", athlete.getId(), teamId, request.getJerseyNumber());
        return mapPlayerToResponse(savedPlayer);
    }

    @Override
    public TeamPlayerResponse transferPlayer(UUID teamPlayerId, TransferPlayerRequest request) {
        if (request.getTransferType() != TransferType.TRANSFER && request.getTransferType() != TransferType.LOAN) {
            throw new InvalidStateTransitionException(
                    "Transfer type must be TRANSFER or LOAN for this operation");
        }

        TeamPlayer currentEntry = findTeamPlayerOrThrow(teamPlayerId);
        if (currentEntry.getStatus() != PlayerStatus.ACTIVE) {
            throw new InvalidStateTransitionException(
                    "Only ACTIVE team players can be transferred");
        }

        Team targetTeam = findTeamOrThrow(request.getToTeamId());
        if (targetTeam.getId().equals(currentEntry.getTeam().getId())) {
            throw new InvalidStateTransitionException(
                    "Target team must be different from the current team");
        }

        Team previousTeam = currentEntry.getTeam();

        // Deactivate the current roster entry
        currentEntry.setStatus(PlayerStatus.TRANSFERRED);
        currentEntry.setReleasedDate(LocalDate.now());
        teamPlayerRepository.save(currentEntry);

        // Create new roster entry on target team
        TeamPlayer newEntry = TeamPlayer.builder()
                .team(targetTeam)
                .athlete(currentEntry.getAthlete())
                .jerseyNumber(currentEntry.getJerseyNumber())
                .position(currentEntry.getPosition())
                .status(PlayerStatus.ACTIVE)
                .draftedDate(LocalDate.now())
                .build();
        TeamPlayer savedNew = teamPlayerRepository.save(newEntry);

        // Record the transfer
        PlayerTransfer transfer = PlayerTransfer.builder()
                .athleteUserId(currentEntry.getAthlete().getId())
                .fromTeam(previousTeam)
                .toTeam(targetTeam)
                .transferType(request.getTransferType())
                .transferDate(LocalDate.now())
                .notes(request.getNotes())
                .build();
        transferRepository.save(transfer);

        log.info("Player {} {} from team {} to team {}",
                currentEntry.getAthlete().getId(), request.getTransferType(),
                previousTeam.getId(), targetTeam.getId());
        return mapPlayerToResponse(savedNew);
    }

    @Override
    public TeamPlayerResponse releasePlayer(UUID teamPlayerId, String notes) {
        TeamPlayer teamPlayer = findTeamPlayerOrThrow(teamPlayerId);
        if (teamPlayer.getStatus() != PlayerStatus.ACTIVE) {
            throw new InvalidStateTransitionException(
                    "Only ACTIVE team players can be released");
        }

        Team currentTeam = teamPlayer.getTeam();

        teamPlayer.setStatus(PlayerStatus.TRANSFERRED);
        teamPlayer.setReleasedDate(LocalDate.now());
        TeamPlayer saved = teamPlayerRepository.save(teamPlayer);

        // Record the release
        PlayerTransfer transfer = PlayerTransfer.builder()
                .athleteUserId(teamPlayer.getAthlete().getId())
                .fromTeam(currentTeam)
                .toTeam(null)
                .transferType(TransferType.RELEASE)
                .transferDate(LocalDate.now())
                .notes(notes)
                .build();
        transferRepository.save(transfer);

        log.info("Player {} released from team {}", teamPlayer.getAthlete().getId(), currentTeam.getId());
        return mapPlayerToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamPlayerResponse> getTeamRoster(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }
        return teamPlayerRepository.findByTeamIdAndStatus(teamId, PlayerStatus.ACTIVE).stream()
                .map(this::mapPlayerToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerTransferResponse> getPlayerTransferHistory(UUID athleteUserId) {
        if (!userRepository.existsById(athleteUserId)) {
            throw new ResourceNotFoundException("User not found with id: " + athleteUserId);
        }
        return transferRepository.findByAthleteUserIdOrderByTransferDateDesc(athleteUserId).stream()
                .map(this::mapTransferToResponse)
                .collect(Collectors.toList());
    }

    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
    }

    private TeamPlayer findTeamPlayerOrThrow(UUID teamPlayerId) {
        return teamPlayerRepository.findById(teamPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team player entry not found with id: " + teamPlayerId));
    }

    private TeamPlayerResponse mapPlayerToResponse(TeamPlayer tp) {
        return TeamPlayerResponse.builder()
                .id(tp.getId())
                .teamId(tp.getTeam().getId())
                .teamName(tp.getTeam().getName())
                .athleteUserId(tp.getAthlete().getId())
                .athleteEmail(tp.getAthlete().getEmail())
                .jerseyNumber(tp.getJerseyNumber())
                .position(tp.getPosition().name())
                .status(tp.getStatus().name())
                .draftedDate(tp.getDraftedDate())
                .releasedDate(tp.getReleasedDate())
                .build();
    }

    private PlayerTransferResponse mapTransferToResponse(PlayerTransfer pt) {
        return PlayerTransferResponse.builder()
                .id(pt.getId())
                .athleteUserId(pt.getAthleteUserId())
                .fromTeamId(pt.getFromTeam() != null ? pt.getFromTeam().getId() : null)
                .fromTeamName(pt.getFromTeam() != null ? pt.getFromTeam().getName() : null)
                .toTeamId(pt.getToTeam() != null ? pt.getToTeam().getId() : null)
                .toTeamName(pt.getToTeam() != null ? pt.getToTeam().getName() : null)
                .transferType(pt.getTransferType().name())
                .transferDate(pt.getTransferDate())
                .notes(pt.getNotes())
                .createdBy(pt.getCreatedBy())
                .createdDate(pt.getCreatedDate())
                .build();
    }
}
