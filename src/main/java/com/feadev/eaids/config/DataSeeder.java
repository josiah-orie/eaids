package com.feadev.eaids.config;

import com.feadev.eaids.adminprofile.model.AdminProfile;
import com.feadev.eaids.adminprofile.model.enums.AdminPermissionScope;
import com.feadev.eaids.adminprofile.repository.AdminProfileRepository;
import com.feadev.eaids.registrations.model.AthleteProfile;
import com.feadev.eaids.registrations.model.AthleteVideo;
import com.feadev.eaids.registrations.model.CoachProfile;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.*;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.AthleteVideoRepository;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final AthleteVideoRepository athleteVideoRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SEED_PASSWORD = "Password@123";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting seed data insertion...");

        String encodedPassword = passwordEncoder.encode(SEED_PASSWORD);

        seedAdminUser(encodedPassword);
        seedActiveAthlete(encodedPassword);
        seedActiveCoach(encodedPassword);
        seedPendingVerificationAthlete(encodedPassword);
        seedPendingConsentAthlete(encodedPassword);
        seedUnderReviewAthlete(encodedPassword);

        log.info("Seed data insertion completed.");
    }

    private void seedAdminUser(String encodedPassword) {
        String email = "admin@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Admin user already exists, skipping.");
            return;
        }

        User admin = User.builder()
                .email(email)
                .phoneNumber("+2348000000001")
                .password(encodedPassword)
                .role(UserRole.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        admin = userRepository.save(admin);

        AdminProfile adminProfile = AdminProfile.builder()
                .user(admin)
                .fullName("Super Administrator")
                .jobTitle("System Administrator")
                .department("Administration")
                .region("Nigeria")
                .countryManaged("Nigeria")
                .permissionsScope(AdminPermissionScope.SUPER_ADMIN_I)
                .build();

        adminProfileRepository.save(adminProfile);
        log.info("Seeded admin user with SUPER_ADMIN_I profile: {}", email);
    }

    private void seedActiveAthlete(String encodedPassword) {
        String email = "athlete@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Active athlete user already exists, skipping.");
            return;
        }

        User athleteUser = User.builder()
                .email(email)
                .phoneNumber("+2348000000002")
                .password(encodedPassword)
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        athleteUser = userRepository.save(athleteUser);

        AthleteProfile profile = AthleteProfile.builder()
                .user(athleteUser)
                .fullName("John Okafor")
                .age(22)
                .dateOfBirth(LocalDate.of(2002, 5, 15))
                .gender(Gender.MALE)
                .stateOfResidence("Lagos")
                .countryOfResidence("Nigeria")
                .nationality("Nigerian")
                .heightCm(new BigDecimal("185.5"))
                .weightKg(new BigDecimal("90.0"))
                .athleteInterest(AthleteInterest.TACKLE_FOOTBALL)
                .sportsBackgrounds(Set.of(
                        SportsBackground.ATHLETICS,
                        SportsBackground.BASKETBALL,
                        SportsBackground.RUGBY
                ))
                .build();

        profile = athleteProfileRepository.save(profile);

        AthleteVideo video1 = AthleteVideo.builder()
                .athleteProfile(profile)
                .url("https://www.youtube.com/watch?v=abc123")
                .videoType(VideoType.YOUTUBE)
                .createdDate(LocalDateTime.now())
                .build();

        AthleteVideo video2 = AthleteVideo.builder()
                .athleteProfile(profile)
                .url("https://www.hudl.com/video/3/12345/xyz")
                .videoType(VideoType.HUDL)
                .createdDate(LocalDateTime.now())
                .build();

        athleteVideoRepository.save(video1);
        athleteVideoRepository.save(video2);

        log.info("Seeded active athlete user with profile and videos: {}", email);
    }

    private void seedActiveCoach(String encodedPassword) {
        String email = "coach@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Active coach user already exists, skipping.");
            return;
        }

        User coachUser = User.builder()
                .email(email)
                .phoneNumber("+2348000000003")
                .password(encodedPassword)
                .role(UserRole.ROLE_COACH)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        coachUser = userRepository.save(coachUser);

        CoachProfile coachProfile = CoachProfile.builder()
                .user(coachUser)
                .fullName("Michael Adeyemi")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1985, 8, 20))
                .nationality("Nigerian")
                .stateOfResidence("Abuja")
                .phoneNumber("+2348000000003")
                .emailAddress("coach@eaids.com")
                .currentSport("American Football")
                .currentOrganization("Lagos Gridiron Academy")
                .currentPosition("Head Coach")
                .yearsOfExperience(10)
                .areaOfInterest(AreaOfInterest.COACH)
                .availability(Availability.FULL_TIME)
                .americanFootballExperience(AmericanFootballExperience.TACKLE_FOOTBALL)
                .involvementLevel(InvolvementLevel.NATIONAL)
                .coachingInterests(Set.of(
                        CoachingInterest.YOUTH_DEVELOPMENT,
                        CoachingInterest.OFFENSIVE_COACHING,
                        CoachingInterest.ATHLETE_DEVELOPMENT
                ))
                .build();

        coachProfileRepository.save(coachProfile);
        log.info("Seeded active coach user with profile: {}", email);
    }

    private void seedPendingVerificationAthlete(String encodedPassword) {
        String email = "pending@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Pending verification athlete already exists, skipping.");
            return;
        }

        User pendingUser = User.builder()
                .email(email)
                .phoneNumber("+2348000000004")
                .password(encodedPassword)
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        userRepository.save(pendingUser);
        log.info("Seeded PENDING_VERIFICATION athlete: {}", email);
    }

    private void seedPendingConsentAthlete(String encodedPassword) {
        String email = "consent@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Pending consent athlete already exists, skipping.");
            return;
        }

        User consentUser = User.builder()
                .email(email)
                .phoneNumber("+2348000000005")
                .password(encodedPassword)
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_CONSENT)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        consentUser = userRepository.save(consentUser);

        AthleteProfile profile = AthleteProfile.builder()
                .user(consentUser)
                .fullName("Chidi Nwosu")
                .age(16)
                .dateOfBirth(LocalDate.of(2008, 11, 3))
                .gender(Gender.MALE)
                .stateOfResidence("Rivers")
                .countryOfResidence("Nigeria")
                .nationality("Nigerian")
                .heightCm(new BigDecimal("175.0"))
                .weightKg(new BigDecimal("70.5"))
                .athleteInterest(AthleteInterest.BOTH)
                .sportsBackgrounds(Set.of(
                        SportsBackground.ATHLETICS,
                        SportsBackground.FOOTBALL_SOCCER
                ))
                .build();

        athleteProfileRepository.save(profile);
        log.info("Seeded PENDING_CONSENT athlete with profile: {}", email);
    }

    private void seedUnderReviewAthlete(String encodedPassword) {
        String email = "review@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Under review athlete already exists, skipping.");
            return;
        }

        User reviewUser = User.builder()
                .email(email)
                .phoneNumber("+2348000000006")
                .password(encodedPassword)
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.UNDER_REVIEW)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        reviewUser = userRepository.save(reviewUser);

        AthleteProfile profile = AthleteProfile.builder()
                .user(reviewUser)
                .fullName("Emeka Obiora")
                .age(20)
                .dateOfBirth(LocalDate.of(2004, 3, 22))
                .gender(Gender.MALE)
                .stateOfResidence("Enugu")
                .countryOfResidence("Nigeria")
                .nationality("Nigerian")
                .heightCm(new BigDecimal("190.0"))
                .weightKg(new BigDecimal("95.0"))
                .athleteInterest(AthleteInterest.FLAG_FOOTBALL)
                .sportsBackgrounds(Set.of(
                        SportsBackground.WRESTLING,
                        SportsBackground.RUGBY
                ))
                .build();

        athleteProfileRepository.save(profile);
        log.info("Seeded UNDER_REVIEW athlete with profile: {}", email);
    }
}
