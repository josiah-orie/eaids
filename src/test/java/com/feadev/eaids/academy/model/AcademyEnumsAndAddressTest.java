package com.feadev.eaids.academy.model;

import com.feadev.eaids.academy.model.enums.ApplicantType;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.model.enums.InstitutionType;
import com.feadev.eaids.academy.model.enums.Location;
import com.feadev.eaids.academy.model.enums.ScheduleStatus;
import com.feadev.eaids.academy.model.enums.ScheduleTargetType;
import com.feadev.eaids.academy.model.enums.StaffType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AcademyEnumsAndAddressTest {

    @Test
    @DisplayName("ApplicationStatus has PENDING, APPROVED, REJECTED")
    void applicationStatusValues() {
        assertThat(ApplicationStatus.values())
                .containsExactly(ApplicationStatus.PENDING,
                        ApplicationStatus.APPROVED,
                        ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("Location has ABUJA and LAGOS")
    void locationValues() {
        assertThat(Location.values())
                .containsExactly(Location.ABUJA, Location.LAGOS);
    }

    @Test
    @DisplayName("InstitutionType has all eight categories")
    void institutionTypeValues() {
        assertThat(InstitutionType.values()).containsExactly(
                InstitutionType.PUBLIC_PRIMARY,
                InstitutionType.PRIVATE_PRIMARY,
                InstitutionType.PUBLIC_SECONDARY,
                InstitutionType.PRIVATE_SECONDARY,
                InstitutionType.UNIVERSITY,
                InstitutionType.POLYTECHNIC,
                InstitutionType.COLLEGE_OF_EDUCATION,
                InstitutionType.OTHER);
    }

    @Test
    @DisplayName("ScheduleTargetType has INDIVIDUAL, INSTITUTION, BOTH")
    void scheduleTargetTypeValues() {
        assertThat(ScheduleTargetType.values()).containsExactly(
                ScheduleTargetType.INDIVIDUAL,
                ScheduleTargetType.INSTITUTION,
                ScheduleTargetType.BOTH);
    }

    @Test
    @DisplayName("ScheduleStatus has DRAFT, PUBLISHED, CANCELLED, COMPLETED")
    void scheduleStatusValues() {
        assertThat(ScheduleStatus.values()).containsExactly(
                ScheduleStatus.DRAFT,
                ScheduleStatus.PUBLISHED,
                ScheduleStatus.CANCELLED,
                ScheduleStatus.COMPLETED);
    }

    @Test
    @DisplayName("StaffType has COACH and SCOUT")
    void staffTypeValues() {
        assertThat(StaffType.values()).containsExactly(StaffType.COACH, StaffType.SCOUT);
    }

    @Test
    @DisplayName("ApplicantType has INDIVIDUAL and INSTITUTION")
    void applicantTypeValues() {
        assertThat(ApplicantType.values())
                .containsExactly(ApplicantType.INDIVIDUAL, ApplicantType.INSTITUTION);
    }

    @Test
    @DisplayName("Address builder and getters round-trip all fields")
    void addressBuilderAndGetters() {
        Address address = Address.builder()
                .country("Nigeria")
                .state("FCT")
                .lgaOrProvince("Municipal")
                .streetAddress("12 Independence Ave")
                .build();

        assertThat(address.getCountry()).isEqualTo("Nigeria");
        assertThat(address.getState()).isEqualTo("FCT");
        assertThat(address.getLgaOrProvince()).isEqualTo("Municipal");
        assertThat(address.getStreetAddress()).isEqualTo("12 Independence Ave");
    }
}
