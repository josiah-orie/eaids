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

    /**
     * Notifies an academy applicant that their application was approved.
     *
     * @param email the applicant's email address
     * @param name  the applicant/institution name
     */
    void sendApplicationApprovedEmail(String email, String name);

    /**
     * Notifies an academy applicant that their application was rejected.
     *
     * @param email  the applicant's email address
     * @param name   the applicant/institution name
     * @param reason the rejection reason
     */
    void sendApplicationRejectedEmail(String email, String name, String reason);

    /**
     * Notifies a coach or scout that they have been assigned to a training schedule.
     *
     * @param email         the staff member's email address
     * @param name          the staff member's name
     * @param scheduleTitle the training schedule title
     */
    void sendStaffAssignmentEmail(String email, String name, String scheduleTitle);

    /**
     * Notifies an academy applicant that they have been enrolled into a training schedule.
     *
     * @param email         the applicant's email address
     * @param name          the applicant/institution name
     * @param scheduleTitle the training schedule title
     * @param startDate     the schedule start date (ISO text)
     */
    void sendEnrollmentEmail(String email, String name, String scheduleTitle, String startDate);
}
