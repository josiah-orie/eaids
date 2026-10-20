package com.feadev.eaids.medical.controller;

import com.feadev.eaids.medical.dto.response.MedicalDocumentMetadataResponse;
import com.feadev.eaids.medical.service.MedicalDocumentService;
import com.feadev.eaids.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Medical Documents",
     description = "Secure upload and download of medical reports, certificates, and documents")
public class MedicalDocumentController {

    private final MedicalDocumentService documentService;

    @PostMapping(value = "/api/v1/athletes/{athleteId}/medical-documents",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a medical document for an athlete",
               description = "Accepted types: PDF, JPEG, PNG, WebP. Maximum size: 10 MB. " +
                             "The stored file path is never returned in the response.")
    public ResponseEntity<MedicalDocumentMetadataResponse> uploadDocument(
            @PathVariable UUID athleteId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentCategory") String documentCategory,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "relatedAssessmentId", required = false) UUID relatedAssessmentId,
            @RequestParam(value = "relatedInjuryId", required = false) UUID relatedInjuryId,
            @RequestParam(value = "relatedClearanceId", required = false) UUID relatedClearanceId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                documentService.uploadDocument(athleteId, file, documentCategory, description,
                        relatedAssessmentId, relatedInjuryId, relatedClearanceId,
                        actor.getId(), actor.getRole()));
    }

    @GetMapping("/api/v1/medical-documents/{documentId}/metadata")
    @Operation(summary = "Get document metadata",
               description = "Returns document details. The file storage path is never exposed.")
    public ResponseEntity<MedicalDocumentMetadataResponse> getMetadata(
            @PathVariable UUID documentId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                documentService.getMetadata(documentId, actor.getId(), actor.getRole()));
    }

    @GetMapping("/api/v1/medical-documents/{documentId}/download")
    @Operation(summary = "Download a medical document",
               description = "Streams the file content after authorisation check. " +
                             "Does not return a URL or file path — streams bytes directly.")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable UUID documentId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();

        // Fetch metadata first to get content type and filename for headers
        MedicalDocumentMetadataResponse metadata =
                documentService.getMetadata(documentId, actor.getId(), actor.getRole());

        Resource resource = documentService.downloadDocument(documentId,
                actor.getId(), actor.getRole());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(metadata.getOriginalFilename())
                                .build()
                                .toString())
                .body(resource);
    }

    @DeleteMapping("/api/v1/medical-documents/{documentId}")
    @PreAuthorize("hasAnyRole('MEDICAL_OFFICER','ADMIN')")
    @Operation(summary = "Delete a medical document",
               description = "Only medical officers and admins may delete documents.")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID documentId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        documentService.deleteDocument(documentId, actor.getId(), actor.getRole());
        return ResponseEntity.noContent().build();
    }
}
