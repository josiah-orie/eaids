package com.feadev.eaids.academy.repository;

import com.feadev.eaids.academy.model.AcademyIndividualApplication;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AcademyIndividualApplicationRepository
        extends JpaRepository<AcademyIndividualApplication, UUID> {

    boolean existsByEmail(String email);

    Page<AcademyIndividualApplication> findByStatus(ApplicationStatus status, Pageable pageable);
}
