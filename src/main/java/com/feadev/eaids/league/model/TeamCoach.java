package com.feadev.eaids.league.model;

import com.feadev.eaids.league.model.enums.TeamCoachRole;
import com.feadev.eaids.registrations.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_coaches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TeamCoach {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_user_id", nullable = false)
    private User coach;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamCoachRole role;

    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedBy
    @Column(name = "assigned_by")
    private String assignedBy;
}
