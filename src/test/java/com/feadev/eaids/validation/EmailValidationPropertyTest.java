package com.feadev.eaids.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import java.util.Set;

/**
 * Property-based tests for email format validation.
 *
 * Validates: Requirements 1.8, 2.7
 */
class EmailValidationPropertyTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Simple wrapper to test @Email annotation in isolation
    private record EmailHolder(@Email(message = "Email format is invalid") String email) {}

    @Property
    void validEmailsShouldPassValidation(@ForAll("validEmails") String email) {
        EmailHolder holder = new EmailHolder(email);
        Set<ConstraintViolation<EmailHolder>> violations = validator.validate(holder);
        Assertions.assertThat(violations).isEmpty();
    }

    @Property
    void emailsWithoutAtSignShouldFailValidation(@ForAll("emailsWithoutAtSign") String email) {
        EmailHolder holder = new EmailHolder(email);
        Set<ConstraintViolation<EmailHolder>> violations = validator.validate(holder);
        Assertions.assertThat(violations).isNotEmpty();
    }

    @Property
    void emailsWithEmptyLocalPartShouldFailValidation(@ForAll("emailsWithEmptyLocalPart") String email) {
        EmailHolder holder = new EmailHolder(email);
        Set<ConstraintViolation<EmailHolder>> violations = validator.validate(holder);
        Assertions.assertThat(violations).isNotEmpty();
    }

    @Property
    void emailsWithEmptyDomainShouldFailValidation(@ForAll("emailsWithEmptyDomain") String email) {
        EmailHolder holder = new EmailHolder(email);
        Set<ConstraintViolation<EmailHolder>> violations = validator.validate(holder);
        Assertions.assertThat(violations).isNotEmpty();
    }

    @Property
    void nullEmailShouldPassValidation() {
        // @Email allows null; @NotNull/@NotBlank handles null check
        EmailHolder holder = new EmailHolder(null);
        Set<ConstraintViolation<EmailHolder>> violations = validator.validate(holder);
        Assertions.assertThat(violations).isEmpty();
    }

    @Provide
    Arbitrary<String> validEmails() {
        Arbitrary<String> localPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(20);
        Arbitrary<String> domain = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(10);
        Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "io", "dev", "ng");

        return Combinators.combine(localPart, domain, tld)
                .as((local, dom, t) -> local + "@" + dom + "." + t);
    }

    @Provide
    Arbitrary<String> emailsWithoutAtSign() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(3)
                .ofMaxLength(30)
                .filter(s -> !s.contains("@"));
    }

    @Provide
    Arbitrary<String> emailsWithEmptyLocalPart() {
        Arbitrary<String> domain = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(10);
        Arbitrary<String> tld = Arbitraries.of("com", "org", "net");

        return Combinators.combine(domain, tld)
                .as((dom, t) -> "@" + dom + "." + t);
    }

    @Provide
    Arbitrary<String> emailsWithEmptyDomain() {
        Arbitrary<String> localPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(10);

        return localPart.map(local -> local + "@");
    }

    /**
     * Helper assertions class to avoid depending on AssertJ directly.
     */
    private static class Assertions {
        static <T> ViolationAssert<T> assertThat(Set<ConstraintViolation<T>> violations) {
            return new ViolationAssert<>(violations);
        }
    }

    private static class ViolationAssert<T> {
        private final Set<ConstraintViolation<T>> violations;

        ViolationAssert(Set<ConstraintViolation<T>> violations) {
            this.violations = violations;
        }

        void isEmpty() {
            if (!violations.isEmpty()) {
                throw new AssertionError("Expected no violations but found: " + violations);
            }
        }

        void isNotEmpty() {
            if (violations.isEmpty()) {
                throw new AssertionError("Expected violations but found none");
            }
        }
    }
}
