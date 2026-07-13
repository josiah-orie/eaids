package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.dto.response.AuditLogResponse;
import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.dto.response.UserListResponse;
import com.feadev.eaids.registrations.event.AthleteApprovedEvent;
import com.feadev.eaids.registrations.event.CoachVerifiedEvent;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.AthleteProfile;
import com.feadev.eaids.registrations.model.AuditLog;
import com.feadev.eaids.registrations.model.CoachProfile;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AdminService;
import com.feadev.eaids.registrations.service.AuditService;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<UserListResponse> listUsers(String role, String status, String search, Pageable pageable) {
        Specification<User> spec = buildUserSpecification(role, status, search);
        Page<User> usersPage = userRepository.findAll(spec, pageable);
        return usersPage.map(this::mapToUserListResponse);
    }

    @Override
    public MessageResponse approveAthlete(UUID athleteId) {
        User user = userRepository.findById(athleteId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + athleteId));

        if (user.getStatus() != UserStatus.UNDER_REVIEW) {
            throw new InvalidStateTransitionException(
                    "Cannot approve athlete. Current status is " + user.getStatus()
                            + ", expected UNDER_REVIEW");
        }

        if (user.getRole() != UserRole.ROLE_ATHLETE) {
            throw new InvalidStateTransitionException(
                    "User is not an athlete. Role: " + user.getRole());
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        userRepository.save(user);

        eventPublisher.publishEvent(new AthleteApprovedEvent(user.getId(), user.getEmail()));

        auditService.log(AuditAction.ATHLETE_APPROVAL, user.getId(),
                user.getEmail(), null, null);

        log.info("Athlete approved: userId={}, email={}", user.getId(), user.getEmail());

        return MessageResponse.builder()
                .message("Athlete approved successfully")
                .build();
    }

    @Override
    public MessageResponse verifyCoach(UUID coachId) {
        User user = userRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + coachId));

        if (user.getStatus() != UserStatus.UNDER_REVIEW) {
            throw new InvalidStateTransitionException(
                    "Cannot verify coach. Current status is " + user.getStatus()
                            + ", expected UNDER_REVIEW");
        }

        if (user.getRole() != UserRole.ROLE_COACH) {
            throw new InvalidStateTransitionException(
                    "User is not a coach. Role: " + user.getRole());
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        userRepository.save(user);

        eventPublisher.publishEvent(new CoachVerifiedEvent(user.getId(), user.getEmail()));

        auditService.log(AuditAction.COACH_APPROVAL, user.getId(),
                user.getEmail(), null, null);

        log.info("Coach verified: userId={}, email={}", user.getId(), user.getEmail());

        return MessageResponse.builder()
                .message("Coach verified successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(String action, UUID userId,
                                                LocalDateTime from, LocalDateTime to, Pageable pageable) {
        AuditAction auditAction = null;
        if (action != null && !action.isBlank()) {
            auditAction = AuditAction.valueOf(action.toUpperCase());
        }

        Page<AuditLog> auditLogs = auditService.findAll(auditAction, userId, from, to, pageable);
        return auditLogs.map(this::mapToAuditLogResponse);
    }

    private Specification<User> buildUserSpecification(String role, String status, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null && !role.isBlank()) {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                predicates.add(criteriaBuilder.equal(root.get("role"), userRole));
            }

            if (status != null && !status.isBlank()) {
                UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
                predicates.add(criteriaBuilder.equal(root.get("status"), userStatus));
            }

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";

                Predicate emailMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate phoneMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("phoneNumber")), searchPattern);

                // Subquery for athlete profile fullName match
                Subquery<UUID> athleteSubquery = query.subquery(UUID.class);
                Root<AthleteProfile> athleteRoot = athleteSubquery.from(AthleteProfile.class);
                athleteSubquery.select(athleteRoot.get("user").get("id"))
                        .where(criteriaBuilder.like(
                                criteriaBuilder.lower(athleteRoot.get("fullName")), searchPattern));
                Predicate athleteNameMatch = root.get("id").in(athleteSubquery);

                // Subquery for coach profile fullName match
                Subquery<UUID> coachSubquery = query.subquery(UUID.class);
                Root<CoachProfile> coachRoot = coachSubquery.from(CoachProfile.class);
                coachSubquery.select(coachRoot.get("user").get("id"))
                        .where(criteriaBuilder.like(
                                criteriaBuilder.lower(coachRoot.get("fullName")), searchPattern));
                Predicate coachNameMatch = root.get("id").in(coachSubquery);

                predicates.add(criteriaBuilder.or(emailMatch, phoneMatch, athleteNameMatch, coachNameMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private UserListResponse mapToUserListResponse(User user) {
        String fullName = resolveFullName(user);

        return UserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .fullName(fullName)
                .build();
    }

    private String resolveFullName(User user) {
        if (user.getRole() == UserRole.ROLE_ATHLETE) {
            return athleteProfileRepository.findByUser_Id(user.getId())
                    .map(AthleteProfile::getFullName)
                    .orElse(null);
        } else if (user.getRole() == UserRole.ROLE_COACH) {
            return coachProfileRepository.findByUser_Id(user.getId())
                    .map(CoachProfile::getFullName)
                    .orElse(null);
        }
        return null;
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction().name())
                .userId(auditLog.getUserId())
                .actorIdentifier(auditLog.getActorIdentifier())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .details(auditLog.getDetails())
                .createdDate(auditLog.getCreatedDate())
                .build();
    }
}
