package com.feadev.eaids.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Pattern;
import net.jqwik.api.*;

import java.util.Set;

/**
 * Property-based tests for password complexity validation.
 * Password must contain: 8-128 chars, at least one uppercase, one lowercase, one digit, one special character.
 *
 * Validates: Requirements 2.3, 9.7
 */
class PasswordComplexityPropertyTest {

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,128}$";
    private static final java.util.regex.Pattern PASSWORD_PATTERN = java.util.regex.Pattern.compile(PASSWORD_REGEX);

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Wrapper to test @Pattern annotation with the same regex used in the DTO
    private record PasswordHolder(
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,128}$",
                    message = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character"
            )
            String password
    ) {}

    @Property
    void validPasswordsShouldPassValidation(@ForAll("validPasswords") String password) {
        PasswordHolder holder = new PasswordHolder(password);
        Set<ConstraintViolation<PasswordHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected valid password but got violations for: " + password + " -> " + violations);
        }
    }

    @Property
    void passwordsWithoutUppercaseShouldFail(@ForAll("passwordsWithoutUppercase") String password) {
        PasswordHolder holder = new PasswordHolder(password);
        Set<ConstraintViolation<PasswordHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for password without uppercase: " + password);
        }
    }

    @Property
    void passwordsWithoutLowercaseShouldFail(@ForAll("passwordsWithoutLowercase") String password) {
        PasswordHolder holder = new PasswordHolder(password);
        Set<ConstraintViolation<PasswordHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for password without lowercase: " + password);
        }
    }

    @Property
    void passwordsWithoutDigitShouldFail(@ForAll("passwordsWithoutDigit") String password) {
        PasswordHolder holder = new PasswordHolder(password);
        Set<ConstraintViolation<PasswordHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for password without digit: " + password);
        }
    }

    @Property
    void passwordsWithoutSpecialCharShouldFail(@ForAll("passwordsWithoutSpecialChar") String password) {
        PasswordHolder holder = new PasswordHolder(password);
        Set<ConstraintViolation<PasswordHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for password without special character: " + password);
        }
    }

    @Property
    void passwordsTooShortShouldFail(@ForAll("tooShortPasswords") String password) {
        PasswordHolder holder = new PasswordHolder(password);
        Set<ConstraintViolation<PasswordHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for too-short password: " + password);
        }
    }

    @Provide
    Arbitrary<String> validPasswords() {
        // Build passwords that always contain at least one of each required character type
        Arbitrary<String> lowerPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(30);
        Arbitrary<String> upperPart = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(1)
                .ofMaxLength(30);
        Arbitrary<String> digitPart = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(10);
        Arbitrary<Character> specialChar = Arbitraries.of('@', '$', '!', '%', '*', '?', '&');

        return Combinators.combine(lowerPart, upperPart, digitPart, specialChar)
                .as((lower, upper, digit, special) -> lower + upper + digit + special)
                .filter(p -> p.length() >= 8 && p.length() <= 128)
                .filter(p -> PASSWORD_PATTERN.matcher(p).matches());
    }

    @Provide
    Arbitrary<String> passwordsWithoutUppercase() {
        // Only lowercase, digits, and special chars
        Arbitrary<String> lowerPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(4)
                .ofMaxLength(20);
        Arbitrary<String> digitPart = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(5);
        Arbitrary<Character> specialChar = Arbitraries.of('@', '$', '!', '%', '*', '?', '&');

        return Combinators.combine(lowerPart, digitPart, specialChar)
                .as((lower, digit, special) -> lower + digit + special)
                .filter(p -> p.length() >= 8);
    }

    @Provide
    Arbitrary<String> passwordsWithoutLowercase() {
        // Only uppercase, digits, and special chars
        Arbitrary<String> upperPart = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(4)
                .ofMaxLength(20);
        Arbitrary<String> digitPart = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(5);
        Arbitrary<Character> specialChar = Arbitraries.of('@', '$', '!', '%', '*', '?', '&');

        return Combinators.combine(upperPart, digitPart, specialChar)
                .as((upper, digit, special) -> upper + digit + special)
                .filter(p -> p.length() >= 8);
    }

    @Provide
    Arbitrary<String> passwordsWithoutDigit() {
        // Only uppercase, lowercase, and special chars
        Arbitrary<String> lowerPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(20);
        Arbitrary<String> upperPart = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(1)
                .ofMaxLength(10);
        Arbitrary<Character> specialChar = Arbitraries.of('@', '$', '!', '%', '*', '?', '&');

        return Combinators.combine(lowerPart, upperPart, specialChar)
                .as((lower, upper, special) -> lower + upper + special)
                .filter(p -> p.length() >= 8);
    }

    @Provide
    Arbitrary<String> passwordsWithoutSpecialChar() {
        // Only uppercase, lowercase, and digits
        Arbitrary<String> lowerPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(20);
        Arbitrary<String> upperPart = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(1)
                .ofMaxLength(10);
        Arbitrary<String> digitPart = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(5);

        return Combinators.combine(lowerPart, upperPart, digitPart)
                .as((lower, upper, digit) -> lower + upper + digit)
                .filter(p -> p.length() >= 8);
    }

    @Provide
    Arbitrary<String> tooShortPasswords() {
        // Valid character types but less than 8 chars
        return Arbitraries.of(
                "aA1@", "aA1@b", "aA1@bc", "Ab1@xyz"
        );
    }
}
