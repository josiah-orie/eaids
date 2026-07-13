package com.feadev.eaids.scouting.model;

import com.feadev.eaids.scouting.model.enums.ScopeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scout_access_scopes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoutAccessScope {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "scout_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID scoutUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private ScopeType scopeType;

    @Column(name = "scope_value", nullable = false, length = 100)
    private String scopeValue;

    @Column(name = "granted_by", columnDefinition = "BINARY(16)")
    private UUID grantedBy;

    @Column(name = "granted_date", nullable = false)
    private LocalDateTime grantedDate;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
