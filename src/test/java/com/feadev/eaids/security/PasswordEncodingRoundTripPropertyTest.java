package com.feadev.eaids.security;

import net.jqwik.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Property-based tests for password encoding round-trip.
 *
 * Property 5: Password Encoding Round-Trip
 * Validates: Requirements 2.4
 */
class PasswordEncodingRoundTripPropertyTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Property(tries = 20)
    void encodedPasswordAlwaysDiffersFromPlaintext(
            @ForAll("validPasswords") String plaintext
    ) {
        String encoded = passwordEncoder.encode(plaintext);

        assert !encoded.equals(plaintext)
                : "Encoded password must never equal the plaintext for: " + plaintext;
    }

    @Property(tries = 20)
    void encodedPasswordAlwaysVerifiesWithMatches(
            @ForAll("validPasswords") String plaintext
    ) {
        String encoded = passwordEncoder.encode(plaintext);

        assert passwordEncoder.matches(plaintext, encoded)
                : "BCrypt.matches should return true for the original plaintext";
    }

    @Property(tries = 20)
    void differentPlaintextNeverMatchesEncodedPassword(
            @ForAll("validPasswords") String plaintext,
            @ForAll("validPasswords") String differentPlaintext
    ) {
        Assume.that(!plaintext.equals(differentPlaintext));

        String encoded = passwordEncoder.encode(plaintext);

        assert !passwordEncoder.matches(differentPlaintext, encoded)
                : "A different password should never match the encoded hash";
    }

    @Property(tries = 20)
    void encodingIsSalted_samePasswordProducesDifferentHashes(
            @ForAll("validPasswords") String plaintext
    ) {
        String encoded1 = passwordEncoder.encode(plaintext);
        String encoded2 = passwordEncoder.encode(plaintext);

        assert !encoded1.equals(encoded2)
                : "BCrypt should produce different hashes for the same input (salted)";
    }

    @Provide
    Arbitrary<String> validPasswords() {
        // Generate passwords that meet complexity: 8-128 chars with uppercase, lowercase, digit, special
        Arbitrary<String> upperPart = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(1)
                .ofMaxLength(3);
        Arbitrary<String> lowerPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(10);
        Arbitrary<String> digitPart = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(3);
        Arbitrary<String> specialPart = Arbitraries.of("@", "#", "$", "%", "!", "&", "*");

        return Combinators.combine(upperPart, lowerPart, digitPart, specialPart)
                .as((upper, lower, digit, special) -> upper + lower + digit + special);
    }
}
