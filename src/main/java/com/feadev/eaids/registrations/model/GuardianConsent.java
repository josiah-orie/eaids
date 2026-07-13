package com.feadev.eaids.registrations.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guardian_consents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardianConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "guardian_name", nullable = false, length = 100)
    private String guardianName;

    @Column(name = "guardian_email", nullable = false, length = 255)
    private String guardianEmail;

    @Column(name = "guardian_phone", nullable = false, length = 20)
    private String guardianPhone;

    @Column(name = "consent_accepted", nullable = false)
    private boolean consentAccepted;

    @Column(name = "electronic_signature", nullable = false, length = 500)
    private String electronicSignature;

    @Column(name = "consent_date", nullable = false)
    private LocalDateTime consentDate;
}
