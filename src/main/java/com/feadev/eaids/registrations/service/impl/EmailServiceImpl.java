package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link EmailService} using Spring's JavaMailSender.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String FROM_ADDRESS = "notifications@jovine360solutions.com";
    private static final String VERIFICATION_SUBJECT = "Verify your email";
    private static final String OTP_SUBJECT = "Password Reset OTP";
    private static final String PASSWORD_SETUP_SUBJECT = "Set up your EAIDS account password";

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendVerificationEmail(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(email);
            message.setSubject(VERIFICATION_SUBJECT);
            message.setText("Please verify your email by clicking: "
                    + baseUrl + "/api/v1/auth/verify-email?token=" + token);

            mailSender.send(message);
            log.info("Verification email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Override
    public void sendOtpEmail(String email, String otp, String subjectType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(email);
            if(subjectType.equals("reset")){
                message.setSubject(OTP_SUBJECT);
                message.setText("Your password reset OTP code is: " + otp
                        + "\n\nThis code expires in 20 minutes. Do not share it with anyone.");
            }else{
                message.setSubject(VERIFICATION_SUBJECT);
                message.setText("Your email verification OTP code is: " + otp
                        + "\n\nThis code expires in 20 minutes. Do not share it with anyone.");
            }

            mailSender.send(message);
            log.info("OTP email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Override
    public void sendPasswordSetupEmail(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(email);
            message.setSubject(PASSWORD_SETUP_SUBJECT);
            message.setText("Welcome to EAIDS!\n\n"
                    + "An administrator has created a scout account for you. "
                    + "Please set up your password by clicking the link below:\n\n"
                    + baseUrl + "/setup-password?token=" + token + "\n\n"
                    + "This link expires in 72 hours.\n\n"
                    + "If you did not expect this email, please ignore it.");

            mailSender.send(message);
            log.info("Password setup email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send password setup email to {}: {}", email, e.getMessage(), e);
        }
    }
}
