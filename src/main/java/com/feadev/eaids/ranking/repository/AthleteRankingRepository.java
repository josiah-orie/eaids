package com.feadev.eaids.ranking.repository;

import com.feadev.eaids.ranking.model.AthleteRanking;
import com.feadev.eaids.ranking.model.enums.RankingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AthleteRankingRepository extends JpaRepository<AthleteRanking, UUID> {

    List<AthleteRanking> findByCampIdAndRankingType(UUID campId, RankingType rankingType);

    List<AthleteRanking> findByCampId(UUID campId);

    List<AthleteRanking> findByEnrollmentId(UUID enrollmentId);

    List<AthleteRanking> findByCampIdAndRankingTypeAndFilterValue(UUID campId, RankingType rankingType, String filterValue);

    void deleteByCampId(UUID campId);
}
