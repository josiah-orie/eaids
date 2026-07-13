package com.feadev.eaids.nationalranking.repository;

import com.feadev.eaids.nationalranking.model.NationalRanking;
import com.feadev.eaids.nationalranking.model.enums.RankingScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NationalRankingRepository extends JpaRepository<NationalRanking, UUID> {

    @Query("SELECT nr FROM NationalRanking nr WHERE nr.rankingScope = :scope " +
            "AND (:scopeValue IS NULL OR nr.scopeValue = :scopeValue) " +
            "ORDER BY nr.rankValue ASC")
    Page<NationalRanking> findByScope(
            @Param("scope") RankingScope scope,
            @Param("scopeValue") String scopeValue,
            Pageable pageable);

    List<NationalRanking> findByAthleteUserIdOrderByGeneratedAtDesc(UUID athleteUserId);

    void deleteByConfigId(UUID configId);
}
