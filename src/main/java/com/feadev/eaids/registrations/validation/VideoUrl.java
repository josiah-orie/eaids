package com.feadev.eaids.registrations.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint annotation that validates a video URL is well-formed
 * (http/https scheme, ≤2048 characters) and that the URL domain matches the
 * declared video type (HUDL → hudl.com, YOUTUBE → youtube.com/youtu.be,
 * GOOGLE_DRIVE → drive.google.com).
 *
 * The annotated class must have {@code url} (String) and {@code videoType}
 * (VideoType enum) properties accessible via standard getters.
 */
@Documented
@Constraint(validatedBy = VideoUrlValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VideoUrl {

    String message() default "Invalid video URL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
