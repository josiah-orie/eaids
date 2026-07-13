package com.feadev.eaids.registrations.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PasswordResetRequestedEvent extends BaseDomainEvent {
    private final UUID userId;
    private final String otp;
}
