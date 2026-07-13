package com.feadev.eaids.registrations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {

    private UUID id;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private String fullName;
}
