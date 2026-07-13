package com.feadev.eaids.service;

import com.feadev.eaids.registrations.exception.MaxLimitReachedException;
import com.feadev.eaids.registrations.model.CoachProfile;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.DocumentType;
import com.feadev.eaids.registrations.repository.CoachDocumentRepository;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.StorageService;
import com.feadev.eaids.registrations.service.impl.CoachServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for document upload constraints.
 *
 * Property 15: Document Upload Constraints
 * Validates: Requirements 7.1, 7.2, 7.3, 7.6, 7.7
 *
 * Verifies file type, size, document type, and count constraints.
 */
class DocumentUploadConstraintsPropertyTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CoachProfileRepository coachProfileRepository = mock(CoachProfileRepository.class);
    private final CoachDocumentRepository coachDocumentRepository = mock(CoachDocumentRepository.class);
    private final StorageService storageService = mock(StorageService.class);
    private final AuditService auditService = mock(AuditService.class);

    private final CoachServiceImpl coachService = new CoachServiceImpl(
            userRepository,
            coachProfileRepository,
            coachDocumentRepository,
            storageService,
            auditService
    );

    @Property
    void validFileTypeAndSizeAccepted(
            @ForAll("allowedContentTypes") String contentType,
            @ForAll @LongRange(min = 1, max = 10_485_760) long fileSize,
            @ForAll("documentTypes") DocumentType docType
    ) {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        reset(coachProfileRepository, coachDocumentRepository, storageService);

        CoachProfile profile = CoachProfile.builder()
                .id(profileId)
                .user(User.builder().id(userId).build())
                .documents(new ArrayList<>())
                .build();

        when(coachProfileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(coachDocumentRepository.countByCoachProfileId(profileId)).thenReturn(0);
        when(storageService.store(any(), any())).thenReturn("/storage/file.pdf");
        when(coachDocumentRepository.save(any())).thenAnswer(i -> {
            var doc = i.getArgument(0);
            return doc;
        });

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(fileSize);
        when(mockFile.getOriginalFilename()).thenReturn("test-document.pdf");

        // Should not throw any exception
        var response = coachService.uploadDocument(userId, mockFile, docType);
        assert response != null : "Expected a non-null response for valid document upload";
    }

    @Property
    void invalidFileTypeRejected(
            @ForAll("disallowedContentTypes") String contentType,
            @ForAll("documentTypes") DocumentType docType
    ) {
        UUID userId = UUID.randomUUID();

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("test.exe");

        try {
            coachService.uploadDocument(userId, mockFile, docType);
            throw new AssertionError("Expected IllegalArgumentException for content type: " + contentType);
        } catch (IllegalArgumentException e) {
            assert e.getMessage().toLowerCase().contains("file type")
                    || e.getMessage().toLowerCase().contains("allowed")
                    : "Expected message about invalid file type but got: " + e.getMessage();
        }
    }

    @Property
    void fileSizeExceeding10MBRejected(
            @ForAll @LongRange(min = 10_485_761, max = 100_000_000) long fileSize,
            @ForAll("allowedContentTypes") String contentType,
            @ForAll("documentTypes") DocumentType docType
    ) {
        UUID userId = UUID.randomUUID();

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(fileSize);
        when(mockFile.getOriginalFilename()).thenReturn("large-file.pdf");

        try {
            coachService.uploadDocument(userId, mockFile, docType);
            throw new AssertionError("Expected IllegalArgumentException for file size: " + fileSize);
        } catch (IllegalArgumentException e) {
            assert e.getMessage().toLowerCase().contains("size")
                    || e.getMessage().toLowerCase().contains("10mb")
                    || e.getMessage().contains("10")
                    : "Expected message about file size but got: " + e.getMessage();
        }
    }

    @Property
    void documentCountAtLimitRejected(
            @ForAll("allowedContentTypes") String contentType,
            @ForAll("documentTypes") DocumentType docType
    ) {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        reset(coachProfileRepository, coachDocumentRepository);

        CoachProfile profile = CoachProfile.builder()
                .id(profileId)
                .user(User.builder().id(userId).build())
                .documents(new ArrayList<>())
                .build();

        when(coachProfileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(coachDocumentRepository.countByCoachProfileId(profileId)).thenReturn(20);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("doc.pdf");

        try {
            coachService.uploadDocument(userId, mockFile, docType);
            throw new AssertionError("Expected MaxLimitReachedException when document count is 20");
        } catch (MaxLimitReachedException e) {
            assert e.getMessage().contains("20")
                    : "Expected message to mention limit of 20 but got: " + e.getMessage();
        }
    }

    @Property
    void documentCountBelowLimitAccepted(
            @ForAll @IntRange(min = 0, max = 19) int currentCount,
            @ForAll("allowedContentTypes") String contentType,
            @ForAll("documentTypes") DocumentType docType
    ) {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        reset(coachProfileRepository, coachDocumentRepository, storageService);

        CoachProfile profile = CoachProfile.builder()
                .id(profileId)
                .user(User.builder().id(userId).build())
                .documents(new ArrayList<>())
                .build();

        when(coachProfileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(coachDocumentRepository.countByCoachProfileId(profileId)).thenReturn(currentCount);
        when(storageService.store(any(), any())).thenReturn("/storage/file.pdf");
        when(coachDocumentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn(contentType);
        when(mockFile.getSize()).thenReturn(5000L);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");

        var response = coachService.uploadDocument(userId, mockFile, docType);
        assert response != null : "Expected successful upload when count is " + currentCount;
    }

    @Provide
    Arbitrary<String> allowedContentTypes() {
        return Arbitraries.of("application/pdf", "image/jpeg", "image/png");
    }

    @Provide
    Arbitrary<String> disallowedContentTypes() {
        return Arbitraries.of(
                "application/octet-stream",
                "text/plain",
                "application/zip",
                "image/gif",
                "application/msword",
                "video/mp4"
        );
    }

    @Provide
    Arbitrary<DocumentType> documentTypes() {
        return Arbitraries.of(DocumentType.values());
    }
}
