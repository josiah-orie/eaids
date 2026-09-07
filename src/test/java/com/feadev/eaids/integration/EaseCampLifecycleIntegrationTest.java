package com.feadev.eaids.integration;

import com.feadev.eaids.camp.dto.request.CreateCampRequest;
import com.feadev.eaids.camp.dto.response.CampResponse;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.camp.model.enums.CampType;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.camp.service.CampService;
import com.feadev.eaids.enrollment.dto.response.EnrollmentResponse;
import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;
import com.feadev.eaids.enrollment.service.EnrollmentService;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for the EASE Camp Lifecycle.
 *
 * Tests the full flow:
 * 1. Create camp
 * 2. Configure camp (stages, metrics)
 * 3. Transition to REGISTRATION_OPEN
 * 4. Enroll athletes
 * 5. Transition through lifecycle
 * 6. Verify data consistency and business rules
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("integration")
@Tag("docker")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EaseCampLifecycleIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("eaids_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private CampService campService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CampRepository campRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static UUID campId;
    private static UUID athleteUserId;
    private static UUID adminUserId;

    @BeforeAll
    static void beforeAll() {
        // Static references will be set during tests
    }

    @Test
    @Order(1)
    @DisplayName("Setup: Create admin and athlete users")
    void setupUsers() {
        // Create admin user
        User admin = User.builder()
                .email("admin@eaids-test.com")
                .phoneNumber("+2341234567890")
                .password(passwordEncoder.encode("Admin@123"))
                .role(UserRole.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();
        admin = userRepository.save(admin);
        adminUserId = admin.getId();
        assertNotNull(adminUserId);

        // Create athlete user
        User athlete = User.builder()
                .email("athlete@eaids-test.com")
                .phoneNumber("+2349876543210")
                .password(passwordEncoder.encode("Athlete@123"))
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();
        athlete = userRepository.save(athlete);
        athleteUserId = athlete.getId();
        assertNotNull(athleteUserId);
    }

    @Test
    @Order(2)
    @DisplayName("Step 1: Create a camp - should start in DRAFT status")
    void createCamp_shouldStartInDraftStatus() {
        CreateCampRequest request = CreateCampRequest.builder()
                .name("Integration Test Camp 2026")
                .description("E2E test camp for EASE module")
                .campType(CampType.IDENTIFICATION)
                .ageCategory(AgeCategory.UNDER_18)
                .location("Test Stadium, Lagos")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(37))
                .registrationDeadline(LocalDate.now().plusDays(25))
                .capacity(50)
                .build();

        CampResponse response = campService.createCamp(request);

        assertNotNull(response.getId());
        assertEquals("DRAFT", response.getStatus());
        assertEquals("Integration Test Camp 2026", response.getName());
        assertEquals(50, response.getCapacity());
        assertEquals(0, response.getCurrentEnrollment());

        campId = response.getId();
    }

    @Test
    @Order(3)
    @DisplayName("Step 2: Transition camp DRAFT -> REGISTRATION_OPEN")
    void transitionToRegistrationOpen() {
        CampResponse response = campService.transitionStatus(campId, CampStatus.REGISTRATION_OPEN);

        assertEquals("REGISTRATION_OPEN", response.getStatus());
    }

    @Test
    @Order(4)
    @DisplayName("Step 3: Invalid transition REGISTRATION_OPEN -> IN_PROGRESS should fail")
    void invalidTransition_shouldThrow() {
        assertThrows(IllegalStateException.class, () ->
                campService.transitionStatus(campId, CampStatus.IN_PROGRESS));
    }

    @Test
    @Order(5)
    @DisplayName("Step 4: Transition REGISTRATION_OPEN -> REGISTRATION_CLOSED")
    void transitionToRegistrationClosed() {
        CampResponse response = campService.transitionStatus(campId, CampStatus.REGISTRATION_CLOSED);
        assertEquals("REGISTRATION_CLOSED", response.getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("Step 5: Transition REGISTRATION_CLOSED -> IN_PROGRESS")
    void transitionToInProgress() {
        CampResponse response = campService.transitionStatus(campId, CampStatus.IN_PROGRESS);
        assertEquals("IN_PROGRESS", response.getStatus());
    }

    @Test
    @Order(7)
    @DisplayName("Step 6: Transition IN_PROGRESS -> COMPLETED")
    void transitionToCompleted() {
        CampResponse response = campService.transitionStatus(campId, CampStatus.COMPLETED);
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    @Order(8)
    @DisplayName("Step 7: Transition COMPLETED -> ARCHIVED")
    void transitionToArchived() {
        CampResponse response = campService.transitionStatus(campId, CampStatus.ARCHIVED);
        assertEquals("ARCHIVED", response.getStatus());
    }

    @Test
    @Order(9)
    @DisplayName("Step 8: Cannot transition from ARCHIVED")
    void cannotTransitionFromArchived() {
        assertThrows(IllegalStateException.class, () ->
                campService.transitionStatus(campId, CampStatus.DRAFT));
    }

    @Test
    @Order(10)
    @DisplayName("Step 9: Create second camp for enrollment tests")
    void createSecondCampForEnrollment() {
        CreateCampRequest request = CreateCampRequest.builder()
                .name("Enrollment Test Camp")
                .description("Testing enrollment business rules")
                .campType(CampType.DEVELOPMENT)
                .ageCategory(AgeCategory.OPEN)
                .location("Abuja National Stadium")
                .country("Nigeria")
                .state("FCT")
                .startDate(LocalDate.now().plusDays(60))
                .endDate(LocalDate.now().plusDays(67))
                .registrationDeadline(LocalDate.now().plusDays(55))
                .capacity(2)
                .build();

        CampResponse response = campService.createCamp(request);
        campId = response.getId();
        assertEquals("DRAFT", response.getStatus());
        assertEquals(2, response.getCapacity());
    }

    @Test
    @Order(11)
    @DisplayName("Step 10: Enrollment fails when camp not REGISTRATION_OPEN")
    void enrollment_failsWhenCampNotOpen() {
        assertThrows(IllegalStateException.class, () ->
                enrollmentService.enrollAthlete(campId, athleteUserId));
    }

    @Test
    @Order(12)
    @DisplayName("Step 11: Camp CRUD - list all camps")
    void listAllCamps() {
        List<CampResponse> camps = campService.getAllCamps();
        assertTrue(camps.size() >= 2);
    }

    @Test
    @Order(13)
    @DisplayName("Step 12: Camp CRUD - get single camp")
    void getSingleCamp() {
        CampResponse camp = campService.getCamp(campId);
        assertNotNull(camp);
        assertEquals("Enrollment Test Camp", camp.getName());
    }

    @Test
    @Order(14)
    @DisplayName("Step 13: Camp cannot be updated after leaving DRAFT (regression)")
    void campUpdateOnlyInDraft() {
        // Transition to REGISTRATION_OPEN first
        campService.transitionStatus(campId, CampStatus.REGISTRATION_OPEN);

        // Should fail to update
        var updateRequest = com.feadev.eaids.camp.dto.request.UpdateCampRequest.builder()
                .name("Updated Name")
                .build();

        assertThrows(IllegalStateException.class, () ->
                campService.updateCamp(campId, updateRequest));
    }

    @Test
    @Order(15)
    @DisplayName("Step 14: Verify camp status filtering")
    void filterCampsByStatus() {
        List<CampResponse> openCamps = campService.getCampsByStatus(CampStatus.REGISTRATION_OPEN);
        assertFalse(openCamps.isEmpty());
        openCamps.forEach(c -> assertEquals("REGISTRATION_OPEN", c.getStatus()));
    }

    @Test
    @Order(16)
    @DisplayName("Step 15: Delete only allowed on DRAFT camps")
    void deleteOnlyDraftCamps() {
        // campId is now REGISTRATION_OPEN, should fail
        assertThrows(IllegalStateException.class, () ->
                campService.deleteCamp(campId));
    }
}
