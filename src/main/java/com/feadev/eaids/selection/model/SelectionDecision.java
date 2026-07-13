package com.feadev.eaids.selection.model;

import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.selection.model.enums.ApprovalStatus;
import com.feadev.eaids.selection.model.enums.SelectionTier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "selection_decisions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_selection_enrollment", columnNames = {"enrollment_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SelectionDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CampEnrollment enrollment;

    @Column(name = "camp_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID campId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommended_tier", nullable = false)
    private SelectionTier recommendedTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_tier")
    private SelectionTier finalTier;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "reviewed_by", columnDefinition = "BINARY(16)")
    private UUID reviewedBy;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
