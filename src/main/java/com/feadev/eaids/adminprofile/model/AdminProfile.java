package com.feadev.eaids.adminprofile.model;

import com.feadev.eaids.adminprofile.model.enums.AdminPermissionScope;
import com.feadev.eaids.registrations.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AdminProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    @Column(name = "job_title", nullable = false, length = 150)
    private String jobTitle;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(length = 100)
    private String region;

    @Column(name = "country_managed", length = 100)
    private String countryManaged;

    @Column(name = "phone_extension", length = 20)
    private String phoneExtension;

    @Enumerated(EnumType.STRING)
    @Column(name = "permissions_scope", nullable = false)
    private AdminPermissionScope permissionsScope;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
