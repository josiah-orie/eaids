package com.feadev.eaids.positionprofile.model;

import com.feadev.eaids.camp.model.Camp;
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
@Table(name = "camp_position_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_camp_position_profile", columnNames = {"camp_id", "position_profile_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CampPositionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_profile_id", nullable = false)
    private PositionProfile positionProfile;

    @CreatedDate
    @Column(name = "assigned_date", nullable = false, updatable = false)
    private LocalDateTime assignedDate;
}
