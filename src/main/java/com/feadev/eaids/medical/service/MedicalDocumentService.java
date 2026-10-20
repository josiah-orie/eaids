package com.feadev.eaids.medical.service;

import com.feadev.eaids.medical.dto.response.MedicalDocumentMetadataResponse;
import com.feadev.eaids.registrations.model.enums.UserRole;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MedicalDocumentService {

    MedicalDocumentMetadataResponse uploadDocument(UUID athleteUserId,
                                                    MultipartFile file,
                                                    String documentCategory,
                                                    String description,
                                                    UUID relatedAssessmentId,
                                                    UUID relatedInjuryId,
                                                    UUID relatedClearanceId,
                                                    UUID uploadedBy,
                                                    UserRole actorRole);

    MedicalDocumentMetadataResponse getMetadata(UUID documentId, UUID actorId, UserRole actorRole);

    /**
     * Returns the document as a Spring {@link Resource} for download streaming.
     * Performs authorisation check before resolving the stored path.
     * The stored path is NEVER returned to the caller — only the file bytes.
     */
    Resource downloadDocument(UUID documentId, UUID actorId, UserRole actorRole);

    void deleteDocument(UUID documentId, UUID actorId, UserRole actorRole);
}
