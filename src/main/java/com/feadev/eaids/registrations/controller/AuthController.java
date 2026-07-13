package com.feadev.eaids.registrations.controller;

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
import com.feadev.eaids.registrations.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, token management, and password reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/athlete")
    @Operation(summary = "Register a new athlete", description = "Creates a new athlete account with PENDING_VERIFICATION status and sends a verification email")
    public ResponseEntity<RegistrationResponse> registerAthlete(@Valid @RequestBody AthleteRegistrationRequest request) {
        RegistrationResponse response = authService.registerAthlete(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/coach")
    @Operation(summary = "Register a new coach", description = "Creates a new coach account with profile details and PENDING_VERIFICATION status")
    public ResponseEntity<RegistrationResponse> registerCoach(@Valid @RequestBody CoachRegistrationRequest request) {
        RegistrationResponse response = authService.registerCoach(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user with email/phone and password, returning access and refresh tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token and refresh token using a valid refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes the provided refresh token to end the user session")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(MessageResponse.builder().message("Logged out successfully").build());
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verifies a user's email address using the token sent during registration")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        MessageResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Invalidates any existing verification token and sends a new one")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        MessageResponse response = authService.resendVerification(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Generates a 6-digit OTP and sends it to the provided email address")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Validates the OTP and returns a password reset token if successful")
    public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        MessageResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the user password using a valid password reset token")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/setup-password")
    @Operation(summary = "Set up scout password", description = "Sets the password for a newly created scout account using the token from the setup email. Activates the account upon success.")
    public ResponseEntity<MessageResponse> setupPassword(@Valid @RequestBody SetupPasswordRequest request) {
        MessageResponse response = authService.setupPassword(request);
        return ResponseEntity.ok(response);
    }
}
