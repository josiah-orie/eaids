package com.feadev.eaids.adminprofile.dto.request;

import com.feadev.eaids.adminprofile.model.enums.AdminPermissionScope;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminProfileRequest {

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Size(max = 512, message = "Profile image URL must not exceed 512 characters")
    private String profileImageUrl;

    @Size(max = 150, message = "Job title must not exceed 150 characters")
    private String jobTitle;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    @Size(max = 100, message = "Country managed must not exceed 100 characters")
    private String countryManaged;

    @Size(max = 20, message = "Phone extension must not exceed 20 characters")
    private String phoneExtension;

    private AdminPermissionScope permissionsScope;
}
