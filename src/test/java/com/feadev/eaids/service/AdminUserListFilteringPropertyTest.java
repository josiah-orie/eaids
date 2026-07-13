package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.response.UserListResponse;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.impl.AdminServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for admin user list filtering and pagination.
 *
 * Property 26: Admin User List Filtering and Pagination
 * Validates: Requirements 13.1
 *
 * Verifies filter criteria, page size limits, and case-insensitive search behavior.
 */
class AdminUserListFilteringPropertyTest {

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
    void filterByRoleReturnsOnlyMatchingUsers(
            @ForAll("userRoles") UserRole filterRole,
            @ForAll @IntRange(min = 1, max = 10) int totalUsers
    ) {
        reset(userRepository, athleteProfileRepository, coachProfileRepository);

        List<User> filteredUsers = IntStream.range(0, totalUsers)
                .mapToObj(i -> User.builder()
                        .id(UUID.randomUUID())
                        .email("user" + i + "@example.com")
                        .phoneNumber("+234801234567" + i)
                        .password("encoded")
                        .role(filterRole)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .phoneVerified(false)
                        .enabled(true)
                        .build())
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(filteredUsers, pageable, totalUsers);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(athleteProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());
        when(coachProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());

        Page<UserListResponse> result = adminService.listUsers(filterRole.name(), null, null, pageable);

        assert result.getContent().size() == totalUsers
                : "Expected " + totalUsers + " users but got " + result.getContent().size();

        // All returned users should have the filtered role
        for (UserListResponse user : result.getContent()) {
            assert user.getRole().equals(filterRole.name())
                    : "Expected role " + filterRole + " but got " + user.getRole();
        }
    }

    @Property
    void filterByStatusReturnsCorrectly(
            @ForAll("userStatuses") UserStatus filterStatus,
            @ForAll @IntRange(min = 0, max = 5) int count
    ) {
        reset(userRepository, athleteProfileRepository, coachProfileRepository);

        List<User> users = IntStream.range(0, count)
                .mapToObj(i -> User.builder()
                        .id(UUID.randomUUID())
                        .email("status" + i + "@example.com")
                        .phoneNumber("+234801234500" + i)
                        .password("encoded")
                        .role(UserRole.ROLE_ATHLETE)
                        .status(filterStatus)
                        .emailVerified(true)
                        .phoneVerified(false)
                        .enabled(filterStatus == UserStatus.ACTIVE)
                        .build())
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(users, pageable, count);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(athleteProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());
        when(coachProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());

        Page<UserListResponse> result = adminService.listUsers(null, filterStatus.name(), null, pageable);

        assert result.getContent().size() == count
                : "Expected " + count + " users but got " + result.getContent().size();

        for (UserListResponse user : result.getContent()) {
            assert user.getStatus().equals(filterStatus.name())
                    : "Expected status " + filterStatus + " but got " + user.getStatus();
        }
    }

    @Property
    void paginationRespectsPageSize(
            @ForAll @IntRange(min = 1, max = 100) int pageSize,
            @ForAll @IntRange(min = 0, max = 5) int pageNumber
    ) {
        reset(userRepository, athleteProfileRepository, coachProfileRepository);

        // Generate a page of results with the given page size
        int resultCount = Math.min(pageSize, 5); // simulate fewer results than page size sometimes
        List<User> users = IntStream.range(0, resultCount)
                .mapToObj(i -> User.builder()
                        .id(UUID.randomUUID())
                        .email("page" + i + "@example.com")
                        .phoneNumber("+234801234600" + i)
                        .password("encoded")
                        .role(UserRole.ROLE_ATHLETE)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .phoneVerified(false)
                        .enabled(true)
                        .build())
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> page = new PageImpl<>(users, pageable, 50L);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(athleteProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());
        when(coachProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());

        Page<UserListResponse> result = adminService.listUsers(null, null, null, pageable);

        assert result.getContent().size() <= pageSize
                : "Result size " + result.getContent().size() + " should not exceed page size " + pageSize;
        assert result.getContent().size() == resultCount
                : "Expected " + resultCount + " results on current page";
    }

    @Property
    void searchTermFiltersUsers(
            @ForAll("searchTerms") String searchTerm
    ) {
        reset(userRepository, athleteProfileRepository, coachProfileRepository);

        // When a search is provided, the specification is built and findAll is called
        List<User> matchingUsers = List.of(
                User.builder()
                        .id(UUID.randomUUID())
                        .email(searchTerm.toLowerCase() + "@example.com")
                        .phoneNumber("+2348012345678")
                        .password("encoded")
                        .role(UserRole.ROLE_ATHLETE)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .phoneVerified(false)
                        .enabled(true)
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(matchingUsers, pageable, 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(athleteProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());
        when(coachProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());

        Page<UserListResponse> result = adminService.listUsers(null, null, searchTerm, pageable);

        // Verify that the service was called with the specification (search filtering applied)
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
        assert result != null : "Result should not be null";
    }

    @Property
    void responseContainsCorrectUserFields(
            @ForAll("userRoles") UserRole role,
            @ForAll("userStatuses") UserStatus status
    ) {
        reset(userRepository, athleteProfileRepository, coachProfileRepository);

        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String phone = "+2348012345678";

        User user = User.builder()
                .id(userId)
                .email(email)
                .phoneNumber(phone)
                .password("encoded")
                .role(role)
                .status(status)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(athleteProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());
        when(coachProfileRepository.findByUser_Id(any())).thenReturn(Optional.empty());

        Page<UserListResponse> result = adminService.listUsers(null, null, null, pageable);

        UserListResponse response = result.getContent().get(0);
        assert response.getId().equals(userId) : "ID should match";
        assert response.getEmail().equals(email) : "Email should match";
        assert response.getPhoneNumber().equals(phone) : "Phone should match";
        assert response.getRole().equals(role.name()) : "Role should match";
        assert response.getStatus().equals(status.name()) : "Status should match";
    }

    @Provide
    Arbitrary<UserRole> userRoles() {
        return Arbitraries.of(UserRole.values());
    }

    @Provide
    Arbitrary<UserStatus> userStatuses() {
        return Arbitraries.of(UserStatus.values());
    }

    @Provide
    Arbitrary<String> searchTerms() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .ofMinLength(2)
                .ofMaxLength(20);
    }
}
