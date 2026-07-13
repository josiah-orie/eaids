package com.feadev.eaids.scouting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scout_bookmarks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_scout_bookmark", columnNames = {"scout_user_id", "athlete_user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ScoutBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "scout_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID scoutUserId;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
