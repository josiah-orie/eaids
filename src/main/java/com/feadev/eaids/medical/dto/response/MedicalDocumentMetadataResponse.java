package com.feadev.eaids.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Document metadata response — deliberately omits {@code storedPath}.
 * The stored path is an internal implementation detail and must never be
 * exposed through the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDocumentMetadataResponse {

    private UUID id;
    private UUID athleteUserId;
    private UUID relatedAssessmentId;
    private UUID relatedInjuryId;
    private UUID relatedClearanceId;
    private String documentCategory;
    private String originalFilename;
    private String contentType;
    private Long fileSizeBytes;
    private String description;
    private UUID uploadedByUserId;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
