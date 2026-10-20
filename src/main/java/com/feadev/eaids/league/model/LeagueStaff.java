package com.feadev.eaids.league.model;

import com.feadev.eaids.league.model.enums.LeagueStaffRole;
import com.feadev.eaids.registrations.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "league_staff")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_role", nullable = false)
    private LeagueStaffRole staffRole;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate;
}
