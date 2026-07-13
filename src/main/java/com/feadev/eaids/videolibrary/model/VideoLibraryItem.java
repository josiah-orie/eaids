package com.feadev.eaids.videolibrary.model;

import com.feadev.eaids.registrations.model.enums.VideoType;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.videolibrary.model.enums.VideoCategory;
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
@Table(name = "video_library_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class VideoLibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_type", nullable = false)
    private VideoType videoType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoCategory category;

    @Enumerated(EnumType.STRING)
    @Column
    private FootballPosition position;

    @Column(name = "camp_id", columnDefinition = "BINARY(16)")
    private UUID campId;

    @Column(name = "uploaded_by", nullable = false, columnDefinition = "BINARY(16)")
    private UUID uploadedBy;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
