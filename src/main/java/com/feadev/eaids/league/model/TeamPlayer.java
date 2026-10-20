package com.feadev.eaids.league.model;

import com.feadev.eaids.league.model.enums.PlayerStatus;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "team_players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_user_id", nullable = false)
    private User athlete;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FootballPosition position;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.ACTIVE;

    @Column(name = "drafted_date", nullable = false)
    private LocalDate draftedDate;

    @Column(name = "released_date")
    private LocalDate releasedDate;
}
