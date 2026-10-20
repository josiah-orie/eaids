package com.feadev.eaids.medical.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MedicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Column(name = "related_assessment_id", columnDefinition = "BINARY(16)")
    private UUID relatedAssessmentId;

    @Column(name = "related_injury_id", columnDefinition = "BINARY(16)")
    private UUID relatedInjuryId;

    @Column(name = "related_clearance_id", columnDefinition = "BINARY(16)")
    private UUID relatedClearanceId;

    @Column(name = "document_category", nullable = false, length = 50)
    private String documentCategory;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * Relative path from the storage base — used internally for download streaming.
     * MUST NEVER be included in any response DTO.
     */
    @Column(name = "stored_path", nullable = false, length = 512)
    private String storedPath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "uploaded_by", nullable = false, columnDefinition = "BINARY(16)")
    private UUID uploadedBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}
