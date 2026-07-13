package com.feadev.eaids.validation;

import com.feadev.eaids.registrations.model.enums.VideoType;
import com.feadev.eaids.registrations.validation.VideoUrl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.jqwik.api.*;

import java.util.Set;

/**
 * Property-based tests for video URL domain validation.
 * Verifies that VideoUrlValidator correctly accepts/rejects URLs based on VideoType.
 *
 * Validates: Requirements 17.1, 17.2, 17.3, 17.4, 17.5, 17.6
 */
class VideoUrlValidationPropertyTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Test wrapper mimicking VideoAddRequest structure
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @VideoUrl
    static class VideoUrlHolder {
        private String url;
        private VideoType videoType;
    }

    @Property
    void validHudlUrlsShouldBeAccepted(@ForAll("validHudlUrls") String url) {
        VideoUrlHolder holder = new VideoUrlHolder(url, VideoType.HUDL);
        Set<ConstraintViolation<VideoUrlHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected valid HUDL URL but got violations for: " + url + " -> " + violations);
        }
    }

    @Property
    void validYoutubeUrlsShouldBeAccepted(@ForAll("validYoutubeUrls") String url) {
        VideoUrlHolder holder = new VideoUrlHolder(url, VideoType.YOUTUBE);
        Set<ConstraintViolation<VideoUrlHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected valid YOUTUBE URL but got violations for: " + url + " -> " + violations);
        }
    }

    @Property
    void validGoogleDriveUrlsShouldBeAccepted(@ForAll("validGoogleDriveUrls") String url) {
        VideoUrlHolder holder = new VideoUrlHolder(url, VideoType.GOOGLE_DRIVE);
        Set<ConstraintViolation<VideoUrlHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected valid GOOGLE_DRIVE URL but got violations for: " + url + " -> " + violations);
        }
    }

    @Property
    void mismatchedDomainShouldBeRejected(@ForAll("mismatchedDomainUrls") VideoUrlHolder holder) {
        Set<ConstraintViolation<VideoUrlHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for mismatched domain: url=" + holder.getUrl() + ", type=" + holder.getVideoType());
        }
    }

    @Property
    void urlsWithInvalidSchemeShouldBeRejected(@ForAll("invalidSchemeUrls") VideoUrlHolder holder) {
        Set<ConstraintViolation<VideoUrlHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for invalid scheme: " + holder.getUrl());
        }
    }

    @Property
    void urlsExceedingMaxLengthShouldBeRejected(@ForAll("tooLongUrls") VideoUrlHolder holder) {
        Set<ConstraintViolation<VideoUrlHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for URL exceeding 2048 chars");
        }
    }

    @Provide
    Arbitrary<String> validHudlUrls() {
        Arbitrary<String> path = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(30);

        return Combinators.combine(
                Arbitraries.of("https://www.hudl.com/", "https://hudl.com/", "http://hudl.com/"),
                path
        ).as((base, p) -> base + p);
    }

    @Provide
    Arbitrary<String> validYoutubeUrls() {
        Arbitrary<String> videoId = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(8)
                .ofMaxLength(15);

        Arbitrary<String> base = Arbitraries.of(
                "https://www.youtube.com/watch?v=",
                "https://youtube.com/watch?v=",
                "https://youtu.be/"
        );

        return Combinators.combine(base, videoId).as((b, id) -> b + id);
    }

    @Provide
    Arbitrary<String> validGoogleDriveUrls() {
        Arbitrary<String> fileId = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(10)
                .ofMaxLength(30);

        return fileId.map(id -> "https://drive.google.com/file/d/" + id + "/view");
    }

    @Provide
    Arbitrary<VideoUrlHolder> mismatchedDomainUrls() {
        // HUDL type with YouTube URL
        Arbitrary<VideoUrlHolder> hudlWithYoutube = Arbitraries.of(
                new VideoUrlHolder("https://www.youtube.com/watch?v=abc123", VideoType.HUDL),
                new VideoUrlHolder("https://drive.google.com/file/d/abc/view", VideoType.HUDL)
        );
        // YOUTUBE type with Hudl URL
        Arbitrary<VideoUrlHolder> youtubeWithHudl = Arbitraries.of(
                new VideoUrlHolder("https://www.hudl.com/video/abc123", VideoType.YOUTUBE),
                new VideoUrlHolder("https://drive.google.com/file/d/abc/view", VideoType.YOUTUBE)
        );
        // GOOGLE_DRIVE type with YouTube URL
        Arbitrary<VideoUrlHolder> driveWithYoutube = Arbitraries.of(
                new VideoUrlHolder("https://www.youtube.com/watch?v=abc123", VideoType.GOOGLE_DRIVE),
                new VideoUrlHolder("https://www.hudl.com/video/abc123", VideoType.GOOGLE_DRIVE)
        );

        return Arbitraries.oneOf(hudlWithYoutube, youtubeWithHudl, driveWithYoutube);
    }

    @Provide
    Arbitrary<VideoUrlHolder> invalidSchemeUrls() {
        Arbitrary<VideoType> videoType = Arbitraries.of(VideoType.values());
        Arbitrary<String> invalidUrl = Arbitraries.of(
                "ftp://www.hudl.com/video/123",
                "file:///etc/passwd",
                "javascript:alert(1)",
                "data:text/html,hello"
        );

        return Combinators.combine(invalidUrl, videoType)
                .as((url, type) -> new VideoUrlHolder(url, type));
    }

    @Provide
    Arbitrary<VideoUrlHolder> tooLongUrls() {
        // Generate URLs longer than 2048 characters
        Arbitrary<String> longPath = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2040)
                .ofMaxLength(2100);
        Arbitrary<VideoType> videoType = Arbitraries.of(VideoType.values());

        return Combinators.combine(longPath, videoType)
                .as((path, type) -> new VideoUrlHolder("https://www.hudl.com/" + path, type));
    }
}
