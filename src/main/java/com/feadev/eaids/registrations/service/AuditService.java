package com.feadev.eaids.registrations.service;

import com.feadev.eaids.registrations.model.AuditLog;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditService {

    void log(AuditAction action, UUID userId, String actorIdentifier,
             String ipAddress, String userAgent);

    Page<AuditLog> findAll(AuditAction action, UUID userId,
                           LocalDateTime from, LocalDateTime to, Pageable pageable);
}
