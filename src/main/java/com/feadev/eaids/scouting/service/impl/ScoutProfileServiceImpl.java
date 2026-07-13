package com.feadev.eaids.scouting.service.impl;

import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.scouting.dto.request.CreateScoutProfileRequest;
import com.feadev.eaids.scouting.dto.request.GrantScopeRequest;
import com.feadev.eaids.scouting.dto.response.ScoutProfileResponse;
import com.feadev.eaids.scouting.dto.response.ScopeResponse;
import com.feadev.eaids.scouting.model.ScoutAccessScope;
import com.feadev.eaids.scouting.model.ScoutProfile;
import com.feadev.eaids.scouting.repository.ScoutAccessScopeRepository;
import com.feadev.eaids.scouting.repository.ScoutProfileRepository;
import com.feadev.eaids.scouting.service.ScoutProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoutProfileServiceImpl implements ScoutProfileService {

    private final ScoutProfileRepository scoutProfileRepository;
    private final ScoutAccessScopeRepository scopeRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;

    @Override
    @Transactional
    public ScoutProfileResponse createProfile(UUID userId, CreateScoutProfileRequest request) {
        if (scoutProfileRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Scout profile already exists for user: " + userId);
        }

        ScoutProfile profile = ScoutProfile.builder()
                .userId(userId)
                .fullName(request.getFullName())
                .organization(request.getOrganization())
                .organizationType(request.getOrganizationType())
                .country(request.getCountry())
                .region(request.getRegion())
                .verified(false)
                .build();

        ScoutProfile saved = scoutProfileRepository.save(profile);
        log.info("Scout profile created: userId={}", userId);
        return mapToResponse(saved, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public ScoutProfileResponse getProfile(UUID userId) {
        ScoutProfile profile = scoutProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Scout profile not found for user: " + userId));
        List<ScoutAccessScope> scopes = scopeRepository.findByScoutUserId(userId);
        return mapToResponse(profile, scopes);
    }

    @Override
    @Transactional
    public ScopeResponse grantScope(UUID scoutUserId, GrantScopeRequest request) {
        // Verify scout exists
        if (!scoutProfileRepository.existsByUserId(scoutUserId)) {
            throw new ResourceNotFoundException("Scout profile not found for user: " + scoutUserId);
        }

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        ScoutAccessScope scope = ScoutAccessScope.builder()
                .scoutUserId(scoutUserId)
                .scopeType(request.getScopeType())
                .scopeValue(request.getScopeValue())
                .grantedBy(currentUser.getId())
                .grantedDate(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .build();

        ScoutAccessScope saved = scopeRepository.save(scope);

        auditService.log(AuditAction.SCOUT_SCOPE_GRANTED, scoutUserId,
                currentUserEmail, auditInterceptor.getClientIpAddress(),
                auditInterceptor.getUserAgent());

        log.info("Scope granted: scoutUserId={}, type={}, value={}",
                scoutUserId, request.getScopeType(), request.getScopeValue());

        return mapScopeToResponse(saved);
    }

    @Override
    @Transactional
    public void revokeScope(UUID scopeId) {
        if (!scopeRepository.existsById(scopeId)) {
            throw new ResourceNotFoundException("Scope not found: " + scopeId);
        }

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        scopeRepository.deleteById(scopeId);

        auditService.log(AuditAction.SCOUT_SCOPE_REVOKED, null,
                currentUserEmail, auditInterceptor.getClientIpAddress(),
                auditInterceptor.getUserAgent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScopeResponse> getScopes(UUID scoutUserId) {
        return scopeRepository.findByScoutUserId(scoutUserId).stream()
                .map(this::mapScopeToResponse)
                .collect(Collectors.toList());
    }

    private ScoutProfileResponse mapToResponse(ScoutProfile profile, List<ScoutAccessScope> scopes) {
        return ScoutProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .organization(profile.getOrganization())
                .organizationType(profile.getOrganizationType())
                .country(profile.getCountry())
                .region(profile.getRegion())
                .verified(profile.isVerified())
                .scopes(scopes.stream().map(this::mapScopeToResponse).collect(Collectors.toList()))
                .createdDate(profile.getCreatedDate())
                .build();
    }

    private ScopeResponse mapScopeToResponse(ScoutAccessScope scope) {
        return ScopeResponse.builder()
                .id(scope.getId())
                .scopeType(scope.getScopeType())
                .scopeValue(scope.getScopeValue())
                .grantedDate(scope.getGrantedDate())
                .expiresAt(scope.getExpiresAt())
                .build();
    }
}
