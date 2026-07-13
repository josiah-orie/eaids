package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.impl.AdminServiceImpl;
import net.jqwik.api.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for admin approval state transitions.
 *
 * Property 23: Admin Approval State Transitions
 * Validates: Requirements 13.2, 13.3, 13.4, 13.5
 */
class AdminApprovalStateTransitionPropertyTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AthleteProfileRepository athleteProfileRepository = mock(AthleteProfileRepository.class);
    private final CoachProfileRepository coachProfileRepository = mock(CoachProfileRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private final AdminServiceImpl adminService = new AdminServiceImpl(
            userRepository,
            athleteProfileRepository,
            coachProfileRepository,
            auditService,
            eventPublisher
    );

    @Property
    void underReviewAthleteCanBeApprovedToActive(
            @ForAll("validEmails") String email
    ) {
        UUID athleteId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository, auditService, eventPublisher);

        User user = User.builder()
                .id(athleteId)
                .email(email)
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.UNDER_REVIEW)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(athleteId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        MessageResponse response = adminService.approveAthlete(athleteId);

        assert user.getStatus() == UserStatus.ACTIVE
                : "Expected ACTIVE after approval but got " + user.getStatus();
        assert user.isEnabled()
                : "Expected enabled to be true after approval";
        assert response.getMessage().contains("approved")
                : "Expected approval success message";
    }

    @Property
    void underReviewCoachCanBeVerifiedToActive(
            @ForAll("validEmails") String email
    ) {
        UUID coachId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository, auditService, eventPublisher);

        User user = User.builder()
                .id(coachId)
                .email(email)
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_COACH)
                .status(UserStatus.UNDER_REVIEW)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(coachId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        MessageResponse response = adminService.verifyCoach(coachId);

        assert user.getStatus() == UserStatus.ACTIVE
                : "Expected ACTIVE after verification but got " + user.getStatus();
        assert user.isEnabled()
                : "Expected enabled to be true after verification";
        assert response.getMessage().contains("verified")
                : "Expected verification success message";
    }

    @Property
    void nonUnderReviewAthleteCannotBeApproved(
            @ForAll("nonUnderReviewStatuses") UserStatus status
    ) {
        UUID athleteId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository);

        User user = User.builder()
                .id(athleteId)
                .email("athlete@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(status)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(athleteId)).thenReturn(Optional.of(user));

        try {
            adminService.approveAthlete(athleteId);
            throw new AssertionError("Expected InvalidStateTransitionException for status " + status);
        } catch (InvalidStateTransitionException e) {
            assert e.getMessage().contains("UNDER_REVIEW")
                    : "Expected message to mention UNDER_REVIEW but got: " + e.getMessage();
        }
    }

    @Property
    void nonUnderReviewCoachCannotBeVerified(
            @ForAll("nonUnderReviewStatuses") UserStatus status
    ) {
        UUID coachId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository);

        User user = User.builder()
                .id(coachId)
                .email("coach@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_COACH)
                .status(status)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(coachId)).thenReturn(Optional.of(user));

        try {
            adminService.verifyCoach(coachId);
            throw new AssertionError("Expected InvalidStateTransitionException for status " + status);
        } catch (InvalidStateTransitionException e) {
            assert e.getMessage().contains("UNDER_REVIEW")
                    : "Expected message to mention UNDER_REVIEW but got: " + e.getMessage();
        }
    }

    @Provide
    Arbitrary<String> validEmails() {
        Arbitrary<String> localPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(3)
                .ofMaxLength(15);
        Arbitrary<String> domain = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(10);
        Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "io", "ng");

        return Combinators.combine(localPart, domain, tld)
                .as((local, dom, t) -> local + "@" + dom + "." + t);
    }

    @Provide
    Arbitrary<UserStatus> nonUnderReviewStatuses() {
        return Arbitraries.of(
                UserStatus.PENDING_VERIFICATION,
                UserStatus.PENDING_CONSENT,
                UserStatus.ACTIVE,
                UserStatus.REJECTED,
                UserStatus.SUSPENDED,
                UserStatus.ARCHIVED
        );
    }
}
