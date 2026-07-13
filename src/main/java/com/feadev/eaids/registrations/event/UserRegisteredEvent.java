package com.feadev.eaids.registrations.event;

import com.feadev.eaids.registrations.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent extends BaseDomainEvent {
    private final UUID userId;
    private final String email;
    private final UserRole role;
}
