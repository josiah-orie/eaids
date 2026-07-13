package com.feadev.eaids.registrations.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for {@link PhoneNumber} annotation.
 * Validates that the phone number conforms to E.164 format:
 * leading '+', followed by a non-zero first digit, then 6-14 additional digits
 * (total 7-15 digits after the '+').
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,14}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // let @NotNull handle null check
        }

        return E164_PATTERN.matcher(value).matches();
    }
}
