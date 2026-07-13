package com.feadev.eaids.videolibrary.repository;

import com.feadev.eaids.videolibrary.model.VideoLibraryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoLibraryRepository extends JpaRepository<VideoLibraryItem, UUID>, JpaSpecificationExecutor<VideoLibraryItem> {

    Page<VideoLibraryItem> findByAthleteUserId(UUID athleteUserId, Pageable pageable);

    Page<VideoLibraryItem> findByCampId(UUID campId, Pageable pageable);

    boolean existsByUrlAndAthleteUserId(String url, UUID athleteUserId);

    long countByCategory(com.feadev.eaids.videolibrary.model.enums.VideoCategory category);
}
