package com.feadev.eaids.config;

import com.feadev.eaids.adminprofile.model.AdminProfile;
import com.feadev.eaids.adminprofile.model.enums.AdminPermissionScope;
import com.feadev.eaids.adminprofile.repository.AdminProfileRepository;
import com.feadev.eaids.analytics.benchmarking.model.BenchmarkComparison;
import com.feadev.eaids.analytics.benchmarking.model.BenchmarkProfile;
import com.feadev.eaids.analytics.benchmarking.model.enums.BenchmarkType;
import com.feadev.eaids.analytics.benchmarking.repository.BenchmarkComparisonRepository;
import com.feadev.eaids.analytics.benchmarking.repository.BenchmarkProfileRepository;
import com.feadev.eaids.analytics.injuryrisk.model.InjuryRiskAssessment;
import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
import com.feadev.eaids.analytics.injuryrisk.repository.InjuryRiskRepository;
import com.feadev.eaids.analytics.prediction.model.PotentialPrediction;
import com.feadev.eaids.analytics.prediction.model.enums.PredictionTier;
import com.feadev.eaids.analytics.prediction.repository.PotentialPredictionRepository;
import com.feadev.eaids.assessment.model.CampStage;
import com.feadev.eaids.assessment.repository.CampStageRepository;
import com.feadev.eaids.attendance.model.AttendanceRecord;
import com.feadev.eaids.attendance.model.enums.AttendanceStatus;
import com.feadev.eaids.attendance.model.enums.SessionType;
import com.feadev.eaids.attendance.repository.AttendanceRecordRepository;
import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.model.enums.*;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.development.model.*;
import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;
import com.feadev.eaids.development.model.enums.GoalStatus;
import com.feadev.eaids.development.repository.DevelopmentPlanRepository;
import com.feadev.eaids.development.repository.TrainingRecommendationRepository;
import com.feadev.eaids.discipline.model.DisciplineRecord;
import com.feadev.eaids.discipline.model.enums.InfractionType;
import com.feadev.eaids.discipline.repository.DisciplineRecordRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.evaluation.model.CampEvaluator;
import com.feadev.eaids.evaluation.model.CoachEvaluation;
import com.feadev.eaids.evaluation.repository.CampEvaluatorRepository;
import com.feadev.eaids.evaluation.repository.CoachEvaluationRepository;
import com.feadev.eaids.matchstats.model.MatchStatDefinition;
import com.feadev.eaids.matchstats.model.MatchStatEntry;
import com.feadev.eaids.matchstats.model.enums.PositionGroup;
import com.feadev.eaids.matchstats.repository.MatchStatDefinitionRepository;
import com.feadev.eaids.matchstats.repository.MatchStatEntryRepository;
import com.feadev.eaids.nationalranking.model.NationalRanking;
import com.feadev.eaids.nationalranking.model.NationalRankingConfig;
import com.feadev.eaids.nationalranking.model.enums.RankingScope;
import com.feadev.eaids.nationalranking.repository.NationalRankingConfigRepository;
import com.feadev.eaids.nationalranking.repository.NationalRankingRepository;
import com.feadev.eaids.physical.model.AssessmentMetric;
import com.feadev.eaids.physical.model.AthleteMeasurement;
import com.feadev.eaids.physical.model.MetricBenchmark;
import com.feadev.eaids.physical.model.enums.MeasurementUnit;
import com.feadev.eaids.physical.model.enums.MetricCategory;
import com.feadev.eaids.physical.repository.AssessmentMetricRepository;
import com.feadev.eaids.physical.repository.AthleteMeasurementRepository;
import com.feadev.eaids.physical.repository.MetricBenchmarkRepository;
import com.feadev.eaids.ranking.model.AthleteRanking;
import com.feadev.eaids.ranking.model.enums.RankingType;
import com.feadev.eaids.ranking.repository.AthleteRankingRepository;
import com.feadev.eaids.registrations.model.AthleteProfile;
import com.feadev.eaids.registrations.model.AthleteVideo;
import com.feadev.eaids.registrations.model.CoachProfile;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.*;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.AthleteVideoRepository;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.model.CampWeightProfile;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import com.feadev.eaids.scoring.repository.CampWeightProfileRepository;
import com.feadev.eaids.scouting.model.InterviewRequest;
import com.feadev.eaids.scouting.model.ScoutAccessScope;
import com.feadev.eaids.scouting.model.ScoutBookmark;
import com.feadev.eaids.scouting.model.ScoutProfile;
import com.feadev.eaids.scouting.model.enums.InterviewStatus;
import com.feadev.eaids.scouting.model.enums.OrganizationType;
import com.feadev.eaids.scouting.model.enums.ScopeType;
import com.feadev.eaids.scouting.repository.InterviewRequestRepository;
import com.feadev.eaids.scouting.repository.ScoutAccessScopeRepository;
import com.feadev.eaids.scouting.repository.ScoutBookmarkRepository;
import com.feadev.eaids.scouting.repository.ScoutProfileRepository;
import com.feadev.eaids.selection.model.SelectionDecision;
import com.feadev.eaids.selection.model.enums.ApprovalStatus;
import com.feadev.eaids.selection.model.enums.SelectionTier;
import com.feadev.eaids.selection.repository.SelectionDecisionRepository;
import com.feadev.eaids.tactical.model.TacticalMetric;
import com.feadev.eaids.tactical.model.TacticalScore;
import com.feadev.eaids.tactical.repository.TacticalMetricRepository;
import com.feadev.eaids.tactical.repository.TacticalScoreRepository;
import com.feadev.eaids.technical.model.TechnicalMetric;
import com.feadev.eaids.technical.model.TechnicalScore;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.technical.repository.TechnicalMetricRepository;
import com.feadev.eaids.technical.repository.TechnicalScoreRepository;
import com.feadev.eaids.trainingload.model.TrainingLoadRecord;
import com.feadev.eaids.trainingload.model.enums.TrainingIntensity;
import com.feadev.eaids.trainingload.model.enums.TrainingSessionType;
import com.feadev.eaids.trainingload.repository.TrainingLoadRepository;
import com.feadev.eaids.event.model.Event;
import com.feadev.eaids.event.model.EventRegistration;
import com.feadev.eaids.event.model.enums.EventStatus;
import com.feadev.eaids.event.model.enums.RegistrationStatus;
import com.feadev.eaids.event.repository.EventRegistrationRepository;
import com.feadev.eaids.event.repository.EventRepository;
import com.feadev.eaids.videolibrary.model.VideoLibraryItem;
import com.feadev.eaids.videolibrary.model.enums.VideoCategory;
import com.feadev.eaids.videolibrary.repository.VideoLibraryRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final CampRepository campRepository;
    private final CampEnrollmentRepository campEnrollmentRepository;
    private final CampStageRepository campStageRepository;
    private final CampEvaluatorRepository campEvaluatorRepository;
    private final CoachEvaluationRepository coachEvaluationRepository;
    private final AssessmentMetricRepository assessmentMetricRepository;
    private final MetricBenchmarkRepository metricBenchmarkRepository;
    private final AthleteMeasurementRepository athleteMeasurementRepository;
    private final TechnicalMetricRepository technicalMetricRepository;
    private final TechnicalScoreRepository technicalScoreRepository;
    private final TacticalMetricRepository tacticalMetricRepository;
    private final TacticalScoreRepository tacticalScoreRepository;
    private final MatchStatDefinitionRepository matchStatDefinitionRepository;
    private final MatchStatEntryRepository matchStatEntryRepository;
    private final DisciplineRecordRepository disciplineRecordRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final CampWeightProfileRepository campWeightProfileRepository;
    private final AthleteScoreRepository athleteScoreRepository;
    private final AthleteRankingRepository athleteRankingRepository;
    private final SelectionDecisionRepository selectionDecisionRepository;
    private final DevelopmentPlanRepository developmentPlanRepository;
    private final TrainingRecommendationRepository trainingRecommendationRepository;
    private final TrainingLoadRepository trainingLoadRepository;
    private final NationalRankingConfigRepository nationalRankingConfigRepository;
    private final NationalRankingRepository nationalRankingRepository;
    private final BenchmarkProfileRepository benchmarkProfileRepository;
    private final BenchmarkComparisonRepository benchmarkComparisonRepository;
    private final PotentialPredictionRepository potentialPredictionRepository;
    private final InjuryRiskRepository injuryRiskRepository;
    private final ScoutProfileRepository scoutProfileRepository;
    private final ScoutAccessScopeRepository scoutAccessScopeRepository;
    private final ScoutBookmarkRepository scoutBookmarkRepository;
    private final InterviewRequestRepository interviewRequestRepository;
    private final VideoLibraryRepository videoLibraryRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    private static final String SEED_PASSWORD = "Password@123";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting seed data insertion...");

        String encodedPassword = passwordEncoder.encode(SEED_PASSWORD);

        // Phase 1: Core users
        User adminUser = seedAdminUser(encodedPassword);
        User athleteUser = seedActiveAthlete(encodedPassword);
        User coachUser = seedActiveCoach(encodedPassword);
        User scoutUser = seedScoutUser(encodedPassword);
        User athlete2User = seedSecondAthlete(encodedPassword);
        seedPendingVerificationAthlete(encodedPassword);
        seedPendingConsentAthlete(encodedPassword);
        seedUnderReviewAthlete(encodedPassword);

        // Phase 2: Camp & Enrollment
        Camp camp = seedCamp();
        CampEnrollment enrollment1 = seedEnrollment(camp, athleteUser);
        CampEnrollment enrollment2 = seedEnrollment2(camp, athlete2User);
        seedCampStages(camp);
        CampEvaluator evaluator = seedCampEvaluator(camp, coachUser);

        // Phase 3: Physical Assessment
        AssessmentMetric speedMetric = seedAssessmentMetrics(camp);
        seedMetricBenchmarks(speedMetric);
        seedAthleteMeasurements(enrollment1, enrollment2, speedMetric);

        // Phase 4: Technical & Tactical
        TechnicalMetric techMetric = seedTechnicalMetrics(camp);
        seedTechnicalScores(enrollment1, enrollment2, techMetric, coachUser);
        TacticalMetric tactMetric = seedTacticalMetrics(camp);
        seedTacticalScores(enrollment1, enrollment2, tactMetric, coachUser);

        // Phase 5: Match Stats
        MatchStatDefinition matchStatDef = seedMatchStatDefinitions();
        seedMatchStatEntries(enrollment1, enrollment2, matchStatDef, camp, coachUser);

        // Phase 6: Attendance & Discipline
        seedAttendanceRecords(enrollment1, enrollment2, camp);
        seedDisciplineRecords(enrollment1, camp);

        // Phase 7: Coach Evaluations
        seedCoachEvaluations(enrollment1, enrollment2, evaluator);

        // Phase 8: Scoring & Ranking
        seedCampWeightProfiles(camp);
        seedAthleteScores(enrollment1, enrollment2, camp);
        seedAthleteRankings(enrollment1, enrollment2, camp);

        // Phase 9: Selection
        seedSelectionDecisions(enrollment1, enrollment2, camp, adminUser);

        // Phase 10: Development
        seedDevelopmentPlans(athleteUser, camp, coachUser);
        seedTrainingRecommendations();

        // Phase 11: Training Load
        seedTrainingLoadRecords(enrollment1, camp, coachUser);

        // Phase 12: National Rankings
        seedNationalRankings(athleteUser, athlete2User);

        // Phase 13: Analytics
        seedBenchmarkProfiles(adminUser);
        seedBenchmarkComparisons(enrollment1);
        seedPotentialPredictions(enrollment1, enrollment2);
        seedInjuryRiskAssessments(athleteUser, athlete2User, enrollment1, enrollment2);

        // Phase 14: Scouting
        seedScoutProfile(scoutUser);
        seedScoutAccessScopes(scoutUser, adminUser);
        seedScoutBookmarks(scoutUser, athleteUser, athlete2User);
        seedInterviewRequests(scoutUser, athleteUser, adminUser);

        // Phase 15: Video Library
        seedVideoLibraryItems(athleteUser, camp, coachUser);

        // Phase 16: Events
        seedEvents(athleteUser, coachUser, scoutUser);

        log.info("Seed data insertion completed.");
    }

    // ======================== USERS ========================

    private User seedAdminUser(String encodedPassword) {
        String email = "admin@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Admin user already exists, skipping.");
            return userRepository.findByEmail(email).orElse(null);
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
        log.info("Seeded admin user: {}", email);
        return admin;
    }

    private User seedActiveAthlete(String encodedPassword) {
        String email = "athlete@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Active athlete already exists, skipping.");
            return userRepository.findByEmail(email).orElse(null);
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
                .sportsBackgrounds(Set.of(SportsBackground.ATHLETICS, SportsBackground.BASKETBALL, SportsBackground.RUGBY))
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

        log.info("Seeded active athlete: {}", email);
        return athleteUser;
    }

    private User seedSecondAthlete(String encodedPassword) {
        String email = "athlete2@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Second athlete already exists, skipping.");
            return userRepository.findByEmail(email).orElse(null);
        }

        User athleteUser = User.builder()
                .email(email)
                .phoneNumber("+2348000000007")
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
                .fullName("Amara Eze")
                .age(19)
                .dateOfBirth(LocalDate.of(2005, 9, 10))
                .gender(Gender.FEMALE)
                .stateOfResidence("Abuja")
                .countryOfResidence("Nigeria")
                .nationality("Nigerian")
                .heightCm(new BigDecimal("170.0"))
                .weightKg(new BigDecimal("68.5"))
                .athleteInterest(AthleteInterest.FLAG_FOOTBALL)
                .sportsBackgrounds(Set.of(SportsBackground.ATHLETICS, SportsBackground.FOOTBALL_SOCCER))
                .build();
        athleteProfileRepository.save(profile);

        log.info("Seeded second athlete: {}", email);
        return athleteUser;
    }

    private User seedActiveCoach(String encodedPassword) {
        String email = "coach@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Active coach already exists, skipping.");
            return userRepository.findByEmail(email).orElse(null);
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

        log.info("Seeded active coach: {}", email);
        return coachUser;
    }

    private User seedScoutUser(String encodedPassword) {
        String email = "scout@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Scout user already exists, skipping.");
            return userRepository.findByEmail(email).orElse(null);
        }

        User scoutUser = User.builder()
                .email(email)
                .phoneNumber("+2348000000008")
                .password(encodedPassword)
                .role(UserRole.ROLE_SCOUT)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();
        scoutUser = userRepository.save(scoutUser);
        log.info("Seeded scout user: {}", email);
        return scoutUser;
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
                .sportsBackgrounds(Set.of(SportsBackground.ATHLETICS, SportsBackground.FOOTBALL_SOCCER))
                .build();
        athleteProfileRepository.save(profile);
        log.info("Seeded PENDING_CONSENT athlete: {}", email);
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
                .sportsBackgrounds(Set.of(SportsBackground.WRESTLING, SportsBackground.RUGBY))
                .build();
        athleteProfileRepository.save(profile);
        log.info("Seeded UNDER_REVIEW athlete: {}", email);
    }

    // ======================== CAMP & ENROLLMENT ========================

    private Camp seedCamp() {
        if (campRepository.count() > 0) {
            log.info("Camp already exists, skipping.");
            return campRepository.findAll().getFirst();
        }

        Camp camp = Camp.builder()
                .name("Lagos Elite Identification Camp 2026")
                .description("Premier identification camp for elite athletes in Lagos region")
                .campType(CampType.IDENTIFICATION)
                .status(CampStatus.IN_PROGRESS)
                .ageCategory(AgeCategory.UNDER_21)
                .location("Teslim Balogun Stadium, Lagos")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 14))
                .registrationDeadline(LocalDate.of(2026, 6, 20))
                .capacity(100)
                .currentEnrollment(2)
                .aggregationStrategy(AggregationStrategy.WEIGHTED_AVERAGE)
                .build();
        camp = campRepository.save(camp);
        log.info("Seeded camp: {}", camp.getName());
        return camp;
    }

    private CampEnrollment seedEnrollment(Camp camp, User athleteUser) {
        if (campEnrollmentRepository.count() > 0) {
            return campEnrollmentRepository.findAll().getFirst();
        }

        CampEnrollment enrollment = CampEnrollment.builder()
                .camp(camp)
                .user(athleteUser)
                .status(EnrollmentStatus.CONFIRMED)
                .enrollmentNumber(1)
                .build();
        enrollment = campEnrollmentRepository.save(enrollment);
        log.info("Seeded enrollment for athlete: {}", athleteUser.getEmail());
        return enrollment;
    }

    private CampEnrollment seedEnrollment2(Camp camp, User athlete2User) {
        if (campEnrollmentRepository.count() > 1) {
            return campEnrollmentRepository.findAll().get(1);
        }

        CampEnrollment enrollment = CampEnrollment.builder()
                .camp(camp)
                .user(athlete2User)
                .status(EnrollmentStatus.CONFIRMED)
                .enrollmentNumber(2)
                .build();
        enrollment = campEnrollmentRepository.save(enrollment);
        log.info("Seeded enrollment for athlete2: {}", athlete2User.getEmail());
        return enrollment;
    }

    private void seedCampStages(Camp camp) {
        if (campStageRepository.count() > 0) {
            log.info("Camp stages already exist, skipping.");
            return;
        }

        List<CampStage> stages = List.of(
                CampStage.builder()
                        .camp(camp)
                        .stageName("Physical Assessment")
                        .stageOrder(1)
                        .description("Physical fitness testing: 40yd dash, vertical jump, agility drills")
                        .durationDays(3)
                        .scoreWeight(new BigDecimal("30.00"))
                        .isActive(true)
                        .build(),
                CampStage.builder()
                        .camp(camp)
                        .stageName("Technical Evaluation")
                        .stageOrder(2)
                        .description("Position-specific technical skills assessment")
                        .durationDays(4)
                        .scoreWeight(new BigDecimal("35.00"))
                        .isActive(true)
                        .build(),
                CampStage.builder()
                        .camp(camp)
                        .stageName("Tactical & Game Situations")
                        .stageOrder(3)
                        .description("Tactical awareness and game-situation evaluation")
                        .durationDays(3)
                        .scoreWeight(new BigDecimal("20.00"))
                        .isActive(true)
                        .build(),
                CampStage.builder()
                        .camp(camp)
                        .stageName("Final Selection")
                        .stageOrder(4)
                        .description("Final scrimmages and selection committee review")
                        .durationDays(4)
                        .scoreWeight(new BigDecimal("15.00"))
                        .isActive(true)
                        .build()
        );
        campStageRepository.saveAll(stages);
        log.info("Seeded {} camp stages", stages.size());
    }

    private CampEvaluator seedCampEvaluator(Camp camp, User coachUser) {
        if (campEvaluatorRepository.count() > 0) {
            return campEvaluatorRepository.findAll().getFirst();
        }

        CampEvaluator evaluator = CampEvaluator.builder()
                .camp(camp)
                .user(coachUser)
                .role(UserRole.ROLE_COACH)
                .isLead(true)
                .build();
        evaluator = campEvaluatorRepository.save(evaluator);
        log.info("Seeded camp evaluator: {}", coachUser.getEmail());
        return evaluator;
    }

    // ======================== PHYSICAL ASSESSMENT ========================

    private AssessmentMetric seedAssessmentMetrics(Camp camp) {
        if (assessmentMetricRepository.count() > 0) {
            log.info("Assessment metrics already exist, skipping.");
            return assessmentMetricRepository.findAll().getFirst();
        }

        AssessmentMetric speed40yd = AssessmentMetric.builder()
                .camp(camp)
                .category(MetricCategory.SPEED)
                .metricName("40-Yard Dash")
                .unit(MeasurementUnit.SECONDS)
                .description("Timed 40-yard straight-line sprint")
                .isActive(true)
                .lowerIsBetter(true)
                .build();
        speed40yd = assessmentMetricRepository.save(speed40yd);

        assessmentMetricRepository.save(AssessmentMetric.builder()
                .camp(camp)
                .category(MetricCategory.AGILITY)
                .metricName("Pro Agility Shuttle")
                .unit(MeasurementUnit.SECONDS)
                .description("5-10-5 shuttle run for lateral quickness")
                .isActive(true)
                .lowerIsBetter(true)
                .build());

        assessmentMetricRepository.save(AssessmentMetric.builder()
                .camp(camp)
                .category(MetricCategory.STRENGTH)
                .metricName("Bench Press (Reps)")
                .unit(MeasurementUnit.REPETITIONS)
                .description("Maximum repetitions of 225 lbs bench press")
                .isActive(true)
                .lowerIsBetter(false)
                .build());

        assessmentMetricRepository.save(AssessmentMetric.builder()
                .camp(camp)
                .category(MetricCategory.ENDURANCE)
                .metricName("Beep Test (Shuttle Runs)")
                .unit(MeasurementUnit.METERS)
                .description("Multi-stage fitness test for aerobic endurance")
                .isActive(true)
                .lowerIsBetter(false)
                .build());

        assessmentMetricRepository.save(AssessmentMetric.builder()
                .camp(camp)
                .category(MetricCategory.FLEXIBILITY)
                .metricName("Sit and Reach")
                .unit(MeasurementUnit.CENTIMETERS)
                .description("Flexibility measurement via sit-and-reach test")
                .isActive(true)
                .lowerIsBetter(false)
                .build());

        log.info("Seeded 5 assessment metrics");
        return speed40yd;
    }

    private void seedMetricBenchmarks(AssessmentMetric speedMetric) {
        if (metricBenchmarkRepository.count() > 0) {
            log.info("Metric benchmarks already exist, skipping.");
            return;
        }

        List<MetricBenchmark> benchmarks = List.of(
                MetricBenchmark.builder()
                        .metric(speedMetric)
                        .minValue(new BigDecimal("4.200"))
                        .maxValue(new BigDecimal("4.500"))
                        .score(10)
                        .ageCategory(AgeCategory.UNDER_21)
                        .gender(Gender.MALE)
                        .build(),
                MetricBenchmark.builder()
                        .metric(speedMetric)
                        .minValue(new BigDecimal("4.500"))
                        .maxValue(new BigDecimal("4.800"))
                        .score(8)
                        .ageCategory(AgeCategory.UNDER_21)
                        .gender(Gender.MALE)
                        .build(),
                MetricBenchmark.builder()
                        .metric(speedMetric)
                        .minValue(new BigDecimal("4.800"))
                        .maxValue(new BigDecimal("5.200"))
                        .score(6)
                        .ageCategory(AgeCategory.UNDER_21)
                        .gender(Gender.MALE)
                        .build(),
                MetricBenchmark.builder()
                        .metric(speedMetric)
                        .minValue(new BigDecimal("4.400"))
                        .maxValue(new BigDecimal("4.800"))
                        .score(10)
                        .ageCategory(AgeCategory.UNDER_21)
                        .gender(Gender.FEMALE)
                        .build()
        );
        metricBenchmarkRepository.saveAll(benchmarks);
        log.info("Seeded {} metric benchmarks", benchmarks.size());
    }

    private void seedAthleteMeasurements(CampEnrollment enrollment1, CampEnrollment enrollment2, AssessmentMetric speedMetric) {
        if (athleteMeasurementRepository.count() > 0) {
            log.info("Athlete measurements already exist, skipping.");
            return;
        }

        List<AthleteMeasurement> measurements = List.of(
                AthleteMeasurement.builder()
                        .enrollment(enrollment1)
                        .metric(speedMetric)
                        .rawValue(new BigDecimal("4.550"))
                        .calculatedScore(8)
                        .measuredDate(LocalDate.of(2026, 7, 2))
                        .recordedBy("coach@eaids.com")
                        .build(),
                AthleteMeasurement.builder()
                        .enrollment(enrollment2)
                        .metric(speedMetric)
                        .rawValue(new BigDecimal("4.720"))
                        .calculatedScore(7)
                        .measuredDate(LocalDate.of(2026, 7, 2))
                        .recordedBy("coach@eaids.com")
                        .build()
        );
        athleteMeasurementRepository.saveAll(measurements);
        log.info("Seeded {} athlete measurements", measurements.size());
    }

    // ======================== TECHNICAL & TACTICAL ========================

    private TechnicalMetric seedTechnicalMetrics(Camp camp) {
        if (technicalMetricRepository.count() > 0) {
            log.info("Technical metrics already exist, skipping.");
            return technicalMetricRepository.findAll().getFirst();
        }

        TechnicalMetric routeRunning = TechnicalMetric.builder()
                .camp(camp)
                .position(FootballPosition.WIDE_RECEIVER)
                .metricName("Route Running Precision")
                .maxScore(10)
                .weight(new BigDecimal("30.00"))
                .build();
        routeRunning = technicalMetricRepository.save(routeRunning);

        technicalMetricRepository.save(TechnicalMetric.builder()
                .camp(camp)
                .position(FootballPosition.WIDE_RECEIVER)
                .metricName("Catching Under Pressure")
                .maxScore(10)
                .weight(new BigDecimal("25.00"))
                .build());

        technicalMetricRepository.save(TechnicalMetric.builder()
                .camp(camp)
                .position(FootballPosition.QUARTERBACK)
                .metricName("Throw Accuracy (Short)")
                .maxScore(10)
                .weight(new BigDecimal("35.00"))
                .build());

        technicalMetricRepository.save(TechnicalMetric.builder()
                .camp(camp)
                .position(FootballPosition.QUARTERBACK)
                .metricName("Throw Accuracy (Deep)")
                .maxScore(10)
                .weight(new BigDecimal("30.00"))
                .build());

        log.info("Seeded 4 technical metrics");
        return routeRunning;
    }

    private void seedTechnicalScores(CampEnrollment enrollment1, CampEnrollment enrollment2,
                                     TechnicalMetric techMetric, User coachUser) {
        if (technicalScoreRepository.count() > 0) {
            log.info("Technical scores already exist, skipping.");
            return;
        }

        List<TechnicalScore> scores = List.of(
                TechnicalScore.builder()
                        .enrollment(enrollment1)
                        .metric(techMetric)
                        .score(8)
                        .evaluatorId(coachUser.getId())
                        .build(),
                TechnicalScore.builder()
                        .enrollment(enrollment2)
                        .metric(techMetric)
                        .score(7)
                        .evaluatorId(coachUser.getId())
                        .build()
        );
        technicalScoreRepository.saveAll(scores);
        log.info("Seeded {} technical scores", scores.size());
    }

    private TacticalMetric seedTacticalMetrics(Camp camp) {
        if (tacticalMetricRepository.count() > 0) {
            log.info("Tactical metrics already exist, skipping.");
            return tacticalMetricRepository.findAll().getFirst();
        }

        TacticalMetric playRecognition = TacticalMetric.builder()
                .camp(camp)
                .metricName("Play Recognition")
                .maxScore(10)
                .weight(new BigDecimal("30.00"))
                .description("Ability to read and react to offensive/defensive formations")
                .build();
        playRecognition = tacticalMetricRepository.save(playRecognition);

        tacticalMetricRepository.save(TacticalMetric.builder()
                .camp(camp)
                .metricName("Field Awareness")
                .maxScore(10)
                .weight(new BigDecimal("25.00"))
                .description("Spatial awareness and positioning during live plays")
                .build());

        tacticalMetricRepository.save(TacticalMetric.builder()
                .camp(camp)
                .metricName("Decision Making Under Pressure")
                .maxScore(10)
                .weight(new BigDecimal("25.00"))
                .description("Quality of decisions in high-pressure game situations")
                .build());

        tacticalMetricRepository.save(TacticalMetric.builder()
                .camp(camp)
                .metricName("Communication & Leadership")
                .maxScore(10)
                .weight(new BigDecimal("20.00"))
                .description("Vocal leadership and on-field communication")
                .build());

        log.info("Seeded 4 tactical metrics");
        return playRecognition;
    }

    private void seedTacticalScores(CampEnrollment enrollment1, CampEnrollment enrollment2,
                                    TacticalMetric tactMetric, User coachUser) {
        if (tacticalScoreRepository.count() > 0) {
            log.info("Tactical scores already exist, skipping.");
            return;
        }

        List<TacticalScore> scores = List.of(
                TacticalScore.builder()
                        .enrollment(enrollment1)
                        .metric(tactMetric)
                        .score(9)
                        .evaluatorId(coachUser.getId())
                        .build(),
                TacticalScore.builder()
                        .enrollment(enrollment2)
                        .metric(tactMetric)
                        .score(7)
                        .evaluatorId(coachUser.getId())
                        .build()
        );
        tacticalScoreRepository.saveAll(scores);
        log.info("Seeded {} tactical scores", scores.size());
    }

    // ======================== MATCH STATS ========================

    private MatchStatDefinition seedMatchStatDefinitions() {
        if (matchStatDefinitionRepository.count() > 0) {
            log.info("Match stat definitions already exist, skipping.");
            return matchStatDefinitionRepository.findAll().getFirst();
        }

        MatchStatDefinition passingYards = MatchStatDefinition.builder()
                .positionGroup(PositionGroup.QUARTERBACK)
                .statName("Passing Yards")
                .unit("yards")
                .displayOrder(1)
                .isActive(true)
                .build();
        passingYards = matchStatDefinitionRepository.save(passingYards);

        matchStatDefinitionRepository.save(MatchStatDefinition.builder()
                .positionGroup(PositionGroup.QUARTERBACK)
                .statName("Completions")
                .unit("count")
                .displayOrder(2)
                .isActive(true)
                .build());

        matchStatDefinitionRepository.save(MatchStatDefinition.builder()
                .positionGroup(PositionGroup.RECEIVER)
                .statName("Receiving Yards")
                .unit("yards")
                .displayOrder(1)
                .isActive(true)
                .build());

        matchStatDefinitionRepository.save(MatchStatDefinition.builder()
                .positionGroup(PositionGroup.DEFENSE)
                .statName("Tackles")
                .unit("count")
                .displayOrder(1)
                .isActive(true)
                .build());

        matchStatDefinitionRepository.save(MatchStatDefinition.builder()
                .positionGroup(PositionGroup.SPECIAL_TEAMS)
                .statName("Return Yards")
                .unit("yards")
                .displayOrder(1)
                .isActive(true)
                .build());

        log.info("Seeded 5 match stat definitions");
        return passingYards;
    }

    private void seedMatchStatEntries(CampEnrollment enrollment1, CampEnrollment enrollment2,
                                      MatchStatDefinition definition, Camp camp, User coachUser) {
        if (matchStatEntryRepository.count() > 0) {
            log.info("Match stat entries already exist, skipping.");
            return;
        }

        List<MatchStatEntry> entries = List.of(
                MatchStatEntry.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .definition(definition)
                        .value(new BigDecimal("185.00"))
                        .matchDate(LocalDate.of(2026, 7, 8))
                        .opponent("Abuja Warriors")
                        .recordedBy(coachUser.getId())
                        .build(),
                MatchStatEntry.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .definition(definition)
                        .value(new BigDecimal("220.00"))
                        .matchDate(LocalDate.of(2026, 7, 10))
                        .opponent("Port Harcourt Eagles")
                        .recordedBy(coachUser.getId())
                        .build(),
                MatchStatEntry.builder()
                        .enrollment(enrollment2)
                        .campId(camp.getId())
                        .definition(definition)
                        .value(new BigDecimal("150.00"))
                        .matchDate(LocalDate.of(2026, 7, 8))
                        .opponent("Abuja Warriors")
                        .recordedBy(coachUser.getId())
                        .build()
        );
        matchStatEntryRepository.saveAll(entries);
        log.info("Seeded {} match stat entries", entries.size());
    }

    // ======================== ATTENDANCE & DISCIPLINE ========================

    private void seedAttendanceRecords(CampEnrollment enrollment1, CampEnrollment enrollment2, Camp camp) {
        if (attendanceRecordRepository.count() > 0) {
            log.info("Attendance records already exist, skipping.");
            return;
        }

        List<AttendanceRecord> records = List.of(
                AttendanceRecord.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .attendanceDate(LocalDate.of(2026, 7, 1))
                        .sessionType(SessionType.MORNING)
                        .status(AttendanceStatus.PRESENT)
                        .score(new BigDecimal("10.00"))
                        .recordedBy("coach@eaids.com")
                        .build(),
                AttendanceRecord.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .attendanceDate(LocalDate.of(2026, 7, 1))
                        .sessionType(SessionType.AFTERNOON)
                        .status(AttendanceStatus.PRESENT)
                        .score(new BigDecimal("10.00"))
                        .recordedBy("coach@eaids.com")
                        .build(),
                AttendanceRecord.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .attendanceDate(LocalDate.of(2026, 7, 2))
                        .sessionType(SessionType.MORNING)
                        .status(AttendanceStatus.LATE)
                        .score(new BigDecimal("7.00"))
                        .recordedBy("coach@eaids.com")
                        .build(),
                AttendanceRecord.builder()
                        .enrollment(enrollment2)
                        .campId(camp.getId())
                        .attendanceDate(LocalDate.of(2026, 7, 1))
                        .sessionType(SessionType.MORNING)
                        .status(AttendanceStatus.PRESENT)
                        .score(new BigDecimal("10.00"))
                        .recordedBy("coach@eaids.com")
                        .build(),
                AttendanceRecord.builder()
                        .enrollment(enrollment2)
                        .campId(camp.getId())
                        .attendanceDate(LocalDate.of(2026, 7, 1))
                        .sessionType(SessionType.AFTERNOON)
                        .status(AttendanceStatus.EXCUSED)
                        .score(new BigDecimal("5.00"))
                        .recordedBy("coach@eaids.com")
                        .build(),
                AttendanceRecord.builder()
                        .enrollment(enrollment2)
                        .campId(camp.getId())
                        .attendanceDate(LocalDate.of(2026, 7, 2))
                        .sessionType(SessionType.MORNING)
                        .status(AttendanceStatus.PRESENT)
                        .score(new BigDecimal("10.00"))
                        .recordedBy("coach@eaids.com")
                        .build()
        );
        attendanceRecordRepository.saveAll(records);
        log.info("Seeded {} attendance records", records.size());
    }

    private void seedDisciplineRecords(CampEnrollment enrollment1, Camp camp) {
        if (disciplineRecordRepository.count() > 0) {
            log.info("Discipline records already exist, skipping.");
            return;
        }

        DisciplineRecord record = DisciplineRecord.builder()
                .enrollment(enrollment1)
                .campId(camp.getId())
                .infractionType(InfractionType.WARNING)
                .penaltyPoints(1)
                .description("Late arrival to morning session without prior notice")
                .infractionDate(LocalDate.of(2026, 7, 2))
                .recordedBy("coach@eaids.com")
                .build();
        disciplineRecordRepository.save(record);
        log.info("Seeded 1 discipline record");
    }

    // ======================== COACH EVALUATIONS ========================

    private void seedCoachEvaluations(CampEnrollment enrollment1, CampEnrollment enrollment2, CampEvaluator evaluator) {
        if (coachEvaluationRepository.count() > 0) {
            log.info("Coach evaluations already exist, skipping.");
            return;
        }

        List<CoachEvaluation> evaluations = List.of(
                CoachEvaluation.builder()
                        .enrollment(enrollment1)
                        .evaluator(evaluator)
                        .technicalScore(new BigDecimal("8.50"))
                        .tacticalScore(new BigDecimal("9.00"))
                        .leadershipScore(new BigDecimal("7.50"))
                        .characterScore(new BigDecimal("8.00"))
                        .recommendation("Strong candidate for elite tier. Excellent route running and game awareness.")
                        .comments("John shows natural leadership qualities and consistent improvement in drills.")
                        .build(),
                CoachEvaluation.builder()
                        .enrollment(enrollment2)
                        .evaluator(evaluator)
                        .technicalScore(new BigDecimal("7.00"))
                        .tacticalScore(new BigDecimal("7.50"))
                        .leadershipScore(new BigDecimal("8.00"))
                        .characterScore(new BigDecimal("9.00"))
                        .recommendation("Developing talent with strong character. Consider for development program.")
                        .comments("Amara has excellent attitude and coachability. Needs more game experience.")
                        .build()
        );
        coachEvaluationRepository.saveAll(evaluations);
        log.info("Seeded {} coach evaluations", evaluations.size());
    }

    // ======================== SCORING & RANKING ========================

    private void seedCampWeightProfiles(Camp camp) {
        if (campWeightProfileRepository.count() > 0) {
            log.info("Camp weight profiles already exist, skipping.");
            return;
        }

        List<CampWeightProfile> profiles = List.of(
                CampWeightProfile.builder()
                        .camp(camp)
                        .position(FootballPosition.WIDE_RECEIVER)
                        .physicalWeight(new BigDecimal("20.00"))
                        .technicalWeight(new BigDecimal("30.00"))
                        .tacticalWeight(new BigDecimal("20.00"))
                        .attendanceWeight(new BigDecimal("5.00"))
                        .disciplineWeight(new BigDecimal("5.00"))
                        .coachReviewWeight(new BigDecimal("20.00"))
                        .matchStatsWeight(new BigDecimal("0.00"))
                        .build(),
                CampWeightProfile.builder()
                        .camp(camp)
                        .position(FootballPosition.QUARTERBACK)
                        .physicalWeight(new BigDecimal("15.00"))
                        .technicalWeight(new BigDecimal("30.00"))
                        .tacticalWeight(new BigDecimal("25.00"))
                        .attendanceWeight(new BigDecimal("5.00"))
                        .disciplineWeight(new BigDecimal("5.00"))
                        .coachReviewWeight(new BigDecimal("20.00"))
                        .matchStatsWeight(new BigDecimal("0.00"))
                        .build()
        );
        campWeightProfileRepository.saveAll(profiles);
        log.info("Seeded {} camp weight profiles", profiles.size());
    }

    private void seedAthleteScores(CampEnrollment enrollment1, CampEnrollment enrollment2, Camp camp) {
        if (athleteScoreRepository.count() > 0) {
            log.info("Athlete scores already exist, skipping.");
            return;
        }

        List<AthleteScore> scores = List.of(
                AthleteScore.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .position(FootballPosition.WIDE_RECEIVER)
                        .physicalScore(new BigDecimal("82.00"))
                        .technicalScore(new BigDecimal("85.00"))
                        .tacticalScore(new BigDecimal("90.00"))
                        .attendanceScore(new BigDecimal("90.00"))
                        .disciplineScore(new BigDecimal("95.00"))
                        .coachReviewScore(new BigDecimal("83.00"))
                        .matchStatsScore(new BigDecimal("0.00"))
                        .totalWeightedScore(new BigDecimal("86.50"))
                        .calculatedAt(LocalDateTime.now())
                        .build(),
                AthleteScore.builder()
                        .enrollment(enrollment2)
                        .campId(camp.getId())
                        .position(FootballPosition.WIDE_RECEIVER)
                        .physicalScore(new BigDecimal("75.00"))
                        .technicalScore(new BigDecimal("70.00"))
                        .tacticalScore(new BigDecimal("75.00"))
                        .attendanceScore(new BigDecimal("83.00"))
                        .disciplineScore(new BigDecimal("100.00"))
                        .coachReviewScore(new BigDecimal("78.00"))
                        .matchStatsScore(new BigDecimal("0.00"))
                        .totalWeightedScore(new BigDecimal("76.20"))
                        .calculatedAt(LocalDateTime.now())
                        .build()
        );
        athleteScoreRepository.saveAll(scores);
        log.info("Seeded {} athlete scores", scores.size());
    }

    private void seedAthleteRankings(CampEnrollment enrollment1, CampEnrollment enrollment2, Camp camp) {
        if (athleteRankingRepository.count() > 0) {
            log.info("Athlete rankings already exist, skipping.");
            return;
        }

        List<AthleteRanking> rankings = List.of(
                AthleteRanking.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .rankingType(RankingType.OVERALL)
                        .rankValue(1)
                        .totalInCategory(2)
                        .percentile(new BigDecimal("95.00"))
                        .score(new BigDecimal("86.50"))
                        .calculatedAt(LocalDateTime.now())
                        .build(),
                AthleteRanking.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .rankingType(RankingType.POSITION)
                        .rankValue(1)
                        .totalInCategory(2)
                        .percentile(new BigDecimal("95.00"))
                        .score(new BigDecimal("86.50"))
                        .filterValue("WIDE_RECEIVER")
                        .calculatedAt(LocalDateTime.now())
                        .build(),
                AthleteRanking.builder()
                        .enrollment(enrollment2)
                        .campId(camp.getId())
                        .rankingType(RankingType.OVERALL)
                        .rankValue(2)
                        .totalInCategory(2)
                        .percentile(new BigDecimal("50.00"))
                        .score(new BigDecimal("76.20"))
                        .calculatedAt(LocalDateTime.now())
                        .build()
        );
        athleteRankingRepository.saveAll(rankings);
        log.info("Seeded {} athlete rankings", rankings.size());
    }

    // ======================== SELECTION ========================

    private void seedSelectionDecisions(CampEnrollment enrollment1, CampEnrollment enrollment2,
                                        Camp camp, User adminUser) {
        if (selectionDecisionRepository.count() > 0) {
            log.info("Selection decisions already exist, skipping.");
            return;
        }

        List<SelectionDecision> decisions = List.of(
                SelectionDecision.builder()
                        .enrollment(enrollment1)
                        .campId(camp.getId())
                        .recommendedTier(SelectionTier.TIER_1_ELITE)
                        .finalTier(SelectionTier.TIER_1_ELITE)
                        .approvalStatus(ApprovalStatus.APPROVED)
                        .reviewedBy(adminUser.getId())
                        .comments("Exceptional performance across all categories. Recommended for national program.")
                        .approvalDate(LocalDateTime.now().minusDays(1))
                        .build(),
                SelectionDecision.builder()
                        .enrollment(enrollment2)
                        .campId(camp.getId())
                        .recommendedTier(SelectionTier.TIER_3_DEVELOPING)
                        .approvalStatus(ApprovalStatus.PENDING)
                        .comments("Shows potential. Needs more development in technical skills.")
                        .build()
        );
        selectionDecisionRepository.saveAll(decisions);
        log.info("Seeded {} selection decisions", decisions.size());
    }

    // ======================== DEVELOPMENT ========================

    private void seedDevelopmentPlans(User athleteUser, Camp camp, User coachUser) {
        if (developmentPlanRepository.count() > 0) {
            log.info("Development plans already exist, skipping.");
            return;
        }

        DevelopmentPlan plan = DevelopmentPlan.builder()
                .athleteUserId(athleteUser.getId())
                .campId(camp.getId())
                .coachUserId(coachUser.getId())
                .status(DevelopmentPlanStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 7, 15))
                .targetEndDate(LocalDate.of(2026, 10, 15))
                .overallAssessment("John demonstrates elite-level athleticism with strong route running. Focus areas: hand strength for contested catches and deeper tactical understanding.")
                .strengths(new ArrayList<>())
                .weaknesses(new ArrayList<>())
                .goals(new ArrayList<>())
                .progressEntries(new ArrayList<>())
                .build();
        plan = developmentPlanRepository.save(plan);

        // Add strengths
        DevelopmentStrength strength1 = DevelopmentStrength.builder()
                .plan(plan)
                .category("Speed")
                .description("Top-tier 40-yard dash time, elite acceleration")
                .score(new BigDecimal("9.00"))
                .build();
        DevelopmentStrength strength2 = DevelopmentStrength.builder()
                .plan(plan)
                .category("Route Running")
                .description("Sharp cuts and precise route execution")
                .score(new BigDecimal("8.50"))
                .build();
        plan.getStrengths().add(strength1);
        plan.getStrengths().add(strength2);

        // Add weaknesses
        DevelopmentWeakness weakness1 = DevelopmentWeakness.builder()
                .plan(plan)
                .category("Hand Strength")
                .description("Needs improvement in contested catch situations")
                .score(new BigDecimal("6.00"))
                .targetScore("8.00")
                .build();
        DevelopmentWeakness weakness2 = DevelopmentWeakness.builder()
                .plan(plan)
                .category("Blocking")
                .description("Run blocking technique needs refinement")
                .score(new BigDecimal("5.50"))
                .targetScore("7.50")
                .build();
        plan.getWeaknesses().add(weakness1);
        plan.getWeaknesses().add(weakness2);

        // Add goals
        DevelopmentGoal goal1 = DevelopmentGoal.builder()
                .plan(plan)
                .title("Improve contested catch rate")
                .description("Increase catch rate in contested situations from 55% to 70%")
                .metric("Catch Rate %")
                .currentValue(new BigDecimal("55.00"))
                .targetValue(new BigDecimal("70.00"))
                .targetDate(LocalDate.of(2026, 9, 30))
                .status(GoalStatus.IN_PROGRESS)
                .build();
        DevelopmentGoal goal2 = DevelopmentGoal.builder()
                .plan(plan)
                .title("Reduce 40-yard dash time")
                .description("Target sub-4.45 40-yard dash")
                .metric("Seconds")
                .currentValue(new BigDecimal("4.55"))
                .targetValue(new BigDecimal("4.45"))
                .targetDate(LocalDate.of(2026, 10, 15))
                .status(GoalStatus.NOT_STARTED)
                .build();
        plan.getGoals().add(goal1);
        plan.getGoals().add(goal2);

        // Add progress entry
        DevelopmentProgress progress = DevelopmentProgress.builder()
                .plan(plan)
                .goal(goal1)
                .value(new BigDecimal("58.00"))
                .notes("Improvement visible after two weeks of hand-strengthening drills")
                .recordedBy(coachUser.getId())
                .recordedDate(LocalDate.of(2026, 7, 30))
                .build();
        plan.getProgressEntries().add(progress);

        developmentPlanRepository.save(plan);
        log.info("Seeded development plan with strengths, weaknesses, goals, and progress");
    }

    private void seedTrainingRecommendations() {
        if (trainingRecommendationRepository.count() > 0) {
            log.info("Training recommendations already exist, skipping.");
            return;
        }

        List<TrainingRecommendation> recommendations = List.of(
                TrainingRecommendation.builder()
                        .position(FootballPosition.WIDE_RECEIVER)
                        .category("Speed")
                        .title("Sprint Interval Training")
                        .description("40-yard sprints with 60-second rest intervals. 8-10 reps per session.")
                        .difficultyLevel("Advanced")
                        .isActive(true)
                        .build(),
                TrainingRecommendation.builder()
                        .position(FootballPosition.WIDE_RECEIVER)
                        .category("Hands")
                        .title("Tennis Ball Catch Drills")
                        .description("One-handed tennis ball catches to improve grip strength and hand-eye coordination.")
                        .difficultyLevel("Intermediate")
                        .isActive(true)
                        .build(),
                TrainingRecommendation.builder()
                        .position(FootballPosition.QUARTERBACK)
                        .category("Accuracy")
                        .title("Target Throwing Progression")
                        .description("Progressive distance throwing with decreasing target size. Focus on footwork and release point.")
                        .difficultyLevel("Advanced")
                        .isActive(true)
                        .build(),
                TrainingRecommendation.builder()
                        .position(FootballPosition.LINEBACKER)
                        .category("Tackling")
                        .title("Form Tackling Circuits")
                        .description("Controlled tackling drills emphasizing proper form and wrap technique.")
                        .difficultyLevel("Beginner")
                        .isActive(true)
                        .build()
        );
        trainingRecommendationRepository.saveAll(recommendations);
        log.info("Seeded {} training recommendations", recommendations.size());
    }

    // ======================== TRAINING LOAD ========================

    private void seedTrainingLoadRecords(CampEnrollment enrollment, Camp camp, User coachUser) {
        if (trainingLoadRepository.count() > 0) {
            log.info("Training load records already exist, skipping.");
            return;
        }

        List<TrainingLoadRecord> records = List.of(
                TrainingLoadRecord.builder()
                        .enrollmentId(enrollment.getId())
                        .campId(camp.getId())
                        .sessionDate(LocalDate.of(2026, 7, 1))
                        .durationMinutes(90)
                        .intensity(TrainingIntensity.HIGH)
                        .sessionType(TrainingSessionType.CONDITIONING)
                        .recordedBy(coachUser.getId())
                        .notes("Full conditioning session - sprints, agility ladders, cone drills")
                        .build(),
                TrainingLoadRecord.builder()
                        .enrollmentId(enrollment.getId())
                        .campId(camp.getId())
                        .sessionDate(LocalDate.of(2026, 7, 2))
                        .durationMinutes(120)
                        .intensity(TrainingIntensity.VERY_HIGH)
                        .sessionType(TrainingSessionType.SKILLS)
                        .recordedBy(coachUser.getId())
                        .notes("Intensive position-specific skills work")
                        .build(),
                TrainingLoadRecord.builder()
                        .enrollmentId(enrollment.getId())
                        .campId(camp.getId())
                        .sessionDate(LocalDate.of(2026, 7, 3))
                        .durationMinutes(60)
                        .intensity(TrainingIntensity.LOW)
                        .sessionType(TrainingSessionType.RECOVERY)
                        .recordedBy(coachUser.getId())
                        .notes("Active recovery - yoga, stretching, pool session")
                        .build(),
                TrainingLoadRecord.builder()
                        .enrollmentId(enrollment.getId())
                        .campId(camp.getId())
                        .sessionDate(LocalDate.of(2026, 7, 4))
                        .durationMinutes(90)
                        .intensity(TrainingIntensity.MEDIUM)
                        .sessionType(TrainingSessionType.STRENGTH)
                        .recordedBy(coachUser.getId())
                        .notes("Upper body focus - bench, pull-ups, shoulder press")
                        .build(),
                TrainingLoadRecord.builder()
                        .enrollmentId(enrollment.getId())
                        .campId(camp.getId())
                        .sessionDate(LocalDate.of(2026, 7, 5))
                        .durationMinutes(105)
                        .intensity(TrainingIntensity.HIGH)
                        .sessionType(TrainingSessionType.SCRIMMAGE)
                        .recordedBy(coachUser.getId())
                        .notes("Intra-squad scrimmage - full contact, game situations")
                        .build()
        );
        trainingLoadRepository.saveAll(records);
        log.info("Seeded {} training load records", records.size());
    }

    // ======================== NATIONAL RANKINGS ========================

    private void seedNationalRankings(User athleteUser, User athlete2User) {
        if (nationalRankingConfigRepository.count() > 0) {
            log.info("National rankings already exist, skipping.");
            return;
        }

        NationalRankingConfig config = NationalRankingConfig.builder()
                .name("Nigeria National Ranking 2026")
                .description("Standard national ranking configuration for Nigeria elite athletes")
                .weightFormula("{\"physical\": 0.25, \"technical\": 0.30, \"tactical\": 0.20, \"attendance\": 0.05, \"discipline\": 0.05, \"coachReview\": 0.15}")
                .minCampsRequired(1)
                .ageCategories("[\"UNDER_21\", \"OPEN\"]")
                .positions("[\"QUARTERBACK\", \"WIDE_RECEIVER\", \"RUNNING_BACK\", \"LINEBACKER\"]")
                .active(true)
                .build();
        config = nationalRankingConfigRepository.save(config);

        List<NationalRanking> rankings = List.of(
                NationalRanking.builder()
                        .athleteUserId(athleteUser.getId())
                        .configId(config.getId())
                        .compositeScore(new BigDecimal("86.50"))
                        .rankValue(1)
                        .totalAthletes(50)
                        .percentile(new BigDecimal("98.00"))
                        .rankingScope(RankingScope.NATIONAL)
                        .position(FootballPosition.WIDE_RECEIVER)
                        .ageCategory(AgeCategory.UNDER_21)
                        .generatedAt(LocalDateTime.now())
                        .build(),
                NationalRanking.builder()
                        .athleteUserId(athlete2User.getId())
                        .configId(config.getId())
                        .compositeScore(new BigDecimal("76.20"))
                        .rankValue(12)
                        .totalAthletes(50)
                        .percentile(new BigDecimal("76.00"))
                        .rankingScope(RankingScope.NATIONAL)
                        .position(FootballPosition.WIDE_RECEIVER)
                        .ageCategory(AgeCategory.UNDER_21)
                        .generatedAt(LocalDateTime.now())
                        .build(),
                NationalRanking.builder()
                        .athleteUserId(athleteUser.getId())
                        .configId(config.getId())
                        .compositeScore(new BigDecimal("86.50"))
                        .rankValue(1)
                        .totalAthletes(15)
                        .percentile(new BigDecimal("93.00"))
                        .rankingScope(RankingScope.STATE)
                        .scopeValue("Lagos")
                        .position(FootballPosition.WIDE_RECEIVER)
                        .ageCategory(AgeCategory.UNDER_21)
                        .generatedAt(LocalDateTime.now())
                        .build()
        );
        nationalRankingRepository.saveAll(rankings);
        log.info("Seeded national ranking config and {} rankings", rankings.size());
    }

    // ======================== ANALYTICS ========================

    private void seedBenchmarkProfiles(User adminUser) {
        if (benchmarkProfileRepository.count() > 0) {
            log.info("Benchmark profiles already exist, skipping.");
            return;
        }

        List<BenchmarkProfile> profiles = List.of(
                BenchmarkProfile.builder()
                        .name("NFL Combine WR Average (2025)")
                        .benchmarkType(BenchmarkType.HISTORICAL_CHAMPION)
                        .position(FootballPosition.WIDE_RECEIVER)
                        .ageCategory(AgeCategory.OPEN)
                        .gender(Gender.MALE)
                        .physicalBenchmark(new BigDecimal("90.00"))
                        .technicalBenchmark(new BigDecimal("88.00"))
                        .tacticalBenchmark(new BigDecimal("85.00"))
                        .overallBenchmark(new BigDecimal("87.50"))
                        .sourceDescription("Based on NFL Combine averages for Wide Receivers")
                        .autoCaptured(false)
                        .createdBy(adminUser.getId())
                        .build(),
                BenchmarkProfile.builder()
                        .name("Nigeria Camp Top Performer 2025")
                        .benchmarkType(BenchmarkType.CAMP)
                        .position(FootballPosition.WIDE_RECEIVER)
                        .ageCategory(AgeCategory.UNDER_21)
                        .gender(Gender.MALE)
                        .physicalBenchmark(new BigDecimal("84.00"))
                        .technicalBenchmark(new BigDecimal("82.00"))
                        .tacticalBenchmark(new BigDecimal("80.00"))
                        .overallBenchmark(new BigDecimal("82.00"))
                        .sourceDescription("Top performer from 2025 Lagos Identification Camp")
                        .autoCaptured(true)
                        .createdBy(adminUser.getId())
                        .build(),
                BenchmarkProfile.builder()
                        .name("West Africa Regional Standard")
                        .benchmarkType(BenchmarkType.REGION)
                        .position(FootballPosition.QUARTERBACK)
                        .ageCategory(AgeCategory.UNDER_21)
                        .gender(Gender.MALE)
                        .physicalBenchmark(new BigDecimal("78.00"))
                        .technicalBenchmark(new BigDecimal("75.00"))
                        .tacticalBenchmark(new BigDecimal("72.00"))
                        .overallBenchmark(new BigDecimal("75.00"))
                        .sourceDescription("Regional average for West African quarterback prospects")
                        .autoCaptured(false)
                        .createdBy(adminUser.getId())
                        .build()
        );
        benchmarkProfileRepository.saveAll(profiles);
        log.info("Seeded {} benchmark profiles", profiles.size());
    }

    private void seedBenchmarkComparisons(CampEnrollment enrollment) {
        if (benchmarkComparisonRepository.count() > 0) {
            log.info("Benchmark comparisons already exist, skipping.");
            return;
        }

        BenchmarkProfile profile = benchmarkProfileRepository.findAll().getFirst();

        BenchmarkComparison comparison = BenchmarkComparison.builder()
                .enrollmentId(enrollment.getId())
                .benchmarkProfileId(profile.getId())
                .physicalGap(new BigDecimal("-8.00"))
                .technicalGap(new BigDecimal("-3.00"))
                .tacticalGap(new BigDecimal("5.00"))
                .overallGap(new BigDecimal("-1.00"))
                .percentileRank(new BigDecimal("85.00"))
                .comparedAt(LocalDateTime.now())
                .build();
        benchmarkComparisonRepository.save(comparison);
        log.info("Seeded 1 benchmark comparison");
    }

    private void seedPotentialPredictions(CampEnrollment enrollment1, CampEnrollment enrollment2) {
        if (potentialPredictionRepository.count() > 0) {
            log.info("Potential predictions already exist, skipping.");
            return;
        }

        List<PotentialPrediction> predictions = List.of(
                PotentialPrediction.builder()
                        .athleteUserId(enrollment1.getUser().getId())
                        .predictionTier(PredictionTier.ELITE_PROSPECT)
                        .overallGrowthRate(new BigDecimal("12.50"))
                        .physicalDelta(new BigDecimal("3.00"))
                        .technicalDelta(new BigDecimal("5.00"))
                        .tacticalDelta(new BigDecimal("4.50"))
                        .projectedScore1yr(new BigDecimal("90.00"))
                        .projectedScore2yr(new BigDecimal("93.00"))
                        .confidenceLevel(new BigDecimal("82.00"))
                        .calculatedAt(LocalDateTime.now())
                        .build(),
                PotentialPrediction.builder()
                        .athleteUserId(enrollment2.getUser().getId())
                        .predictionTier(PredictionTier.SOLID_POTENTIAL)
                        .overallGrowthRate(new BigDecimal("8.00"))
                        .physicalDelta(new BigDecimal("4.00"))
                        .technicalDelta(new BigDecimal("6.00"))
                        .tacticalDelta(new BigDecimal("5.00"))
                        .projectedScore1yr(new BigDecimal("80.00"))
                        .projectedScore2yr(new BigDecimal("84.00"))
                        .confidenceLevel(new BigDecimal("70.00"))
                        .calculatedAt(LocalDateTime.now())
                        .build()
        );
        potentialPredictionRepository.saveAll(predictions);
        log.info("Seeded {} potential predictions", predictions.size());
    }

    private void seedInjuryRiskAssessments(User athleteUser, User athlete2User,
                                            CampEnrollment enrollment1, CampEnrollment enrollment2) {
        if (injuryRiskRepository.count() > 0) {
            log.info("Injury risk assessments already exist, skipping.");
            return;
        }

        List<InjuryRiskAssessment> assessments = List.of(
                InjuryRiskAssessment.builder()
                        .athleteUserId(athleteUser.getId())
                        .enrollmentId(enrollment1.getId())
                        .riskScore(25)
                        .riskLevel(RiskLevel.LOW)
                        .loadFactorScore(new BigDecimal("20.00"))
                        .attendanceFactorScore(new BigDecimal("5.00"))
                        .performanceFactorScore(new BigDecimal("10.00"))
                        .recommendations("[\"Continue current training load\", \"Monitor recovery between sessions\"]")
                        .assessedAt(LocalDateTime.now())
                        .build(),
                InjuryRiskAssessment.builder()
                        .athleteUserId(athlete2User.getId())
                        .enrollmentId(enrollment2.getId())
                        .riskScore(45)
                        .riskLevel(RiskLevel.MODERATE)
                        .loadFactorScore(new BigDecimal("30.00"))
                        .attendanceFactorScore(new BigDecimal("15.00"))
                        .performanceFactorScore(new BigDecimal("20.00"))
                        .recommendations("[\"Reduce training intensity by 20%\", \"Add extra recovery day\", \"Monitor hamstring flexibility\"]")
                        .assessedAt(LocalDateTime.now())
                        .build()
        );
        injuryRiskRepository.saveAll(assessments);
        log.info("Seeded {} injury risk assessments", assessments.size());
    }

    // ======================== SCOUTING ========================

    private void seedScoutProfile(User scoutUser) {
        if (scoutProfileRepository.count() > 0) {
            log.info("Scout profiles already exist, skipping.");
            return;
        }

        ScoutProfile profile = ScoutProfile.builder()
                .userId(scoutUser.getId())
                .fullName("David Williams")
                .organization("American Football International Academy")
                .organizationType(OrganizationType.NFL_ACADEMY)
                .country("United States")
                .region("West Africa")
                .verified(true)
                .build();
        scoutProfileRepository.save(profile);
        log.info("Seeded scout profile");
    }

    private void seedScoutAccessScopes(User scoutUser, User adminUser) {
        if (scoutAccessScopeRepository.count() > 0) {
            log.info("Scout access scopes already exist, skipping.");
            return;
        }

        List<ScoutAccessScope> scopes = List.of(
                ScoutAccessScope.builder()
                        .scoutUserId(scoutUser.getId())
                        .scopeType(ScopeType.COUNTRY)
                        .scopeValue("Nigeria")
                        .grantedBy(adminUser.getId())
                        .grantedDate(LocalDateTime.now().minusDays(30))
                        .expiresAt(LocalDateTime.now().plusMonths(6))
                        .build(),
                ScoutAccessScope.builder()
                        .scoutUserId(scoutUser.getId())
                        .scopeType(ScopeType.POSITION)
                        .scopeValue("WIDE_RECEIVER")
                        .grantedBy(adminUser.getId())
                        .grantedDate(LocalDateTime.now().minusDays(30))
                        .expiresAt(LocalDateTime.now().plusMonths(6))
                        .build(),
                ScoutAccessScope.builder()
                        .scoutUserId(scoutUser.getId())
                        .scopeType(ScopeType.AGE_GROUP)
                        .scopeValue("UNDER_21")
                        .grantedBy(adminUser.getId())
                        .grantedDate(LocalDateTime.now().minusDays(30))
                        .expiresAt(LocalDateTime.now().plusMonths(6))
                        .build()
        );
        scoutAccessScopeRepository.saveAll(scopes);
        log.info("Seeded {} scout access scopes", scopes.size());
    }

    private void seedScoutBookmarks(User scoutUser, User athleteUser, User athlete2User) {
        if (scoutBookmarkRepository.count() > 0) {
            log.info("Scout bookmarks already exist, skipping.");
            return;
        }

        List<ScoutBookmark> bookmarks = List.of(
                ScoutBookmark.builder()
                        .scoutUserId(scoutUser.getId())
                        .athleteUserId(athleteUser.getId())
                        .notes("Elite speed and route-running ability. Top prospect for WR position. Follow up after next camp.")
                        .build(),
                ScoutBookmark.builder()
                        .scoutUserId(scoutUser.getId())
                        .athleteUserId(athlete2User.getId())
                        .notes("High character athlete with strong work ethic. Monitor development over next 6 months.")
                        .build()
        );
        scoutBookmarkRepository.saveAll(bookmarks);
        log.info("Seeded {} scout bookmarks", bookmarks.size());
    }

    private void seedInterviewRequests(User scoutUser, User athleteUser, User adminUser) {
        if (interviewRequestRepository.count() > 0) {
            log.info("Interview requests already exist, skipping.");
            return;
        }

        List<InterviewRequest> requests = List.of(
                InterviewRequest.builder()
                        .scoutUserId(scoutUser.getId())
                        .athleteUserId(athleteUser.getId())
                        .preferredDates("[\"2026-08-15\", \"2026-08-16\", \"2026-08-17\"]")
                        .notes("Would like to discuss athlete's long-term goals and potential pathway to NFL International Program.")
                        .status(InterviewStatus.APPROVED)
                        .reviewedBy(adminUser.getId())
                        .reviewNotes("Approved - athlete and guardian notified.")
                        .reviewDate(LocalDateTime.now().minusDays(5))
                        .build(),
                InterviewRequest.builder()
                        .scoutUserId(scoutUser.getId())
                        .athleteUserId(athleteUser.getId())
                        .preferredDates("[\"2026-09-01\", \"2026-09-02\"]")
                        .notes("Follow-up interview to discuss training program enrollment.")
                        .status(InterviewStatus.PENDING)
                        .build()
        );
        interviewRequestRepository.saveAll(requests);
        log.info("Seeded {} interview requests", requests.size());
    }

    // ======================== VIDEO LIBRARY ========================

    private void seedVideoLibraryItems(User athleteUser, Camp camp, User coachUser) {
        if (videoLibraryRepository.count() > 0) {
            log.info("Video library items already exist, skipping.");
            return;
        }

        List<VideoLibraryItem> items = List.of(
                VideoLibraryItem.builder()
                        .athleteUserId(athleteUser.getId())
                        .title("40-Yard Dash Assessment - Day 1")
                        .description("Official 40-yard dash recorded during physical assessment stage")
                        .url("https://www.youtube.com/watch?v=speed_test_001")
                        .videoType(VideoType.YOUTUBE)
                        .category(VideoCategory.DRILL)
                        .position(FootballPosition.WIDE_RECEIVER)
                        .campId(camp.getId())
                        .uploadedBy(coachUser.getId())
                        .isPublic(true)
                        .build(),
                VideoLibraryItem.builder()
                        .athleteUserId(athleteUser.getId())
                        .title("Route Running Drill Highlights")
                        .description("Compilation of route running drills showing precision cuts and acceleration")
                        .url("https://www.hudl.com/video/route_running_001")
                        .videoType(VideoType.HUDL)
                        .category(VideoCategory.POSITION_SPECIFIC)
                        .position(FootballPosition.WIDE_RECEIVER)
                        .campId(camp.getId())
                        .uploadedBy(coachUser.getId())
                        .isPublic(true)
                        .build(),
                VideoLibraryItem.builder()
                        .athleteUserId(athleteUser.getId())
                        .title("Scrimmage Game 1 - Full Highlights")
                        .description("Game highlights from first intra-squad scrimmage")
                        .url("https://www.youtube.com/watch?v=scrimmage_game1")
                        .videoType(VideoType.YOUTUBE)
                        .category(VideoCategory.MATCH)
                        .position(FootballPosition.WIDE_RECEIVER)
                        .campId(camp.getId())
                        .uploadedBy(coachUser.getId())
                        .isPublic(true)
                        .build(),
                VideoLibraryItem.builder()
                        .athleteUserId(athleteUser.getId())
                        .title("Camp Day 7 - Training Session")
                        .description("General training session footage showing work ethic and conditioning")
                        .url("https://drive.google.com/file/d/training_day7")
                        .videoType(VideoType.GOOGLE_DRIVE)
                        .category(VideoCategory.TRAINING)
                        .campId(camp.getId())
                        .uploadedBy(coachUser.getId())
                        .isPublic(false)
                        .build()
        );
        videoLibraryRepository.saveAll(items);
        log.info("Seeded {} video library items", items.size());
    }

    // ======================== EVENTS ========================

    private void seedEvents(User athleteUser, User coachUser, User scoutUser) {
        if (eventRepository.count() > 0) {
            log.info("Events already seeded, skipping.");
            return;
        }

        // Event 1: Exhibition Game - REGISTRATION_OPEN
        Event exhibitionGame = eventRepository.save(Event.builder()
                .name("Lagos Exhibition Game 2026")
                .description("Annual exhibition game showcasing top talent from across Nigeria. This event brings together elite athletes for a competitive showcase.")
                .eventType("Exhibition Game")
                .status(EventStatus.REGISTRATION_OPEN)
                .location("Teslim Balogun Stadium")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.of(2026, 10, 15))
                .endDate(LocalDate.of(2026, 10, 15))
                .startTime(java.time.LocalTime.of(10, 0))
                .endTime(java.time.LocalTime.of(16, 0))
                .registrationDeadline(LocalDate.of(2026, 10, 1))
                .capacity(200)
                .currentRegistrationCount(3)
                .eventImage("https://images.eaids.com/events/lagos-exhibition-2026.jpg")
                .imagePath("/events/lagos-exhibition-2026.jpg")
                .build());

        // Event 2: Showcase - DRAFT
        eventRepository.save(Event.builder()
                .name("Abuja Talent Showcase")
                .description("A two-day showcase event for scouts and coaches to evaluate emerging talent in the region.")
                .eventType("Showcase")
                .status(EventStatus.DRAFT)
                .location("National Stadium Abuja")
                .country("Nigeria")
                .state("FCT")
                .startDate(LocalDate.of(2026, 12, 5))
                .endDate(LocalDate.of(2026, 12, 6))
                .startTime(java.time.LocalTime.of(9, 0))
                .endTime(java.time.LocalTime.of(17, 0))
                .registrationDeadline(LocalDate.of(2026, 11, 20))
                .capacity(150)
                .currentRegistrationCount(0)
                .build());

        // Event 3: Community Event - COMPLETED
        Event communityEvent = eventRepository.save(Event.builder()
                .name("Port Harcourt Community Day")
                .description("Community engagement event introducing American football to local youth.")
                .eventType("Community Event")
                .status(EventStatus.COMPLETED)
                .location("Liberation Stadium")
                .country("Nigeria")
                .state("Rivers")
                .startDate(LocalDate.of(2026, 6, 20))
                .endDate(LocalDate.of(2026, 6, 20))
                .startTime(java.time.LocalTime.of(8, 0))
                .endTime(java.time.LocalTime.of(14, 0))
                .registrationDeadline(LocalDate.of(2026, 6, 10))
                .capacity(300)
                .currentRegistrationCount(3)
                .eventImage("https://images.eaids.com/events/ph-community-day-2026.jpg")
                .imagePath("/events/ph-community-day-2026.jpg")
                .build());

        // Registrations for Exhibition Game (REGISTRATION_OPEN)
        eventRegistrationRepository.save(EventRegistration.builder()
                .event(exhibitionGame)
                .user(athleteUser)
                .status(RegistrationStatus.REGISTERED)
                .build());

        eventRegistrationRepository.save(EventRegistration.builder()
                .event(exhibitionGame)
                .user(coachUser)
                .status(RegistrationStatus.CONFIRMED)
                .build());

        // Registrations for Community Event (COMPLETED)
        eventRegistrationRepository.save(EventRegistration.builder()
                .event(communityEvent)
                .user(athleteUser)
                .status(RegistrationStatus.ATTENDED)
                .build());

        eventRegistrationRepository.save(EventRegistration.builder()
                .event(communityEvent)
                .user(coachUser)
                .status(RegistrationStatus.ATTENDED)
                .build());

        eventRegistrationRepository.save(EventRegistration.builder()
                .event(communityEvent)
                .user(scoutUser)
                .status(RegistrationStatus.NO_SHOW)
                .build());

        // Guest registration for Exhibition Game
        eventRegistrationRepository.save(EventRegistration.builder()
                .event(exhibitionGame)
                .user(null)
                .guestName("John Guest")
                .guestEmail("john.guest@example.com")
                .guestPhone("+2348012345678")
                .status(RegistrationStatus.REGISTERED)
                .build());

        log.info("Seeded 3 events and 6 event registrations (including 1 guest)");
    }
}
