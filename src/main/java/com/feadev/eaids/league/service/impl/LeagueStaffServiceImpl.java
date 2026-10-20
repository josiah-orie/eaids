package com.feadev.eaids.league.service.impl;

import com.feadev.eaids.league.dto.request.AssignStaffRequest;
import com.feadev.eaids.league.dto.request.UpdateStaffRequest;
import com.feadev.eaids.league.dto.response.LeagueStaffResponse;
import com.feadev.eaids.league.model.League;
import com.feadev.eaids.league.model.LeagueStaff;
import com.feadev.eaids.league.repository.LeagueRepository;
import com.feadev.eaids.league.repository.LeagueStaffRepository;
import com.feadev.eaids.league.service.LeagueStaffService;
import com.feadev.eaids.registrations.exception.AccountNotActiveException;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeagueStaffServiceImpl implements LeagueStaffService {

    private final LeagueStaffRepository staffRepository;
    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;

    @Override
    public LeagueStaffResponse assignStaff(UUID leagueId, AssignStaffRequest request) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "User must be ACTIVE to be assigned as league staff. Current status: " + user.getStatus());
        }

        if (staffRepository.existsByLeagueIdAndUserIdAndStaffRole(
                leagueId, request.getUserId(), request.getStaffRole())) {
            throw new DuplicateResourceException(
                    "User is already assigned the role '" + request.getStaffRole() + "' on this league");
        }

        LeagueStaff staff = LeagueStaff.builder()
                .league(league)
                .user(user)
                .staffRole(request.getStaffRole())
                .notes(request.getNotes())
                .assignedDate(LocalDateTime.now())
                .build();

        LeagueStaff saved = staffRepository.save(staff);
        log.info("Staff assigned: user {} as {} on league {}", user.getEmail(), request.getStaffRole(), leagueId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueStaffResponse> getStaffForLeague(UUID leagueId) {
        if (!leagueRepository.existsById(leagueId)) {
            throw new ResourceNotFoundException("League not found with id: " + leagueId);
        }
        return staffRepository.findByLeagueId(leagueId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LeagueStaffResponse updateStaffAssignment(UUID staffId, UpdateStaffRequest request) {
        LeagueStaff staff = findStaffOrThrow(staffId);

        if (request.getStaffRole() != null) {
            // Check for duplicate only if the role is actually changing
            if (request.getStaffRole() != staff.getStaffRole()) {
                if (staffRepository.existsByLeagueIdAndUserIdAndStaffRoleAndIdNot(
                        staff.getLeague().getId(), staff.getUser().getId(), request.getStaffRole(), staffId)) {
                    throw new DuplicateResourceException(
                            "User is already assigned the role '" + request.getStaffRole() + "' on this league");
                }
            }
            staff.setStaffRole(request.getStaffRole());
        }
        if (request.getNotes() != null) {
            staff.setNotes(request.getNotes());
        }

        LeagueStaff saved = staffRepository.save(staff);
        log.info("Staff assignment updated: {} ({})", staffId, saved.getStaffRole());
        return mapToResponse(saved);
    }

    @Override
    public void removeStaff(UUID staffId) {
        LeagueStaff staff = findStaffOrThrow(staffId);
        staffRepository.delete(staff);
        log.info("Staff assignment removed: {} (user={}, league={})",
                staffId, staff.getUser().getId(), staff.getLeague().getId());
    }

    private LeagueStaff findStaffOrThrow(UUID staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff assignment not found with id: " + staffId));
    }

    private LeagueStaffResponse mapToResponse(LeagueStaff staff) {
        return LeagueStaffResponse.builder()
                .id(staff.getId())
                .leagueId(staff.getLeague().getId())
                .leagueName(staff.getLeague().getName())
                .userId(staff.getUser().getId())
                .userEmail(staff.getUser().getEmail())
                .staffRole(staff.getStaffRole().name())
                .notes(staff.getNotes())
                .assignedDate(staff.getAssignedDate())
                .build();
    }
}
