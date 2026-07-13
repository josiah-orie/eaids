package com.feadev.eaids.registrations.validation;

import com.feadev.eaids.registrations.model.enums.VideoType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.net.URI;

/**
 * Validator for {@link VideoUrl} annotation.
 * Uses BeanWrapper to access "url" and "videoType" fields from the annotated class.
 *
 * <p>Validation rules:
 * <ol>
 *   <li>If url is null/blank, return true (let @NotNull handle that)</li>
 *   <li>URL must start with http:// or https://</li>
 *   <li>URL length must be ≤ 2048 characters</li>
 *   <li>Domain must match the declared VideoType:
 *       HUDL → hudl.com, YOUTUBE → youtube.com or youtu.be,
 *       GOOGLE_DRIVE → drive.google.com</li>
 * </ol>
 */
public class VideoUrlValidator implements ConstraintValidator<VideoUrl, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(value);

        Object urlObj = beanWrapper.getPropertyValue("url");
        Object videoTypeObj = beanWrapper.getPropertyValue("videoType");

        // If url is null or blank, let @NotNull/@NotBlank handle it
        if (urlObj == null) {
            return true;
        }

        String url = urlObj.toString();
        if (url.isBlank()) {
            return true;
        }

        // If videoType is null, we can't validate domain matching; let @NotNull handle it
        if (videoTypeObj == null) {
            return true;
        }

        VideoType videoType = (VideoType) videoTypeObj;

        // Disable default constraint violation so we can add custom messages
        context.disableDefaultConstraintViolation();

        // Validate URL length
        if (url.length() > 2048) {
            context.buildConstraintViolationWithTemplate("URL must not exceed 2048 characters")
                    .addPropertyNode("url")
                    .addConstraintViolation();
            return false;
        }

        // Validate URL scheme (http or https)
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            context.buildConstraintViolationWithTemplate("URL is not a valid URL")
                    .addPropertyNode("url")
                    .addConstraintViolation();
            return false;
        }

        // Parse URI and extract host
        String host;
        try {
            URI uri = new URI(url);
            host = uri.getHost();
        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("URL is not a valid URL")
                    .addPropertyNode("url")
                    .addConstraintViolation();
            return false;
        }

        if (host == null || host.isBlank()) {
            context.buildConstraintViolationWithTemplate("URL is not a valid URL")
                    .addPropertyNode("url")
                    .addConstraintViolation();
            return false;
        }

        // Validate domain matches declared video type
        String lowerHost = host.toLowerCase();
        boolean domainValid = switch (videoType) {
            case HUDL -> lowerHost.contains("hudl.com");
            case YOUTUBE -> lowerHost.contains("youtube.com") || lowerHost.contains("youtu.be");
            case GOOGLE_DRIVE -> lowerHost.contains("drive.google.com");
        };

        if (!domainValid) {
            context.buildConstraintViolationWithTemplate(
                            "URL does not match the expected platform for video type " + videoType)
                    .addPropertyNode("url")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
