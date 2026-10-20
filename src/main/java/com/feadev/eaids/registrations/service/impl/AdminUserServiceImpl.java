package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.dto.request.CreateUserRequest;
import com.feadev.eaids.registrations.dto.request.UpdateUserRequest;
import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.dto.response.UserDetailResponse;
import com.feadev.eaids.registrations.dto.response.UserListResponse;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.PasswordResetToken;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.PasswordResetTokenRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AdminUserService;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.EmailService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    @Override
    public UserDetailResponse  createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already in use: " + request.getPhoneNumber());
        }

        UserRole role = UserRole.valueOf(request.getRole().toUpperCase());

        if (role == UserRole.ROLE_SCOUT
                || role == UserRole.ROLE_MEDICAL_OFFICER
                || role == UserRole.ROLE_PHYSIOTHERAPIST) {
            return createInvitedUser(request, role);
        }

        UserStatus status = request.getStatus() != null
                ? UserStatus.valueOf(request.getStatus().toUpperCase())
                : UserStatus.PENDING_VERIFICATION;

        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(status)
                .emailVerified(true)
                .phoneVerified(true)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Admin created user: userId={}, email={}, role={}", savedUser.getId(), savedUser.getEmail(), role);

        return mapToDetailResponse(savedUser);
    }

    private UserDetailResponse createScoutUser(CreateUserRequest request) {
        return createInvitedUser(request, UserRole.ROLE_SCOUT);
    }

    /**
     * Shared creation flow for admin-invited roles (Scout, Medical Officer, Physiotherapist).
     * Stores a random placeholder password and sends a password-setup email.
     */
    private UserDetailResponse createInvitedUser(CreateUserRequest request, UserRole role) {
        String placeholderPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(placeholderPassword)
                .role(role)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);

        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken setupToken = PasswordResetToken.builder()
                .user(savedUser)
                .token(tokenValue)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(72))
                .build();

        passwordResetTokenRepository.save(setupToken);
        emailService.sendPasswordSetupEmail(savedUser.getEmail(), tokenValue);

        AuditAction auditAction = role == UserRole.ROLE_SCOUT
                ? AuditAction.SCOUT_REGISTERED
                : AuditAction.REGISTRATION;
        auditService.log(auditAction, savedUser.getId(), savedUser.getEmail(), null, null);

        log.info("Admin created {} user: userId={}, email={}, status=PENDING_VERIFICATION",
                role.name(), savedUser.getId(), savedUser.getEmail());
        return mapToDetailResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserListResponse> getAllUsers(String role, String status, String search, Pageable pageable) {
        Specification<User> spec = buildSpecification(role, status, search);
        Page<User> usersPage = userRepository.findAll(spec, pageable);
        return usersPage.map(this::mapToListResponse);
    }

    @Override
    public UserDetailResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            if (!request.getPhoneNumber().equals(user.getPhoneNumber())
                    && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Phone number already in use: " + request.getPhoneNumber());
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getRole() != null) {
            user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        }

        if (request.getStatus() != null) {
            user.setStatus(UserStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User updatedUser = userRepository.save(user);
        log.info("Admin updated user: userId={}, email={}", updatedUser.getId(), updatedUser.getEmail());

        return mapToDetailResponse(updatedUser);
    }

    @Override
    public MessageResponse deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
        log.info("Admin deleted user: userId={}, email={}", user.getId(), user.getEmail());

        return MessageResponse.builder()
                .message("User deleted successfully")
                .build();
    }

    @Override
    public MessageResponse toggleUserStatus(UUID id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEnabled(enabled);
        userRepository.save(user);

        String action = enabled ? "enabled" : "disabled";
        log.info("Admin {} user: userId={}, email={}", action, user.getId(), user.getEmail());

        return MessageResponse.builder()
                .message("User " + action + " successfully")
                .build();
    }

    private Specification<User> buildSpecification(String role, String status, String search) {
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
                predicates.add(criteriaBuilder.or(emailMatch, phoneMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private UserDetailResponse mapToDetailResponse(User user) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .enabled(user.isEnabled())
                .createdDate(user.getCreatedDate())
                .lastModifiedDate(user.getLastModifiedDate())
                .createdBy(user.getCreatedBy())
                .lastModifiedBy(user.getLastModifiedBy())
                .build();
    }

    private UserListResponse mapToListResponse(User user) {
        return UserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }
}
