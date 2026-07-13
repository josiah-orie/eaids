package com.feadev.eaids.registrations.service;

/**
 * Service interface for sending emails (verification, OTP, etc.).
 */
public interface EmailService {

    /**
     * Sends a verification email containing a link with the given token.
     *
     * @param email the recipient email address
     * @param token the email verification token
     */
    void sendVerificationEmail(String email, String token);

    /**
     * Sends a password-reset OTP code to the given email address.
     *
     * @param email the recipient email address
     * @param otp   the one-time password code
     */
    void sendOtpEmail(String email, String otp, String subjectType);

    /**
     * Sends a password setup email to a newly created scout account.
     * The email contains a link allowing the scout to set their password.
     *
     * @param email the recipient email address
     * @param token the password setup token (72-hour expiry)
     */
    void sendPasswordSetupEmail(String email, String token);
}
