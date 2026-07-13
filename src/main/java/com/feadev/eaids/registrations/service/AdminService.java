package com.feadev.eaids.registrations.service;

import com.feadev.eaids.registrations.dto.response.AuditLogResponse;
import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.dto.response.UserListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AdminService {

    Page<UserListResponse> listUsers(String role, String status, String search, Pageable pageable);

    MessageResponse approveAthlete(UUID athleteId);

    MessageResponse verifyCoach(UUID coachId);

    Page<AuditLogResponse> getAuditLogs(String action, UUID userId,
                                         LocalDateTime from, LocalDateTime to, Pageable pageable);
}
