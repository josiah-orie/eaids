package com.feadev.eaids.adminprofile.service.impl;

import com.feadev.eaids.adminprofile.dto.request.CreateAdminProfileRequest;
import com.feadev.eaids.adminprofile.dto.request.UpdateAdminProfileRequest;
import com.feadev.eaids.adminprofile.dto.response.AdminProfileResponse;
import com.feadev.eaids.adminprofile.model.AdminProfile;
import com.feadev.eaids.adminprofile.repository.AdminProfileRepository;
import com.feadev.eaids.adminprofile.service.AdminProfileService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminProfileServiceImpl implements AdminProfileService {

    private final AdminProfileRepository adminProfileRepository;
    private final UserRepository userRepository;

    @Override
    public AdminProfileResponse createProfile(UUID userId, CreateAdminProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalStateException("Only users with ADMIN role can have admin profiles");
        }

        if (adminProfileRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Admin profile already exists for user: " + userId);
        }

        AdminProfile profile = AdminProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .profileImageUrl(request.getProfileImageUrl())
                .jobTitle(request.getJobTitle())
                .department(request.getDepartment())
                .region(request.getRegion())
                .countryManaged(request.getCountryManaged())
                .phoneExtension(request.getPhoneExtension())
                .permissionsScope(request.getPermissionsScope())
                .build();

        AdminProfile saved = adminProfileRepository.save(profile);
        log.info("Admin profile created for user: {}", userId);
        return mapToResponse(saved);
    }

    @Override
    public AdminProfileResponse updateProfile(UUID userId, UpdateAdminProfileRequest request) {
        AdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found for user: " + userId));

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getJobTitle() != null) {
            profile.setJobTitle(request.getJobTitle());
        }
        if (request.getDepartment() != null) {
            profile.setDepartment(request.getDepartment());
        }
        if (request.getRegion() != null) {
            profile.setRegion(request.getRegion());
        }
        if (request.getCountryManaged() != null) {
            profile.setCountryManaged(request.getCountryManaged());
        }
        if (request.getPhoneExtension() != null) {
            profile.setPhoneExtension(request.getPhoneExtension());
        }
        if (request.getPermissionsScope() != null) {
            profile.setPermissionsScope(request.getPermissionsScope());
        }

        AdminProfile saved = adminProfileRepository.save(profile);
        log.info("Admin profile updated for user: {}", userId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponse getOwnProfile(UUID userId) {
        AdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found for user: " + userId));
        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponse getProfileById(UUID profileId) {
        AdminProfile profile = adminProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found with id: " + profileId));
        return mapToResponse(profile);
    }

    private AdminProfileResponse mapToResponse(AdminProfile profile) {
        return AdminProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .email(profile.getUser().getEmail())
                .fullName(profile.getFullName())
                .profileImageUrl(profile.getProfileImageUrl())
                .jobTitle(profile.getJobTitle())
                .department(profile.getDepartment())
                .region(profile.getRegion())
                .countryManaged(profile.getCountryManaged())
                .phoneExtension(profile.getPhoneExtension())
                .permissionsScope(profile.getPermissionsScope().name())
                .createdDate(profile.getCreatedDate())
                .lastModifiedDate(profile.getLastModifiedDate())
                .build();
    }
}
