package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.exception.InvalidTokenException;
import com.feadev.eaids.registrations.exception.OtpExhaustedException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.OtpToken;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.OtpTokenRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateOtp(User user) {
        // Invalidate any existing unused OTP for this user
        otpTokenRepository.findByUser_IdAndUsedFalse(user.getId())
                .ifPresent(existingOtp -> {
                    existingOtp.setUsed(true);
                    otpTokenRepository.save(existingOtp);
                });

        // Generate a 6-digit numeric code
        String otpCode = generateNumericCode();

        // Create and save OTP token with 5-minute expiry
        OtpToken otpToken = OtpToken.builder()
                .user(user)
                .otpCode(otpCode)
                .failedAttempts(0)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpTokenRepository.save(otpToken);

        return otpCode;
    }

    @Override
    @Transactional
    public User verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        OtpToken otpToken = otpTokenRepository.findByUser_IdAndUsedFalse(user.getId())
                .orElseThrow(() -> new InvalidTokenException("OTP is invalid or has already been used"));

        // Check if OTP has expired
        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
            throw new InvalidTokenException("OTP has expired");
        }

        // Check if code matches
        if (!otpToken.getOtpCode().equals(otp)) {
            otpToken.setFailedAttempts(otpToken.getFailedAttempts() + 1);

            // Invalidate OTP after 5 failed attempts
            if (otpToken.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                otpToken.setUsed(true);
                otpTokenRepository.save(otpToken);
                throw new OtpExhaustedException("OTP has been invalidated after 5 failed attempts. Please request a new one.");
            }

            otpTokenRepository.save(otpToken);
            throw new InvalidTokenException("OTP is invalid or has already been used");
        }

        // OTP is valid — mark as used and return user
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        return user;
    }

    private String generateNumericCode() {
        int code = secureRandom.nextInt(900_000) + 100_000; // Generates 100000-999999
        return String.valueOf(code);
    }
}
