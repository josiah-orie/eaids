package com.feadev.eaids.registrations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    private UUID id;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean enabled;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
