package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.model.AuditLog;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.repository.AuditLogRepository;
import com.feadev.eaids.registrations.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    public void log(AuditAction action, UUID userId, String actorIdentifier,
                    String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .userId(userId)
                    .actorIdentifier(actorIdentifier)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: action={}, actor={}", action, actorIdentifier);
        } catch (Exception e) {
            log.error("Failed to persist audit log entry: action={}, actor={}, error={}",
                    action, actorIdentifier, e.getMessage(), e);
        }
    }

    @Override
    public Page<AuditLog> findAll(AuditAction action, UUID userId,
                                  LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.findAllWithFilters(action, userId, from, to, pageable);
    }
}
