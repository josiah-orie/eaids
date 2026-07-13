package com.feadev.eaids.registrations.repository;

import com.feadev.eaids.registrations.model.AuditLog;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:from IS NULL OR a.createdDate >= :from) AND " +
            "(:to IS NULL OR a.createdDate <= :to)")
    Page<AuditLog> findAllWithFilters(
            @Param("action") AuditAction action,
            @Param("userId") UUID userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
