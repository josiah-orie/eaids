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

    @Override
    public void sendApplicationApprovedEmail(String email, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(email);
            message.setSubject("Your EAIDS Academy application has been approved");
            message.setText("Hello " + name + ",\n\n"
                    + "Congratulations! Your application to the EAIDS Academy has been approved.\n\n"
                    + "Our team will be in touch with the next steps, including training schedule "
                    + "assignments.\n\n"
                    + "Welcome aboard,\nThe EAIDS Academy Team");

            mailSender.send(message);
            log.info("Academy approval email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send academy approval email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Override
    public void sendApplicationRejectedEmail(String email, String name, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(email);
            message.setSubject("Update on your EAIDS Academy application");
            message.setText("Hello " + name + ",\n\n"
                    + "Thank you for your interest in the EAIDS Academy. "
                    + "After careful review, we are unable to approve your application at this time.\n\n"
                    + "Reason: " + reason + "\n\n"
                    + "You are welcome to reapply in the future.\n\n"
                    + "Best regards,\nThe EAIDS Academy Team");

            mailSender.send(message);
            log.info("Academy rejection email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send academy rejection email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Override
    public void sendStaffAssignmentEmail(String email, String name, String scheduleTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(email);
            message.setSubject("You have been assigned to an EAIDS Academy training schedule");
            message.setText("Hello " + name + ",\n\n"
                    + "You have been assigned to the training schedule: \"" + scheduleTitle + "\".\n\n"
                    + "Please log in to the EAIDS platform to view the details.\n\n"
                    + "Best regards,\nThe EAIDS Academy Team");

            mailSender.send(message);
            log.info("Academy staff assignment email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send academy staff assignment email to {}: {}", email, e.getMessage(), e);
        }
    }

    @Override
    public void sendEnrollmentEmail(String email, String name, String scheduleTitle, String startDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_ADDRESS);
            message.setTo(email);
            message.setSubject("You have been enrolled in an EAIDS Academy training schedule");
            message.setText("Hello " + name + ",\n\n"
                    + "You have been enrolled in the training schedule: \"" + scheduleTitle + "\""
                    + (startDate != null ? ", starting on " + startDate : "") + ".\n\n"
                    + "Please log in to the EAIDS platform to view the details.\n\n"
                    + "Best regards,\nThe EAIDS Academy Team");

            mailSender.send(message);
            log.info("Academy enrollment email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send academy enrollment email to {}: {}", email, e.getMessage(), e);
        }
    }
}
