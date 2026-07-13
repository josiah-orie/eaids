package com.feadev.eaids.validation;

import com.feadev.eaids.registrations.validation.PhoneNumberValidator;
import net.jqwik.api.*;

/**
 * Property-based tests for phone number format validation (E.164).
 *
 * Validates: Requirements 1.7, 2.8
 */
class PhoneNumberValidationPropertyTest {

    private final PhoneNumberValidator validator = new PhoneNumberValidator();

    @Property
    void validE164NumbersShouldBeAccepted(@ForAll("validE164Numbers") String phoneNumber) {
        boolean result = validator.isValid(phoneNumber, null);
        if (!result) {
            throw new AssertionError("Expected valid for: " + phoneNumber);
        }
    }

    @Property
    void numbersWithoutLeadingPlusShouldBeRejected(@ForAll("numbersWithoutPlus") String phoneNumber) {
        boolean result = validator.isValid(phoneNumber, null);
        if (result) {
            throw new AssertionError("Expected invalid for number without leading '+': " + phoneNumber);
        }
    }

    @Property
    void numbersTooShortShouldBeRejected(@ForAll("tooShortNumbers") String phoneNumber) {
        boolean result = validator.isValid(phoneNumber, null);
        if (result) {
            throw new AssertionError("Expected invalid for too-short number: " + phoneNumber);
        }
    }

    @Property
    void numbersTooLongShouldBeRejected(@ForAll("tooLongNumbers") String phoneNumber) {
        boolean result = validator.isValid(phoneNumber, null);
        if (result) {
            throw new AssertionError("Expected invalid for too-long number: " + phoneNumber);
        }
    }

    @Property
    void numbersWithNonDigitCharactersShouldBeRejected(@ForAll("numbersWithNonDigits") String phoneNumber) {
        boolean result = validator.isValid(phoneNumber, null);
        if (result) {
            throw new AssertionError("Expected invalid for number with non-digits: " + phoneNumber);
        }
    }

    @Property
    void numbersStartingWithZeroAfterPlusShouldBeRejected(@ForAll("numbersStartingWithZero") String phoneNumber) {
        boolean result = validator.isValid(phoneNumber, null);
        if (result) {
            throw new AssertionError("Expected invalid for number starting with +0: " + phoneNumber);
        }
    }

    @Property
    void nullShouldBeAccepted() {
        // Null is valid for @PhoneNumber; @NotNull handles null check
        boolean result = validator.isValid(null, null);
        if (!result) {
            throw new AssertionError("Expected null to be valid (let @NotNull handle it)");
        }
    }

    @Provide
    Arbitrary<String> validE164Numbers() {
        // E.164: '+' followed by first digit [1-9], then 6-14 more digits (total 7-15 digits)
        Arbitrary<Character> firstDigit = Arbitraries.chars().range('1', '9');
        Arbitrary<String> remainingDigits = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(6)
                .ofMaxLength(14);

        return Combinators.combine(firstDigit, remainingDigits)
                .as((first, rest) -> "+" + first + rest);
    }

    @Provide
    Arbitrary<String> numbersWithoutPlus() {
        Arbitrary<Character> firstDigit = Arbitraries.chars().range('1', '9');
        Arbitrary<String> remainingDigits = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(6)
                .ofMaxLength(14);

        return Combinators.combine(firstDigit, remainingDigits)
                .as((first, rest) -> String.valueOf(first) + rest);
    }

    @Provide
    Arbitrary<String> tooShortNumbers() {
        // Less than 7 digits total after '+' (1 first digit + less than 6 remaining)
        Arbitrary<Character> firstDigit = Arbitraries.chars().range('1', '9');
        Arbitrary<String> remainingDigits = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(0)
                .ofMaxLength(5);

        return Combinators.combine(firstDigit, remainingDigits)
                .as((first, rest) -> "+" + first + rest);
    }

    @Provide
    Arbitrary<String> tooLongNumbers() {
        // More than 15 digits total after '+'
        Arbitrary<Character> firstDigit = Arbitraries.chars().range('1', '9');
        Arbitrary<String> remainingDigits = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(15)
                .ofMaxLength(25);

        return Combinators.combine(firstDigit, remainingDigits)
                .as((first, rest) -> "+" + first + rest);
    }

    @Provide
    Arbitrary<String> numbersWithNonDigits() {
        // Valid length but contains non-digit characters
        Arbitrary<String> prefix = Arbitraries.of("+1", "+44", "+234");
        Arbitrary<String> invalidPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(5);
        Arbitrary<String> digitPart = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(2)
                .ofMaxLength(5);

        return Combinators.combine(prefix, digitPart, invalidPart, digitPart)
                .as((p, d1, inv, d2) -> p + d1 + inv + d2);
    }

    @Provide
    Arbitrary<String> numbersStartingWithZero() {
        // '+0' followed by valid-length digits
        Arbitrary<String> remainingDigits = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(6)
                .ofMaxLength(14);

        return remainingDigits.map(rest -> "+0" + rest);
    }
}
