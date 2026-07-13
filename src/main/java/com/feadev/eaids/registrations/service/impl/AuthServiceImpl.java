package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.dto.request.AthleteRegistrationRequest;
import com.feadev.eaids.registrations.dto.request.CoachRegistrationRequest;
import com.feadev.eaids.registrations.dto.request.ForgotPasswordRequest;
import com.feadev.eaids.registrations.dto.request.LoginRequest;
import com.feadev.eaids.registrations.dto.request.RefreshTokenRequest;
import com.feadev.eaids.registrations.dto.request.ResendVerificationRequest;
import com.feadev.eaids.registrations.dto.request.ResetPasswordRequest;
import com.feadev.eaids.registrations.dto.request.SetupPasswordRequest;
import com.feadev.eaids.registrations.dto.request.VerifyEmailRequest;
import com.feadev.eaids.registrations.dto.request.VerifyOtpRequest;
import com.feadev.eaids.registrations.dto.response.AuthResponse;
import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.dto.response.RegistrationResponse;
import com.feadev.eaids.registrations.event.EmailVerifiedEvent;
import com.feadev.eaids.registrations.event.PasswordResetRequestedEvent;
import com.feadev.eaids.registrations.event.UserRegisteredEvent;
import com.feadev.eaids.registrations.exception.AccountNotActiveException;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.InvalidTokenException;
import com.feadev.eaids.registrations.model.CoachProfile;
import com.feadev.eaids.registrations.model.EmailVerificationToken;
import com.feadev.eaids.registrations.model.PasswordResetToken;
import com.feadev.eaids.registrations.model.RefreshToken;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.EmailVerificationTokenRepository;
import com.feadev.eaids.registrations.repository.PasswordResetTokenRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.AuthService;
import com.feadev.eaids.registrations.service.EmailService;
import com.feadev.eaids.registrations.service.OtpService;
import com.feadev.eaids.registrations.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenService tokenService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    // ========== Registration Methods (Task 13.2) ==========

    @Override
    public RegistrationResponse registerAthlete(AthleteRegistrationRequest request) {
        validateUniqueness(request.getEmail(), request.getPhoneNumber());

        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        user = userRepository.save(user);

        String verificationToken = generateAndSaveVerificationToken(user);
//        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        emailService.sendOtpEmail(user.getEmail(), verificationToken, "verify-email");

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getRole()));

        auditService.log(AuditAction.REGISTRATION, user.getId(), user.getEmail(), null, null);

        return RegistrationResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }

    @Override
    public RegistrationResponse registerCoach(CoachRegistrationRequest request) {
        validateUniqueness(request.getEmail(), request.getPhoneNumber());

        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_COACH)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        user = userRepository.save(user);

        CoachProfile coachProfile = CoachProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .nationality(request.getNationality())
                .stateOfResidence(request.getStateOfResidence())
                .phoneNumber(request.getProfilePhoneNumber())
                .emailAddress(request.getEmailAddress())
                .currentSport(request.getCurrentSport())
                .currentOrganization(request.getCurrentOrganization())
                .currentPosition(request.getCurrentPosition())
                .yearsOfExperience(request.getYearsOfExperience())
                .areaOfInterest(request.getAreaOfInterest())
                .availability(request.getAvailability())
                .americanFootballExperience(request.getAmericanFootballExperience())
                .involvementLevel(request.getInvolvementLevel())
                .coachingInterests(new HashSet<>(request.getCoachingInterests()))
                .build();

        coachProfileRepository.save(coachProfile);

        String verificationToken = generateAndSaveVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getRole()));

        auditService.log(AuditAction.REGISTRATION, user.getId(), user.getEmail(), null, null);

        return RegistrationResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }

    // ========== Email Verification Methods (Task 13.3) ==========

    @Override
    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (token.isUsed()) {
            throw new InvalidTokenException("Verification token has already been used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        User user = token.getUser();

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new InvalidTokenException("Email verification is not applicable for the current user status");
        }

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);

        user.setEmailVerified(true);
        userRepository.save(user);

        eventPublisher.publishEvent(new EmailVerifiedEvent(user.getId()));

        auditService.log(AuditAction.EMAIL_VERIFICATION, user.getId(), user.getEmail(), null, null);

        return MessageResponse.builder()
                .message("Email verified successfully")
                .build();
    }

    @Override
    public MessageResponse resendVerification(ResendVerificationRequest request) {
        var userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            // Silent success to prevent email enumeration
            return MessageResponse.builder()
                    .message("If the email is registered, a verification email has been sent")
                    .build();
        }

        User user = userOptional.get();

        if (user.isEmailVerified()) {
            throw new InvalidTokenException("Email is already verified");
        }

        // Invalidate existing unused tokens
        emailVerificationTokenRepository.findByUser_IdAndUsedFalse(user.getId())
                .ifPresent(existingToken -> {
                    existingToken.setUsed(true);
                    emailVerificationTokenRepository.save(existingToken);
                });

        // Generate new token with 24h expiry
        String verificationToken = generateAndSaveVerificationToken(user);
//        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        emailService.sendOtpEmail(user.getEmail(), verificationToken, "verify-email");

        return MessageResponse.builder()
                .message("If the email is registered, a verification email has been sent")
                .build();
    }

    // ========== Login, Refresh, Logout (Task 13.4) ==========

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();

        // Try email first, then phone
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByPhoneNumber(identifier)
                        .orElse(null));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditService.log(AuditAction.LOGIN_FAILURE, null, identifier, null, null);
            throw new InvalidTokenException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            auditService.log(AuditAction.LOGIN_FAILURE, user.getId(), identifier, null, null);
            throw new AccountNotActiveException("Account is not active. Current status: " + user.getStatus().name());
        }

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        auditService.log(AuditAction.LOGIN_SUCCESS, user.getId(), user.getEmail(), null, null);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .status(user.getStatus().name())
                        .build())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(request.getRefreshToken());

        // Revoke old token
        tokenService.revokeRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        // Generate new token pair
        String newAccessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = tokenService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(3600)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .status(user.getStatus().name())
                        .build())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        RefreshToken token = tokenService.validateRefreshToken(refreshToken);
        tokenService.revokeRefreshToken(refreshToken);

        auditService.log(AuditAction.LOGOUT, token.getUser().getId(), token.getUser().getEmail(), null, null);
    }

    // ========== Password Reset Flow (Task 13.5) ==========

    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        var userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            // Silent success to prevent email enumeration
            return MessageResponse.builder()
                    .message("If the email is registered, a password reset OTP has been sent")
                    .build();
        }

        User user = userOptional.get();
        String otp = otpService.generateOtp(user);

        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getId(), otp));

        return MessageResponse.builder()
                .message("If the email is registered, a password reset OTP has been sent")
                .build();
    }

    @Override
    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        User user = otpService.verifyOtp(request.getEmail(), request.getOtp());

        // Generate password reset token with 15-minute expiry
        String resetTokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(resetTokenValue)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        passwordResetTokenRepository.save(resetToken);

        return MessageResponse.builder()
                .message("OTP verified successfully")
                .data(resetTokenValue)
                .build();
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Password reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Password reset token has expired");
        }

        User user = resetToken.getUser();

        // Encode and update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens for the user
        tokenService.revokeAllUserRefreshTokens(user.getId());

        auditService.log(AuditAction.PASSWORD_CHANGE, user.getId(), user.getEmail(), null, null);

        return MessageResponse.builder()
                .message("Password reset successfully")
                .build();
    }

    // ========== Scout Password Setup (Task 2) ==========

    @Override
    public MessageResponse setupPassword(SetupPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid password setup token"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Password setup token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Password setup token has expired");
        }

        User user = resetToken.getUser();

        if (user.getRole() != UserRole.ROLE_SCOUT) {
            throw new InvalidTokenException("Password setup is only available for scout accounts");
        }

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new InvalidTokenException("Account is not in a valid state for password setup");
        }

        // Set the scout's chosen password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        auditService.log(AuditAction.REGISTRATION, user.getId(), user.getEmail(), null, null);

        log.info("Scout password setup completed: userId={}, email={}", user.getId(), user.getEmail());

        return MessageResponse.builder()
                .message("Password set successfully. You can now log in.")
                .build();
    }

    // ========== Private Helper Methods ==========

    private void validateUniqueness(String email, String phoneNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateResourceException("Phone number is already registered");
        }
    }

    private String generateAndSaveVerificationToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(tokenValue)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .build();

        emailVerificationTokenRepository.save(verificationToken);

        return tokenValue;
    }
}
