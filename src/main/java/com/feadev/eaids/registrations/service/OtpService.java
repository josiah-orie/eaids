package com.feadev.eaids.registrations.service;

import com.feadev.eaids.registrations.model.User;

public interface OtpService {

    /**
     * Generates a 6-digit numeric OTP for the given user,
     * stores it with a 5-minute expiry, and returns the OTP code.
     *
     * @param user the user requesting the OTP
     * @return the generated 6-digit OTP code
     */
    String generateOtp(User user);

    /**
     * Verifies the OTP submitted for the given email.
     * Checks expiry, compares codes, and tracks failed attempts.
     * Invalidates the OTP after 5 failed attempts.
     *
     * @param email the email of the user
     * @param otp   the OTP code to verify
     * @return the User associated with the OTP if verification succeeds
     */
    User verifyOtp(String email, String otp);
}
