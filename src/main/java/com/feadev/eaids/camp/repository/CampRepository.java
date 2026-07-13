package com.feadev.eaids.camp.repository;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.camp.model.enums.CampType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampRepository extends JpaRepository<Camp, UUID>, JpaSpecificationExecutor<Camp> {

    List<Camp> findByStatus(CampStatus status);

    List<Camp> findByCampType(CampType campType);

    List<Camp> findByCountryIgnoreCase(String country);
}
