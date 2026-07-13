package com.feadev.eaids.adminprofile.dto.response;

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
public class AdminProfileResponse {

    private UUID id;
    private UUID userId;
    private String email;
    private String fullName;
    private String profileImageUrl;
    private String jobTitle;
    private String department;
    private String region;
    private String countryManaged;
    private String phoneExtension;
    private String permissionsScope;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
