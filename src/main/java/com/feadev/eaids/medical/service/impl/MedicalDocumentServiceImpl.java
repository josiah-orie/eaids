package com.feadev.eaids.medical.service.impl;

import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.medical.dto.response.MedicalDocumentMetadataResponse;
import com.feadev.eaids.medical.model.MedicalDocument;
import com.feadev.eaids.medical.repository.MedicalDocumentRepository;
import com.feadev.eaids.medical.service.MedicalDocumentService;
import com.feadev.eaids.registrations.exception.MedicalAccessException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicalDocumentServiceImpl implements MedicalDocumentService {

    private final MedicalDocumentRepository documentRepository;
    private final StorageService storageService;
    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;

    @Value("${storage.local.base-path:./storage}")
    private String storageBasePath;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Override
    public MedicalDocumentMetadataResponse uploadDocument(UUID athleteUserId,
                                                           MultipartFile file,
                                                           String documentCategory,
                                                           String description,
                                                           UUID relatedAssessmentId,
                                                           UUID relatedInjuryId,
                                                           UUID relatedClearanceId,
                                                           UUID uploadedBy,
                                                           UserRole actorRole) {
        enforceUploadAccess(athleteUserId, uploadedBy, actorRole);

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "File type not allowed. Accepted types: PDF, JPEG, PNG, WebP");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the 10 MB limit");
        }

        String folder = "medical/athletes/" + athleteUserId;
        String storedPath = storageService.store(file, folder);

        MedicalDocument document = MedicalDocument.builder()
                .athleteUserId(athleteUserId)
                .relatedAssessmentId(relatedAssessmentId)
                .relatedInjuryId(relatedInjuryId)
                .relatedClearanceId(relatedClearanceId)
                .documentCategory(documentCategory)
                .originalFilename(file.getOriginalFilename() != null
                        ? file.getOriginalFilename() : "document")
                .storedPath(storedPath)
                .contentType(contentType)
                .fileSizeBytes(file.getSize())
                .description(description)
                .uploadedBy(uploadedBy)
                .build();

        MedicalDocument saved = documentRepository.save(document);

        auditService.log(AuditAction.MEDICAL_DOCUMENT_UPLOADED, uploadedBy, uploadedBy.toString(),
                auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());

        log.info("Medical document uploaded: id={} athlete={} by={}", saved.getId(),
                athleteUserId, uploadedBy);
        return mapToMetadata(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalDocumentMetadataResponse getMetadata(UUID documentId,
                                                        UUID actorId,
                                                        UserRole actorRole) {
        MedicalDocument doc = findOrThrow(documentId);
        enforceReadAccess(doc.getAthleteUserId(), actorId, actorRole);
        return mapToMetadata(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(UUID documentId, UUID actorId, UserRole actorRole) {
        MedicalDocument doc = findOrThrow(documentId);
        enforceReadAccess(doc.getAthleteUserId(), actorId, actorRole);

        try {
            Path basePath = Paths.get(storageBasePath).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(doc.getStoredPath()).normalize();

            // Security: prevent path traversal
            if (!filePath.startsWith(basePath)) {
                throw new MedicalAccessException("Invalid document path");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException(
                        "Document file not found on disk: " + documentId);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Document file could not be resolved: " + documentId);
        }
    }

    @Override
    public void deleteDocument(UUID documentId, UUID actorId, UserRole actorRole) {
        if (actorRole != UserRole.ROLE_MEDICAL_OFFICER && actorRole != UserRole.ROLE_ADMIN) {
            throw new MedicalAccessException(
                    "Only medical officers and admins may delete medical documents");
        }

        MedicalDocument doc = findOrThrow(documentId);
        storageService.delete(doc.getStoredPath());
        documentRepository.delete(doc);
        log.info("Medical document deleted: id={} by={}", documentId, actorId);
    }

    // -----------------------------------------------------------------------
    // Access control
    // -----------------------------------------------------------------------

    private void enforceUploadAccess(UUID athleteUserId, UUID actorId, UserRole actorRole) {
        if (actorRole == UserRole.ROLE_ATHLETE && !athleteUserId.equals(actorId)) {
            throw new MedicalAccessException("Athletes may only upload documents for themselves");
        }
        if (actorRole == UserRole.ROLE_COACH || actorRole == UserRole.ROLE_SCOUT) {
            throw new MedicalAccessException(
                    "Coaches and scouts may not upload medical documents");
        }
    }

    private void enforceReadAccess(UUID athleteUserId, UUID actorId, UserRole actorRole) {
        if (actorRole == UserRole.ROLE_ATHLETE && !athleteUserId.equals(actorId)) {
            throw new MedicalAccessException(
                    "Athletes may only access their own medical documents");
        }
        if (actorRole == UserRole.ROLE_COACH || actorRole == UserRole.ROLE_SCOUT) {
            throw new MedicalAccessException(
                    "Coaches and scouts do not have access to medical documents");
        }
    }

    // -----------------------------------------------------------------------
    // Mapping
    // -----------------------------------------------------------------------

    private MedicalDocument findOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical document not found with id: " + id));
    }

    /** storedPath is deliberately excluded from this response. */
    private MedicalDocumentMetadataResponse mapToMetadata(MedicalDocument d) {
        return MedicalDocumentMetadataResponse.builder()
                .id(d.getId())
                .athleteUserId(d.getAthleteUserId())
                .relatedAssessmentId(d.getRelatedAssessmentId())
                .relatedInjuryId(d.getRelatedInjuryId())
                .relatedClearanceId(d.getRelatedClearanceId())
                .documentCategory(d.getDocumentCategory())
                .originalFilename(d.getOriginalFilename())
                .contentType(d.getContentType())
                .fileSizeBytes(d.getFileSizeBytes())
                .description(d.getDescription())
                .uploadedByUserId(d.getUploadedBy())
                .createdDate(d.getCreatedDate())
                .lastModifiedDate(d.getLastModifiedDate())
                .build();
    }
}
