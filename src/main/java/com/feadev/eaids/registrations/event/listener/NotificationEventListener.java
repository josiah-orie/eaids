package com.feadev.eaids.registrations.event.listener;

import com.feadev.eaids.registrations.event.PasswordResetRequestedEvent;
import com.feadev.eaids.registrations.event.UserRegisteredEvent;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Handles UserRegisteredEvent by sending a notification email.
     * Note: The actual verification email with the token is sent directly during
     * registration (since the service has the token). This listener handles
     * supplementary notification if needed, but primarily logs the event.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Processing UserRegisteredEvent for user: {} ({})", event.getEmail(), event.getUserId());
        // The verification email with token is sent directly by AuthService during registration.
        // This listener can be extended for additional notifications (e.g., welcome email).
    }

    /**
     * Handles PasswordResetRequestedEvent by sending the OTP email.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        log.info("Processing PasswordResetRequestedEvent for user: {}", event.getUserId());

        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new IllegalStateException(
                        "User not found for PasswordResetRequestedEvent: " + event.getUserId()));

        emailService.sendOtpEmail(user.getEmail(), event.getOtp(), "reset");
        log.info("OTP email sent successfully to user: {}", event.getUserId());
    }

    @Recover
    public void recoverUserRegistered(Exception ex, UserRegisteredEvent event) {
        log.error("Permanent failure processing UserRegisteredEvent for user: {} after 3 attempts. Error: {}",
                event.getUserId(), ex.getMessage(), ex);
    }

    @Recover
    public void recoverPasswordResetRequested(Exception ex, PasswordResetRequestedEvent event) {
        log.error("Permanent failure processing PasswordResetRequestedEvent for user: {} after 3 attempts. Error: {}",
                event.getUserId(), ex.getMessage(), ex);
    }
}
