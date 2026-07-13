package com.feadev.eaids.nationalranking.repository;

import com.feadev.eaids.nationalranking.model.NationalRankingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NationalRankingConfigRepository extends JpaRepository<NationalRankingConfig, UUID> {

    List<NationalRankingConfig> findByActiveTrue();
}
