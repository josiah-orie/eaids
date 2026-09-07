package com.feadev.eaids.academy.repository;

import com.feadev.eaids.academy.model.AcademyInstitutionApplication;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AcademyInstitutionApplicationRepository
        extends JpaRepository<AcademyInstitutionApplication, UUID> {

    boolean existsByContactEmail(String contactEmail);

    Page<AcademyInstitutionApplication> findByStatus(ApplicationStatus status, Pageable pageable);
}
