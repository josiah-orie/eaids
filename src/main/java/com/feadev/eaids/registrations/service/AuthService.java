package com.feadev.eaids.registrations.service;

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

public interface AuthService {

    RegistrationResponse registerAthlete(AthleteRegistrationRequest request);

    RegistrationResponse registerCoach(CoachRegistrationRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    MessageResponse verifyEmail(VerifyEmailRequest request);

    MessageResponse resendVerification(ResendVerificationRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse verifyOtp(VerifyOtpRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse setupPassword(SetupPasswordRequest request);
}
