package com.feadev.eaids.registrations.service;

import com.feadev.eaids.registrations.dto.request.CreateUserRequest;
import com.feadev.eaids.registrations.dto.request.UpdateUserRequest;
import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.dto.response.UserDetailResponse;
import com.feadev.eaids.registrations.dto.response.UserListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {

    UserDetailResponse createUser(CreateUserRequest request);

    UserDetailResponse getUserById(UUID id);

    Page<UserListResponse> getAllUsers(String role, String status, String search, Pageable pageable);

    UserDetailResponse updateUser(UUID id, UpdateUserRequest request);

    MessageResponse deleteUser(UUID id);

    MessageResponse toggleUserStatus(UUID id, boolean enabled);
}
