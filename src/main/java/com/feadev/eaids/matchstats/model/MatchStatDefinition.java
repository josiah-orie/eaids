package com.feadev.eaids.matchstats.model;

import com.feadev.eaids.matchstats.model.enums.PositionGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "match_stat_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchStatDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_group", nullable = false)
    private PositionGroup positionGroup;

    @Column(name = "stat_name", nullable = false, length = 100)
    private String statName;

    @Column(length = 30)
    private String unit;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
