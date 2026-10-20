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
import com.feadev.eaids.league.model.LeagueStatEntry;
import com.feadev.eaids.league.repository.LeagueStatEntryRepository;
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
import com.feadev.eaids.league.model.*;
import com.feadev.eaids.league.model.enums.*;
import com.feadev.eaids.league.repository.*;
import com.feadev.eaids.commentary.model.MatchCommentaryEvent;
import com.feadev.eaids.commentary.model.enums.GameEventType;
import com.feadev.eaids.commentary.repository.MatchCommentaryEventRepository;
import com.feadev.eaids.league.model.PostGameSummary;
import com.feadev.eaids.league.repository.PostGameSummaryRepository;
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

    // League & Team Management repositories
    private final LeagueRepository leagueRepository;
    private final LeagueDivisionRepository leagueDivisionRepository;
    private final LeagueStaffRepository leagueStaffRepository;
    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final PlayerTransferRepository playerTransferRepository;
    private final MatchFixtureRepository matchFixtureRepository;
    private final LeagueStatEntryRepository leagueStatEntryRepository;

    // Medical module repositories
    private final com.feadev.eaids.medical.repository.AthleteMedicalProfileRepository athleteMedicalProfileRepository;
    private final com.feadev.eaids.medical.repository.MedicalAssessmentRepository medicalAssessmentRepository;
    private final com.feadev.eaids.medical.repository.AthleteInjuryRepository athleteInjuryRepository;
    private final com.feadev.eaids.medical.repository.MedicalClearanceRepository medicalClearanceRepository;

    // Enhancement feature repositories (commentary, post-game, injury risk)
    private final com.feadev.eaids.commentary.repository.MatchCommentaryEventRepository matchCommentaryEventRepository;
    private final com.feadev.eaids.league.repository.PostGameSummaryRepository postGameSummaryRepository;

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
        // Enhancement feature staff users (medical officer, physiotherapist, coordinator)
        seedMedicalOfficerUser(encodedPassword);
        seedPhysiotherapistUser(encodedPassword);

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

        // Phase 17: League & Team Management
        seedLeagueData(adminUser, athleteUser, athlete2User, coachUser, scoutUser, matchStatDef);

        // Phase 18: Medical History & Health Clearance
        seedMedicalData(adminUser, athleteUser, athlete2User);

        // Phase 19: Enhancement features — Commentary, Post-Game Stats, Injury Risk
        seedEnhancementData(adminUser, athleteUser, athlete2User);

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

    // ======================== LEAGUE & TEAM MANAGEMENT ========================

    private void seedLeagueData(User adminUser, User athleteUser, User athlete2User,
                                User coachUser, User scoutUser,
                                MatchStatDefinition passingYardsDef) {
        if (leagueRepository.count() > 0) {
            log.info("League data already exists, skipping.");
            return;
        }

        // ── 1. Leagues ──────────────────────────────────────────────────────

        League nigeriaLeague = leagueRepository.save(League.builder()
                .name("Nigeria Flag Football League")
                .description("Premier flag football competition for the 2026 season, open to all regions.")
                .seasonYear(2026)
                .country("Nigeria")
                .region("West Africa")
                .status(LeagueStatus.ACTIVE)
                .build());

        League westAfricaLeague = leagueRepository.save(League.builder()
                .name("West Africa Tackle League")
                .description("International tackle football league covering West African nations.")
                .seasonYear(2026)
                .country(null)
                .region("West Africa")
                .status(LeagueStatus.DRAFT)
                .build());

        log.info("Seeded 2 leagues");

        // ── 2. Divisions ─────────────────────────────────────────────────────

        LeagueDivision divisionOne = leagueDivisionRepository.save(LeagueDivision.builder()
                .league(nigeriaLeague)
                .name("Division 1")
                .tierRank(1)
                .description("Top tier — elite clubs competing for the national title.")
                .build());

        LeagueDivision divisionTwo = leagueDivisionRepository.save(LeagueDivision.builder()
                .league(nigeriaLeague)
                .name("Division 2")
                .tierRank(2)
                .description("Second tier — developing clubs with promotion potential.")
                .build());

        log.info("Seeded 2 divisions for Nigeria Flag Football League");

        // ── 3. League Staff ───────────────────────────────────────────────────

        leagueStaffRepository.save(LeagueStaff.builder()
                .league(nigeriaLeague)
                .user(coachUser)
                .staffRole(LeagueStaffRole.LEAGUE_COORDINATOR)
                .notes("Lead coordinator responsible for scheduling and logistics.")
                .assignedDate(LocalDateTime.now())
                .build());

        leagueStaffRepository.save(LeagueStaff.builder()
                .league(nigeriaLeague)
                .user(scoutUser)
                .staffRole(LeagueStaffRole.STATISTICIAN)
                .notes("Responsible for recording and publishing all match statistics.")
                .assignedDate(LocalDateTime.now())
                .build());

        leagueStaffRepository.save(LeagueStaff.builder()
                .league(nigeriaLeague)
                .user(adminUser)
                .staffRole(LeagueStaffRole.REFEREE)
                .notes("Head referee for Division 1 fixtures.")
                .assignedDate(LocalDateTime.now())
                .build());

        log.info("Seeded 3 league staff assignments");

        // ── 4. Teams ─────────────────────────────────────────────────────────

        Team lagosLions = teamRepository.save(Team.builder()
                .name("Lagos Lions")
                .league(nigeriaLeague)
                .division(divisionOne)
                .homeVenue("Onikan Stadium, Lagos")
                .country("Nigeria")
                .state("Lagos")
                .status(TeamStatus.ACTIVE)
                .build());

        Team abujaEagles = teamRepository.save(Team.builder()
                .name("Abuja Eagles")
                .league(nigeriaLeague)
                .division(divisionOne)
                .homeVenue("National Stadium, Abuja")
                .country("Nigeria")
                .state("FCT")
                .status(TeamStatus.ACTIVE)
                .build());

        Team portHarcourtPirates = teamRepository.save(Team.builder()
                .name("Port Harcourt Pirates")
                .league(nigeriaLeague)
                .division(divisionTwo)
                .homeVenue("Sharks Stadium, Port Harcourt")
                .country("Nigeria")
                .state("Rivers")
                .status(TeamStatus.ACTIVE)
                .build());

        log.info("Seeded 3 teams");

        // ── 5. Team Players (Roster) ──────────────────────────────────────────

        TeamPlayer player1 = teamPlayerRepository.save(TeamPlayer.builder()
                .team(lagosLions)
                .athlete(athleteUser)
                .jerseyNumber(12)
                .position(FootballPosition.QUARTERBACK)
                .status(PlayerStatus.ACTIVE)
                .draftedDate(LocalDate.of(2026, 4, 1))
                .build());

        TeamPlayer player2 = teamPlayerRepository.save(TeamPlayer.builder()
                .team(abujaEagles)
                .athlete(athlete2User)
                .jerseyNumber(80)
                .position(FootballPosition.WIDE_RECEIVER)
                .status(PlayerStatus.ACTIVE)
                .draftedDate(LocalDate.of(2026, 4, 1))
                .build());

        log.info("Seeded 2 team players");

        // ── 6. Player Transfer History ────────────────────────────────────────

        // Initial draft record for player1
        playerTransferRepository.save(PlayerTransfer.builder()
                .athleteUserId(athleteUser.getId())
                .fromTeam(null)
                .toTeam(lagosLions)
                .transferType(TransferType.DRAFT)
                .transferDate(LocalDate.of(2026, 4, 1))
                .notes("Drafted to Lagos Lions as starting quarterback.")
                .build());

        // Initial draft record for player2
        playerTransferRepository.save(PlayerTransfer.builder()
                .athleteUserId(athlete2User.getId())
                .fromTeam(null)
                .toTeam(abujaEagles)
                .transferType(TransferType.DRAFT)
                .transferDate(LocalDate.of(2026, 4, 1))
                .notes("Drafted to Abuja Eagles as wide receiver.")
                .build());

        log.info("Seeded 2 player transfer records (DRAFT)");

        // ── 7. Match Fixtures ─────────────────────────────────────────────────

        // Division 1 — upcoming fixture (SCHEDULED)
        MatchFixture scheduledFixture = matchFixtureRepository.save(MatchFixture.builder()
                .league(nigeriaLeague)
                .division(divisionOne)
                .homeTeam(lagosLions)
                .awayTeam(abujaEagles)
                .scheduledDate(LocalDateTime.of(2026, 10, 5, 15, 0))
                .venue("Onikan Stadium, Lagos")
                .status(FixtureStatus.SCHEDULED)
                .refereeUserId(adminUser.getId())
                .notes("Opening day fixture — Division 1.")
                .build());

        // Division 2 — completed: Port Harcourt Pirates 28, Lagos Lions 21
        MatchFixture completedFixture = matchFixtureRepository.save(MatchFixture.builder()
                .league(nigeriaLeague)
                .division(divisionTwo)
                .homeTeam(portHarcourtPirates)
                .awayTeam(lagosLions)
                .scheduledDate(LocalDateTime.of(2026, 9, 20, 14, 0))
                .venue("Sharks Stadium, Port Harcourt")
                .status(FixtureStatus.COMPLETED)
                .homeScore(28)
                .awayScore(21)
                .refereeUserId(adminUser.getId())
                .notes("Port Harcourt won in a close game.")
                .build());

        // Division 1 — postponed
        MatchFixture postponedFixture = matchFixtureRepository.save(MatchFixture.builder()
                .league(nigeriaLeague)
                .division(divisionOne)
                .homeTeam(abujaEagles)
                .awayTeam(portHarcourtPirates)
                .scheduledDate(LocalDateTime.of(2026, 10, 12, 16, 0))
                .venue("National Stadium, Abuja")
                .status(FixtureStatus.POSTPONED)
                .notes("Postponed due to venue unavailability.")
                .build());

        // Division 1 — completed: Lagos Lions 35, Abuja Eagles 14 (matchday 2)
        MatchFixture div1Fixture1 = matchFixtureRepository.save(MatchFixture.builder()
                .league(nigeriaLeague)
                .division(divisionOne)
                .homeTeam(lagosLions)
                .awayTeam(abujaEagles)
                .scheduledDate(LocalDateTime.of(2026, 9, 7, 15, 0))
                .venue("Onikan Stadium, Lagos")
                .status(FixtureStatus.COMPLETED)
                .homeScore(35)
                .awayScore(14)
                .refereeUserId(adminUser.getId())
                .notes("Lagos Lions dominant win — Division 1 matchday 2.")
                .build());

        // Division 1 — completed: Abuja Eagles 28, Lagos Lions 21 (matchday 3)
        MatchFixture div1Fixture2 = matchFixtureRepository.save(MatchFixture.builder()
                .league(nigeriaLeague)
                .division(divisionOne)
                .homeTeam(abujaEagles)
                .awayTeam(lagosLions)
                .scheduledDate(LocalDateTime.of(2026, 9, 14, 15, 0))
                .venue("National Stadium, Abuja")
                .status(FixtureStatus.COMPLETED)
                .homeScore(28)
                .awayScore(21)
                .refereeUserId(adminUser.getId())
                .notes("Abuja Eagles level the series — Division 1 matchday 3.")
                .build());

        // Division 2 — completed: Lagos Lions 42, Port Harcourt Pirates 7 (matchday 3)
        MatchFixture div2Fixture2 = matchFixtureRepository.save(MatchFixture.builder()
                .league(nigeriaLeague)
                .division(divisionTwo)
                .homeTeam(lagosLions)
                .awayTeam(portHarcourtPirates)
                .scheduledDate(LocalDateTime.of(2026, 9, 28, 14, 0))
                .venue("Onikan Stadium, Lagos")
                .status(FixtureStatus.COMPLETED)
                .homeScore(42)
                .awayScore(7)
                .refereeUserId(adminUser.getId())
                .notes("Lagos Lions dominant win in Division 2 rematch.")
                .build());

        log.info("Seeded 6 match fixtures (3 completed, 1 scheduled, 1 postponed, 1 completed-div2)");

        // ── 8. Additional roster players for stat coverage ────────────────────

        // Lagos Lions — Running Back
        TeamPlayer lionsRB = teamPlayerRepository.save(TeamPlayer.builder()
                .team(lagosLions)
                .athlete(coachUser)          // reuse seeded user — distinct athlete not strictly needed for stats
                .jerseyNumber(22)
                .position(FootballPosition.RUNNING_BACK)
                .status(PlayerStatus.ACTIVE)
                .draftedDate(LocalDate.of(2026, 4, 1))
                .build());

        // Abuja Eagles — Quarterback
        TeamPlayer eaglesQB = teamPlayerRepository.save(TeamPlayer.builder()
                .team(abujaEagles)
                .athlete(scoutUser)
                .jerseyNumber(7)
                .position(FootballPosition.QUARTERBACK)
                .status(PlayerStatus.ACTIVE)
                .draftedDate(LocalDate.of(2026, 4, 1))
                .build());

        // Port Harcourt Pirates — Quarterback
        TeamPlayer piratesQB = teamPlayerRepository.save(TeamPlayer.builder()
                .team(portHarcourtPirates)
                .athlete(adminUser)
                .jerseyNumber(1)
                .position(FootballPosition.QUARTERBACK)
                .status(PlayerStatus.ACTIVE)
                .draftedDate(LocalDate.of(2026, 4, 1))
                .build());

        log.info("Seeded 3 additional roster players (RB, QB, QB)");

        // ── 9. Stat definitions lookup ────────────────────────────────────────

        // Find the stat definitions we need — they were seeded in Phase 5 by MatchStatDefinitionSeeder
        MatchStatDefinition rushingYardsDef = matchStatDefinitionRepository
                .findByIsActiveTrue()
                .stream()
                .filter(d -> "Rushing Yards".equalsIgnoreCase(d.getStatName()))
                .findFirst()
                .orElse(null);

        MatchStatDefinition receivingYardsDef = matchStatDefinitionRepository
                .findByIsActiveTrue()
                .stream()
                .filter(d -> "Receiving Yards".equalsIgnoreCase(d.getStatName()))
                .findFirst()
                .orElse(null);

        MatchStatDefinition tacklesDef = matchStatDefinitionRepository
                .findByIsActiveTrue()
                .stream()
                .filter(d -> "Tackles".equalsIgnoreCase(d.getStatName()))
                .findFirst()
                .orElse(null);

        // player1 = Lagos Lions QB #12 (athleteUser)
        // player2 = Abuja Eagles WR #80 (athlete2User)
        // lionsRB = Lagos Lions RB #22
        // eaglesQB = Abuja Eagles QB #7
        // piratesQB = Port Harcourt Pirates QB #1

        // ── 10. LeagueStatEntry records ───────────────────────────────────────
        //
        // Division 1 completed fixtures: div1Fixture1 (Lions 35–14 Eagles) + div1Fixture2 (Eagles 28–21 Lions)
        // Division 2 completed fixtures: completedFixture (Pirates 28–21 Lions) + div2Fixture2 (Lions 42–7 Pirates)

        List<LeagueStatEntry> leagueStats = new java.util.ArrayList<>();

        // --- div1Fixture1: Lagos Lions 35, Abuja Eagles 14 ---
        if (passingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(player1)         // Lions QB
                    .fixture(div1Fixture1)
                    .definition(passingYardsDef)
                    .value(new BigDecimal("285.00"))
                    .recordedBy(adminUser.getId())
                    .build());
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(eaglesQB)        // Eagles QB
                    .fixture(div1Fixture1)
                    .definition(passingYardsDef)
                    .value(new BigDecimal("142.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }
        if (rushingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(lionsRB)         // Lions RB
                    .fixture(div1Fixture1)
                    .definition(rushingYardsDef)
                    .value(new BigDecimal("110.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }
        if (receivingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(player2)         // Eagles WR
                    .fixture(div1Fixture1)
                    .definition(receivingYardsDef)
                    .value(new BigDecimal("88.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }
        if (tacklesDef != null) {
            // A Lions linebacker would be ideal; reuse piratesQB user as a stand-in defender for demo
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(lionsRB)         // using RB slot — tackles would normally be a defender
                    .fixture(div1Fixture1)
                    .definition(tacklesDef)
                    .value(new BigDecimal("5.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }

        // --- div1Fixture2: Abuja Eagles 28, Lagos Lions 21 ---
        if (passingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(eaglesQB)        // Eagles QB
                    .fixture(div1Fixture2)
                    .definition(passingYardsDef)
                    .value(new BigDecimal("310.00"))
                    .recordedBy(adminUser.getId())
                    .build());
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(player1)         // Lions QB
                    .fixture(div1Fixture2)
                    .definition(passingYardsDef)
                    .value(new BigDecimal("198.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }
        if (rushingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(lionsRB)         // Lions RB
                    .fixture(div1Fixture2)
                    .definition(rushingYardsDef)
                    .value(new BigDecimal("87.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }
        if (receivingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(player2)         // Eagles WR
                    .fixture(div1Fixture2)
                    .definition(receivingYardsDef)
                    .value(new BigDecimal("132.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }

        // --- completedFixture: Port Harcourt Pirates 28, Lagos Lions 21 (Div 2) ---
        if (passingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(piratesQB)       // Pirates QB
                    .fixture(completedFixture)
                    .definition(passingYardsDef)
                    .value(new BigDecimal("240.00"))
                    .recordedBy(adminUser.getId())
                    .build());
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(player1)         // Lions QB
                    .fixture(completedFixture)
                    .definition(passingYardsDef)
                    .value(new BigDecimal("175.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }
        if (rushingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(lionsRB)         // Lions RB
                    .fixture(completedFixture)
                    .definition(rushingYardsDef)
                    .value(new BigDecimal("95.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }

        // --- div2Fixture2: Lagos Lions 42, Port Harcourt Pirates 7 (Div 2) ---
        if (passingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(player1)         // Lions QB
                    .fixture(div2Fixture2)
                    .definition(passingYardsDef)
                    .value(new BigDecimal("320.00"))
                    .recordedBy(adminUser.getId())
                    .build());
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(piratesQB)       // Pirates QB
                    .fixture(div2Fixture2)
                    .definition(passingYardsDef)
                    .value(new BigDecimal("88.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }
        if (rushingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(lionsRB)         // Lions RB
                    .fixture(div2Fixture2)
                    .definition(rushingYardsDef)
                    .value(new BigDecimal("145.00"))
                    .recordedBy(adminUser.getId())
                    .build());
        }
        if (receivingYardsDef != null) {
            leagueStats.add(LeagueStatEntry.builder()
                    .teamPlayer(player2)         // Eagles WR (appears in div2 via cross-league stats lookup)
                    .fixture(div2Fixture2)
                    .definition(receivingYardsDef)
                    .value(new BigDecimal("0.00"))   // Eagles not in this fixture — omit meaningful value
                    .recordedBy(adminUser.getId())
                    .build());
        }

        leagueStatEntryRepository.saveAll(leagueStats);
        log.info("Seeded {} league stat entries across 4 completed fixtures", leagueStats.size());

        log.info("League & Team Management seed data complete: 2 leagues, 2 divisions, 3 staff, " +
                 "3 teams, 5 players, 2 transfer records, 6 fixtures, {} league stat entries",
                 leagueStats.size());
    }

    // ======================== MEDICAL HISTORY & HEALTH CLEARANCE ========================

    private void seedMedicalData(User adminUser, User athleteUser, User athlete2User) {
        if (athleteMedicalProfileRepository.count() > 0) {
            log.info("Medical seed data already exists, skipping.");
            return;
        }

        // ── 1. Medical Profiles ─────────────────────────────────────────────

        // Athlete 1 (QB, Lagos Lions) — completed questionnaire, no major conditions
        com.feadev.eaids.medical.model.AthleteMedicalProfile profile1 =
                athleteMedicalProfileRepository.save(
                        com.feadev.eaids.medical.model.AthleteMedicalProfile.builder()
                                .athleteUserId(athleteUser.getId())
                                .bloodType("O+")
                                .existingConditions("None known")
                                .pastIllnesses("Mild asthma in childhood — resolved, no current medication required")
                                .previousSurgeries("None")
                                .hospitalAdmissions("None")
                                .knownAllergies("None")
                                .currentMedications("None")
                                .familyCardiacHistory(false)
                                .exerciseSymptoms("None")
                                .visionHearingNotes("20/20 vision (corrected). No hearing concerns.")
                                .musculoskeletalNotes("Previous right knee sprain (2024) — fully resolved")
                                .emergencyContactName("Funke Okafor")
                                .emergencyContactPhone("+2348055551234")
                                .emergencyContactRelation("Mother")
                                .questionnaireCompleted(true)
                                .reviewedByMedicalOfficer(true)
                                .submittedBy(athleteUser.getId())
                                .submittedDate(LocalDateTime.of(2026, 8, 1, 9, 0))
                                .lastReviewedDate(LocalDateTime.of(2026, 8, 5, 11, 0))
                                .build());

        // Athlete 2 (WR, Abuja Eagles) — completed questionnaire, known allergy and cardiac family history
        com.feadev.eaids.medical.model.AthleteMedicalProfile profile2 =
                athleteMedicalProfileRepository.save(
                        com.feadev.eaids.medical.model.AthleteMedicalProfile.builder()
                                .athleteUserId(athlete2User.getId())
                                .bloodType("B+")
                                .existingConditions("Mild exercise-induced bronchoconstriction — managed with Ventolin PRN")
                                .pastIllnesses("Malaria (2023). Typhoid (2022).")
                                .previousSurgeries("None")
                                .hospitalAdmissions("None")
                                .knownAllergies("Penicillin — causes urticaria and facial swelling. Documented since 2021.")
                                .currentMedications("Salbutamol inhaler (Ventolin) — as needed before intense exercise")
                                .familyCardiacHistory(true)
                                .exerciseSymptoms(
                                        "Occasional mild wheeze at very high intensity — controlled with inhaler. " +
                                        "No chest pain, no syncope, no palpitations.")
                                .visionHearingNotes("Wears contact lenses for training. Left eye dominance.")
                                .musculoskeletalNotes("No significant history")
                                .emergencyContactName("Emeka Okonkwo")
                                .emergencyContactPhone("+2348077775678")
                                .emergencyContactRelation("Father")
                                .questionnaireCompleted(true)
                                .reviewedByMedicalOfficer(false)
                                .submittedBy(athlete2User.getId())
                                .submittedDate(LocalDateTime.of(2026, 8, 2, 10, 0))
                                .outstandingClarifications(
                                        "Family cardiac history requires clarification — specify affected relative " +
                                        "and nature of condition before assessment can be finalised.")
                                .build());

        log.info("Seeded 2 athlete medical profiles");

        // ── 2. Medical Assessments ──────────────────────────────────────────

        // Athlete 1 — finalised pre-participation assessment (admin acts as MO for seed)
        com.feadev.eaids.medical.model.MedicalAssessment assessment1 =
                medicalAssessmentRepository.save(
                        com.feadev.eaids.medical.model.MedicalAssessment.builder()
                                .athleteUserId(athleteUser.getId())
                                .assessmentType(
                                        com.feadev.eaids.medical.model.enums.AssessmentType
                                                .INITIAL_PRE_PARTICIPATION.name())
                                .assessmentDate(LocalDateTime.of(2026, 8, 5, 10, 0))
                                .clinicianUserId(adminUser.getId())
                                .clinicianCredentials("MBBS, MSc Sports Medicine")
                                .bloodPressureSystolic(118)
                                .bloodPressureDiastolic(76)
                                .pulseBpm(62)
                                .cardiovascularFindings(
                                        "Regular rate and rhythm. No murmurs, gallops, or rubs. " +
                                        "No signs of cardiomegaly.")
                                .respiratoryFindings(
                                        "Clear breath sounds bilaterally. No wheeze or crackles. " +
                                        "History of childhood asthma — no current symptoms or medications.")
                                .neurologicalFindings(
                                        "Cranial nerves II–XII intact. No focal neurological deficit.")
                                .musculoskeletalFindings(
                                        "Full range of motion in all major joints. Right knee fully rehabilitated. " +
                                        "No tenderness, instability, or effusion detected.")
                                .investigationsResults(
                                        "ECG: Normal sinus rhythm, no ST changes. " +
                                        "Full blood count: Within normal limits.")
                                .clinicalFindings(
                                        "No contraindications to full participation identified. " +
                                        "Previous right knee sprain fully resolved with no functional limitation.")
                                .recommendations(
                                        "Athlete is fit for all training and competitive activities. " +
                                        "Annual review recommended. Continue monitoring for any recurrence " +
                                        "of knee symptoms.")
                                .participationRestrictions("None")
                                .referralsRequired("None")
                                .followUpDate(LocalDate.of(2027, 8, 5))
                                .status(com.feadev.eaids.medical.model.enums.AssessmentStatus
                                        .FINALISED.name())
                                .finalisedAt(LocalDateTime.of(2026, 8, 5, 11, 30))
                                .build());

        // Athlete 2 — assessment in UNDER_REVIEW (cardiac family history outstanding)
        com.feadev.eaids.medical.model.MedicalAssessment assessment2 =
                medicalAssessmentRepository.save(
                        com.feadev.eaids.medical.model.MedicalAssessment.builder()
                                .athleteUserId(athlete2User.getId())
                                .assessmentType(
                                        com.feadev.eaids.medical.model.enums.AssessmentType
                                                .INITIAL_PRE_PARTICIPATION.name())
                                .assessmentDate(LocalDateTime.of(2026, 8, 6, 14, 0))
                                .clinicianUserId(adminUser.getId())
                                .clinicianCredentials("MBBS, MSc Sports Medicine")
                                .bloodPressureSystolic(122)
                                .bloodPressureDiastolic(78)
                                .pulseBpm(68)
                                .cardiovascularFindings(
                                        "Regular rate and rhythm. No audible murmurs. " +
                                        "Family cardiac history noted — further clarification pending.")
                                .respiratoryFindings(
                                        "Mild expiratory wheeze noted at rest. Responds to Ventolin. " +
                                        "Exercise-induced bronchoconstriction documented — managed.")
                                .neurologicalFindings("No abnormality detected.")
                                .musculoskeletalFindings("No abnormality detected.")
                                .investigationsResults(
                                        "ECG: Normal sinus rhythm. " +
                                        "Spirometry: FEV1/FVC 0.74 — mildly reduced, consistent with " +
                                        "mild obstructive pattern.")
                                .clinicalFindings(
                                        "Athlete has known penicillin allergy — documented in emergency record. " +
                                        "Exercise-induced bronchoconstriction well-controlled with Ventolin PRN. " +
                                        "Family cardiac history requires clarification before full clearance.")
                                .recommendations(
                                        "Conditional training clearance pending cardiology review of family history. " +
                                        "Ventolin inhaler must be available at all training sessions and matches. " +
                                        "Medical team must be informed of penicillin allergy at every event.")
                                .participationRestrictions(
                                        "Light non-contact training only pending cardiac review outcome. " +
                                        "Ventolin inhaler required at all sessions.")
                                .referralsRequired(
                                        "Cardiology referral for family history risk stratification. " +
                                        "Athlete to bring family member medical records to next appointment.")
                                .followUpDate(LocalDate.of(2026, 9, 6))
                                .status(com.feadev.eaids.medical.model.enums.AssessmentStatus
                                        .UNDER_REVIEW.name())
                                .build());

        log.info("Seeded 2 medical assessments (1 FINALISED, 1 UNDER_REVIEW)");

        // ── 3. Injuries ─────────────────────────────────────────────────────

        // Athlete 1 — resolved knee sprain (historical, CLOSED)
        com.feadev.eaids.medical.model.AthleteInjury injury1 =
                athleteInjuryRepository.save(
                        com.feadev.eaids.medical.model.AthleteInjury.builder()
                                .athleteUserId(athleteUser.getId())
                                .incidentDate(LocalDateTime.of(2026, 7, 10, 14, 30))
                                .reportedDate(LocalDateTime.of(2026, 7, 10, 15, 0))
                                .incidentLocation("Training pitch — Onikan Stadium, Lagos")
                                .injuryContext(
                                        com.feadev.eaids.medical.model.enums.InjuryContext
                                                .TRAINING.name())
                                .bodyRegion(
                                        com.feadev.eaids.medical.model.enums.BodyRegion
                                                .KNEE.name())
                                .injuryCategory(
                                        com.feadev.eaids.medical.model.enums.InjuryCategory
                                                .SPRAIN.name())
                                .isSuspectedConcussion(false)
                                .mechanismOfInjury(
                                        "Non-contact twisting injury during cutting drill. " +
                                        "Planted right foot while changing direction at speed.")
                                .symptomsReported(
                                        "Medial knee pain, swelling, and difficulty weight-bearing immediately " +
                                        "after incident. No locking or giving way reported.")
                                .severity("MODERATE")
                                .confirmedDiagnosis(
                                        "Grade II medial collateral ligament (MCL) sprain, right knee. " +
                                        "Confirmed by clinical examination and MRI.")
                                .initialResponse(
                                        "RICE protocol applied immediately (Rest, Ice, Compression, Elevation). " +
                                        "Athlete removed from training. Crutches provided.")
                                .referralDetails(
                                        "Referred to physiotherapy for structured rehabilitation programme. " +
                                        "MRI ordered — completed 2026-07-12.")
                                .treatmentPlan(
                                        "4-week physiotherapy programme: RICE phase 1–3 days, " +
                                        "progressive range-of-motion 4–14 days, strengthening 2–4 weeks. " +
                                        "NSAIDs (Ibuprofen 400mg TDS) for 5 days.")
                                .participationRestrictions(
                                        "No training or competition for minimum 4 weeks from injury date.")
                                .rehabilitationStatus("Completed 4-week physiotherapy programme")
                                .returnToSportDecision(
                                        "Cleared for return to full training and competition — " +
                                        "2026-08-08 by Dr A. Adeyemi. Functional assessment passed.")
                                .status(com.feadev.eaids.medical.model.enums.InjuryStatus
                                        .CLOSED.name())
                                .reportedBy(adminUser.getId())
                                .recordedBy(adminUser.getId())
                                .build());

        // Athlete 2 — suspected concussion, currently AWAITING_ASSESSMENT
        com.feadev.eaids.medical.model.AthleteInjury injury2 =
                athleteInjuryRepository.save(
                        com.feadev.eaids.medical.model.AthleteInjury.builder()
                                .athleteUserId(athlete2User.getId())
                                .incidentDate(LocalDateTime.of(2026, 9, 14, 16, 45))
                                .reportedDate(LocalDateTime.of(2026, 9, 14, 17, 0))
                                .incidentLocation("National Stadium, Abuja — Division 1 Match")
                                .injuryContext(
                                        com.feadev.eaids.medical.model.enums.InjuryContext
                                                .COMPETITION.name())
                                .bodyRegion(
                                        com.feadev.eaids.medical.model.enums.BodyRegion
                                                .HEAD_NECK.name())
                                .injuryCategory(
                                        com.feadev.eaids.medical.model.enums.InjuryCategory
                                                .HEAD_INJURY.name())
                                .isSuspectedConcussion(true)
                                .mechanismOfInjury(
                                        "Head-to-head collision with opponent during contested catch in " +
                                        "the end zone. Athlete fell backwards and struck the ground.")
                                .symptomsReported(
                                        "Appeared dazed immediately after collision. Reported headache, " +
                                        "mild confusion, and light sensitivity. Denied loss of consciousness " +
                                        "but teammates noted brief unresponsiveness (~5 seconds). " +
                                        "Nausea reported 10 minutes post-incident.")
                                .initialResponse(
                                        "Athlete removed from field immediately under concussion protocol. " +
                                        "Assessed pitch-side — not cleared to return to play. " +
                                        "Standard sideline concussion assessment performed by team official.")
                                .status(com.feadev.eaids.medical.model.enums.InjuryStatus
                                        .AWAITING_ASSESSMENT.name())
                                .reportedBy(adminUser.getId())
                                .recordedBy(adminUser.getId())
                                .build());

        log.info("Seeded 2 athlete injuries (1 CLOSED knee sprain, 1 AWAITING_ASSESSMENT suspected concussion)");

        // ── 4. Medical Clearances ────────────────────────────────────────────

        // Athlete 1 — CLEARED (full participation, linked to FINALISED assessment)
        com.feadev.eaids.medical.model.MedicalClearance clearance1 =
                medicalClearanceRepository.save(
                        com.feadev.eaids.medical.model.MedicalClearance.builder()
                                .athleteUserId(athleteUser.getId())
                                .issuedBy(adminUser.getId())
                                .relatedAssessmentId(assessment1.getId())
                                .relatedInjuryId(injury1.getId())
                                .activityScope("All training and competitive activities")
                                .status(com.feadev.eaids.medical.model.enums.ClearanceStatus
                                        .CLEARED.name())
                                .decisionDate(LocalDateTime.of(2026, 8, 5, 12, 0))
                                .effectiveDate(LocalDate.of(2026, 8, 6))
                                .expiryDate(LocalDate.of(2027, 8, 5))
                                .participationRestrictions("None")
                                .requiredAccommodations("None")
                                .followUpRequirements(
                                        "Annual pre-participation assessment due by 2027-08-05.")
                                .build());

        // Athlete 2 — NOT_CLEARED (pending cardiac review and concussion assessment)
        com.feadev.eaids.medical.model.MedicalClearance clearance2 =
                medicalClearanceRepository.save(
                        com.feadev.eaids.medical.model.MedicalClearance.builder()
                                .athleteUserId(athlete2User.getId())
                                .issuedBy(adminUser.getId())
                                .relatedAssessmentId(assessment2.getId())
                                .relatedInjuryId(injury2.getId())
                                .activityScope("All training and competitive activities")
                                .status(com.feadev.eaids.medical.model.enums.ClearanceStatus
                                        .NOT_CLEARED.name())
                                .decisionDate(LocalDateTime.of(2026, 9, 15, 9, 0))
                                .participationRestrictions(
                                        "No training or competition until concussion assessment is completed " +
                                        "and cardiac family history is reviewed and risk-stratified. " +
                                        "Ventolin inhaler required at all times.")
                                .conditionalReason(
                                        "1. Suspected concussion sustained 2026-09-14 — formal medical " +
                                        "assessment not yet completed. Athlete must not participate until " +
                                        "cleared by a qualified medical professional following concussion " +
                                        "protocol. " +
                                        "2. Cardiac family history outstanding — cardiology referral required " +
                                        "before unrestricted participation clearance can be issued.")
                                .followUpRequirements(
                                        "1. Concussion protocol assessment to be completed by qualified " +
                                        "medical officer within 48 hours. " +
                                        "2. Cardiology appointment to be arranged. " +
                                        "3. Updated clearance decision to follow completed assessments.")
                                .build());

        log.info("Seeded 2 medical clearances (athlete1: CLEARED, athlete2: NOT_CLEARED)");
        log.info("Medical seed data complete: 2 profiles, 2 assessments, 2 injuries, 2 clearances");
    }

    // ======================== ENHANCEMENT FEATURE SEED USERS ========================

    private void seedMedicalOfficerUser(String encodedPassword) {
        String email = "mo@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Medical officer user already exists, skipping.");
            return;
        }
        User mo = User.builder()
                .email(email)
                .phoneNumber("+2348000000009")
                .password(encodedPassword)
                .role(UserRole.ROLE_MEDICAL_OFFICER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();
        userRepository.save(mo);
        log.info("Seeded medical officer user: {}", email);
    }

    private void seedPhysiotherapistUser(String encodedPassword) {
        String email = "physio@eaids.com";
        if (userRepository.existsByEmail(email)) {
            log.info("Physiotherapist user already exists, skipping.");
            return;
        }
        User physio = User.builder()
                .email(email)
                .phoneNumber("+2348000000010")
                .password(encodedPassword)
                .role(UserRole.ROLE_PHYSIOTHERAPIST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();
        userRepository.save(physio);
        log.info("Seeded physiotherapist user: {}", email);
    }

    // ======================== PHASE 19: COMMENTARY, POST-GAME STATS, INJURY RISK ========================

    private void seedEnhancementData(User adminUser, User athleteUser, User athlete2User) {
        if (matchCommentaryEventRepository.count() > 0) {
            log.info("Enhancement seed data (commentary / post-game / injury risk) already exists, skipping.");
            return;
        }

        // ── Find the IN_PROGRESS fixture and completed fixtures created in Phase 17 ──

        // We need the live fixture for commentary and the completed fixtures for post-game.
        // The IN_PROGRESS fixture is seeded here; the completed ones were created in seedLeagueData.
        List<MatchFixture> allFixtures = matchFixtureRepository.findAll();
        if (allFixtures.isEmpty()) {
            log.warn("No fixtures found — skipping enhancement seed data. Run Phase 17 first.");
            return;
        }

        // Find or create an IN_PROGRESS fixture for live commentary demo
        MatchFixture liveFixture = allFixtures.stream()
                .filter(f -> f.getStatus() == FixtureStatus.IN_PROGRESS)
                .findFirst()
                .orElseGet(() -> {
                    // Find the SCHEDULED fixture and clone it as IN_PROGRESS for demo purposes
                    MatchFixture scheduled = allFixtures.stream()
                            .filter(f -> f.getStatus() == FixtureStatus.SCHEDULED)
                            .findFirst().orElse(null);
                    if (scheduled == null) return null;

                    MatchFixture live = matchFixtureRepository.save(MatchFixture.builder()
                            .league(scheduled.getLeague())
                            .division(scheduled.getDivision())
                            .homeTeam(scheduled.getHomeTeam())
                            .awayTeam(scheduled.getAwayTeam())
                            .scheduledDate(LocalDateTime.now().minusHours(2))
                            .venue(scheduled.getVenue() != null ? scheduled.getVenue() : "Demo Venue")
                            .status(FixtureStatus.IN_PROGRESS)
                            .homeScore(14)
                            .awayScore(7)
                            .refereeUserId(adminUser.getId())
                            .notes("Live demo fixture for commentary seed data.")
                            .build());
                    log.info("Created IN_PROGRESS demo fixture: {}", live.getId());
                    return live;
                });

        // ── Part A: Commentary Events ─────────────────────────────────────────────────

        if (liveFixture != null) {
            seedCommentaryEvents(liveFixture, adminUser);
        }

        // ── Part B: Post-Game Summaries ───────────────────────────────────────────────

        List<MatchFixture> completedFixtures = allFixtures.stream()
                .filter(f -> f.getStatus() == FixtureStatus.COMPLETED)
                .collect(java.util.stream.Collectors.toList());

        seedPostGameSummaries(completedFixtures, adminUser);

        // ── Part C: Additional injuries for meaningful risk scores ─────────────────────

        seedAdditionalInjuries(adminUser, athleteUser, athlete2User);

        log.info("Phase 19 (enhancement features) seed data complete.");
    }

    // ─────────────────────────────────────────────────────────────────────────────────
    // Commentary Events
    // ─────────────────────────────────────────────────────────────────────────────────

    private void seedCommentaryEvents(MatchFixture liveFixture, User adminUser) {
        String actor = adminUser.getEmail();
        LocalDateTime base = LocalDateTime.now().minusHours(2);

        List<MatchCommentaryEvent> events = new java.util.ArrayList<>();

        // Quarter 1 start
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.QUARTER_START)
                .quarter(1)
                .homeScore(0)
                .awayScore(0)
                .description("Quarter 1 underway! " +
                        liveFixture.getHomeTeam().getName() + " receive the kickoff.")
                .createdBy(actor)
                .build());

        // First score — home TD
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.SCORE_UPDATE)
                .quarter(1)
                .eventTimeSeconds(487)
                .homeScore(7)
                .awayScore(0)
                .description("TOUCHDOWN " + liveFixture.getHomeTeam().getName() +
                        "! #12 connects with #80 for a 42-yard strike. Extra point is good.")
                .playerEventSubtype("TD")
                .createdBy(actor)
                .build());

        // Play-by-play commentary
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.COMMENTARY)
                .quarter(1)
                .eventTimeSeconds(610)
                .homeScore(7)
                .awayScore(0)
                .description("Excellent pocket presence from the quarterback — " +
                        "stepped up and delivered under pressure.")
                .createdBy(actor)
                .build());

        // Penalty
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.PENALTY)
                .quarter(1)
                .eventTimeSeconds(780)
                .homeScore(7)
                .awayScore(0)
                .description("Flag on the play — holding on the offense. 10-yard penalty, " +
                        liveFixture.getHomeTeam().getName() + " back to their own 25.")
                .playerEventSubtype("PENALTY_FLAG")
                .createdBy(actor)
                .build());

        // Quarter 1 end
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.QUARTER_END)
                .quarter(1)
                .homeScore(7)
                .awayScore(0)
                .description("End of Quarter 1. " + liveFixture.getHomeTeam().getName() +
                        " lead 7-0.")
                .createdBy(actor)
                .build());

        // Quarter 2 start
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.QUARTER_START)
                .quarter(2)
                .homeScore(7)
                .awayScore(0)
                .description("Quarter 2 kicks off.")
                .createdBy(actor)
                .build());

        // Away TD
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.SCORE_UPDATE)
                .quarter(2)
                .eventTimeSeconds(1320)
                .homeScore(7)
                .awayScore(7)
                .description("TOUCHDOWN " + liveFixture.getAwayTeam().getName() +
                        "! 35-yard rushing touchdown brings them level. Extra point good. 7-7.")
                .playerEventSubtype("TD")
                .createdBy(actor)
                .build());

        // INT
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.PLAYER_EVENT)
                .quarter(2)
                .eventTimeSeconds(1620)
                .homeScore(7)
                .awayScore(7)
                .description("INTERCEPTION! " + liveFixture.getHomeTeam().getName() +
                        " defense makes a tremendous read — picks off the pass at the 30-yard line!")
                .playerEventSubtype("INT")
                .createdBy(actor)
                .build());

        // Second home TD — now leads 14-7
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.SCORE_UPDATE)
                .quarter(2)
                .eventTimeSeconds(1740)
                .homeScore(14)
                .awayScore(7)
                .description("TOUCHDOWN " + liveFixture.getHomeTeam().getName() +
                        "! Capitalise immediately off the turnover. 14-7 at halftime. Extra point good.")
                .playerEventSubtype("TD")
                .createdBy(actor)
                .build());

        // Halftime commentary
        events.add(MatchCommentaryEvent.builder()
                .fixture(liveFixture)
                .eventType(GameEventType.QUARTER_END)
                .quarter(2)
                .homeScore(14)
                .awayScore(7)
                .description("HALFTIME: " + liveFixture.getHomeTeam().getName() + " 14 – " +
                        liveFixture.getAwayTeam().getName() + " 7. " +
                        "Strong defensive showing in Q2 — turnover was the difference.")
                .createdBy(actor)
                .build());

        // Assign timestamps offset from base so ordering is deterministic
        // (createdDate is set by @CreatedDate auditing, but we need the fixture to be
        //  in session context — so we persist first, then trust audit)
        matchCommentaryEventRepository.saveAll(events);
        log.info("Seeded {} commentary events for live fixture {}", events.size(), liveFixture.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────────────
    // Post-Game Summaries
    // ─────────────────────────────────────────────────────────────────────────────────

    private void seedPostGameSummaries(List<MatchFixture> completedFixtures, User adminUser) {
        if (postGameSummaryRepository.count() > 0) {
            log.info("Post-game summaries already exist, skipping.");
            return;
        }
        if (completedFixtures.isEmpty()) {
            log.warn("No completed fixtures found — skipping post-game summary seed.");
            return;
        }

        List<PostGameSummary> summaries = new java.util.ArrayList<>();

        // Build one summary per completed fixture using the team players already in it.
        // We only create summaries for fixtures where we have stat entries (i.e. where
        // TeamPlayers are reachable). We compose the data manually here to mirror what
        // PostGameStatsServiceImpl would compute, so Swagger / the API has data to show.
        for (MatchFixture fixture : completedFixtures) {

            List<com.feadev.eaids.league.model.LeagueStatEntry> entries =
                    leagueStatEntryRepository.findByFixtureId(fixture.getId());
            if (entries.isEmpty()) continue;

            // Group entries by TeamPlayer
            java.util.Map<UUID, java.util.List<com.feadev.eaids.league.model.LeagueStatEntry>> byPlayer =
                    entries.stream().collect(java.util.stream.Collectors.groupingBy(
                            e -> e.getTeamPlayer().getId()));

            for (java.util.Map.Entry<UUID, java.util.List<com.feadev.eaids.league.model.LeagueStatEntry>> entry
                    : byPlayer.entrySet()) {

                TeamPlayer tp = entry.getValue().get(0).getTeamPlayer();
                java.util.List<com.feadev.eaids.league.model.LeagueStatEntry> playerEntries = entry.getValue();

                // Skip if already exists (upsert guard)
                if (postGameSummaryRepository.findByFixtureIdAndTeamPlayerId(
                        fixture.getId(), tp.getId()).isPresent()) {
                    continue;
                }

                BigDecimal passingYards   = sumStatByName(playerEntries, "Passing Yards");
                BigDecimal rushingYards   = sumStatByName(playerEntries, "Rushing Yards");
                BigDecimal receivingYards = sumStatByName(playerEntries, "Receiving Yards");
                BigDecimal tackles        = sumStatByName(playerEntries, "Tackles");
                BigDecimal touchdowns     = sumStatByName(playerEntries, "Touchdowns");
                BigDecimal interceptions  = sumStatByName(playerEntries, "Interceptions");
                BigDecimal sacks          = sumStatByName(playerEntries, "Sacks");
                BigDecimal fumbles        = sumStatByName(playerEntries, "Fumbles");
                BigDecimal penalties      = sumStatByName(playerEntries, "Penalties");

                summaries.add(PostGameSummary.builder()
                        .fixture(fixture)
                        .teamPlayer(tp)
                        .athleteUserId(tp.getAthlete().getId())
                        .teamId(tp.getTeam().getId())
                        .totalPassingYards(nullIfZero(passingYards))
                        .totalRushingYards(nullIfZero(rushingYards))
                        .totalReceivingYards(nullIfZero(receivingYards))
                        .totalTackles(nullIfZero(tackles))
                        .touchdowns(nullIfZero(touchdowns))
                        .interceptions(nullIfZero(interceptions))
                        .sacks(nullIfZero(sacks))
                        .fumbles(nullIfZero(fumbles))
                        .penalties(nullIfZero(penalties))
                        .customStats(null)
                        .computedAt(LocalDateTime.now())
                        .build());
            }
        }

        if (!summaries.isEmpty()) {
            postGameSummaryRepository.saveAll(summaries);
        }
        log.info("Seeded {} post-game summaries across {} completed fixtures",
                summaries.size(), completedFixtures.size());
    }

    private BigDecimal sumStatByName(
            java.util.List<com.feadev.eaids.league.model.LeagueStatEntry> entries,
            String statName) {
        return entries.stream()
                .filter(e -> statName.equalsIgnoreCase(e.getDefinition().getStatName()))
                .map(com.feadev.eaids.league.model.LeagueStatEntry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nullIfZero(BigDecimal value) {
        return (value == null || value.compareTo(BigDecimal.ZERO) == 0) ? null : value;
    }

    // ─────────────────────────────────────────────────────────────────────────────────
    // Additional injuries for non-trivial risk scores
    // ─────────────────────────────────────────────────────────────────────────────────

    /**
     * Seeds additional injury records so the risk engine produces interesting output:
     * <ul>
     *   <li>Athlete 1 (QB) — two additional injuries in the last 12 months →
     *       HIGH_INJURY_FREQUENCY flag, score MEDIUM/HIGH depending on full history.</li>
     *   <li>Athlete 2 (WR) — already has a suspected concussion; add one more HEAD_NECK
     *       injury and an ankle sprain to trigger RECURRENT_BODY_REGION and ACTIVE_INJURY.</li>
     * </ul>
     */
    private void seedAdditionalInjuries(User adminUser, User athleteUser, User athlete2User) {
        // Guard: only add if athlete1 has exactly 1 injury (the knee sprain from Phase 18)
        long athlete1Count = athleteInjuryRepository.findByAthleteUserId(athleteUser.getId()).size();
        if (athlete1Count > 1) {
            log.info("Additional injury seed data already exists, skipping.");
            return;
        }

        List<com.feadev.eaids.medical.model.AthleteInjury> extras = new java.util.ArrayList<>();

        // ── Athlete 1: shoulder strain (6 months ago, RESOLVED) ──────────────────
        extras.add(com.feadev.eaids.medical.model.AthleteInjury.builder()
                .athleteUserId(athleteUser.getId())
                .incidentDate(LocalDateTime.now().minusMonths(6))
                .reportedDate(LocalDateTime.now().minusMonths(6).plusHours(1))
                .incidentLocation("Training — Onikan Stadium")
                .injuryContext(com.feadev.eaids.medical.model.enums.InjuryContext.TRAINING.name())
                .bodyRegion(com.feadev.eaids.medical.model.enums.BodyRegion.SHOULDER_UPPER_ARM.name())
                .injuryCategory(com.feadev.eaids.medical.model.enums.InjuryCategory.STRAIN.name())
                .isSuspectedConcussion(false)
                .mechanismOfInjury("Overhead throwing motion — overuse strain.")
                .symptomsReported("Anterior shoulder pain, reduced range of motion.")
                .severity("MILD")
                .confirmedDiagnosis("Rotator cuff strain, left shoulder.")
                .initialResponse("Ice, rest, NSAIDs.")
                .rehabilitationStatus("Completed")
                .status(com.feadev.eaids.medical.model.enums.InjuryStatus.RESOLVED.name())
                .reportedBy(adminUser.getId())
                .recordedBy(adminUser.getId())
                .build());

        // ── Athlete 1: ankle sprain (3 months ago, CLOSED) ───────────────────────
        extras.add(com.feadev.eaids.medical.model.AthleteInjury.builder()
                .athleteUserId(athleteUser.getId())
                .incidentDate(LocalDateTime.now().minusMonths(3))
                .reportedDate(LocalDateTime.now().minusMonths(3).plusHours(2))
                .incidentLocation("Onikan Stadium — pre-season scrimmage")
                .injuryContext(com.feadev.eaids.medical.model.enums.InjuryContext.CAMP.name())
                .bodyRegion(com.feadev.eaids.medical.model.enums.BodyRegion.ANKLE_FOOT.name())
                .injuryCategory(com.feadev.eaids.medical.model.enums.InjuryCategory.SPRAIN.name())
                .isSuspectedConcussion(false)
                .mechanismOfInjury("Rolled left ankle on uneven turf during scramble.")
                .symptomsReported("Lateral ankle pain and swelling. Weight-bearing painful.")
                .severity("MILD")
                .confirmedDiagnosis("Grade I lateral ankle sprain.")
                .initialResponse("RICE, ankle brace fitted.")
                .rehabilitationStatus("Completed")
                .status(com.feadev.eaids.medical.model.enums.InjuryStatus.CLOSED.name())
                .reportedBy(adminUser.getId())
                .recordedBy(adminUser.getId())
                .build());

        // Athlete 1 now has 3 injuries in 12 months → triggers HIGH_INJURY_FREQUENCY (+2 pts) → MEDIUM risk

        // ── Athlete 2: second HEAD_NECK injury (4 months ago, RESOLVED) ──────────
        // Combined with the existing suspected concussion → RECURRENT_BODY_REGION:HEAD_NECK
        extras.add(com.feadev.eaids.medical.model.AthleteInjury.builder()
                .athleteUserId(athlete2User.getId())
                .incidentDate(LocalDateTime.now().minusMonths(4))
                .reportedDate(LocalDateTime.now().minusMonths(4).plusHours(1))
                .incidentLocation("Training — National Stadium Abuja")
                .injuryContext(com.feadev.eaids.medical.model.enums.InjuryContext.TRAINING.name())
                .bodyRegion(com.feadev.eaids.medical.model.enums.BodyRegion.HEAD_NECK.name())
                .injuryCategory(com.feadev.eaids.medical.model.enums.InjuryCategory.CONTUSION.name())
                .isSuspectedConcussion(false)
                .mechanismOfInjury(
                        "Caught stray elbow to the back of the head during a contact drill. " +
                        "No loss of consciousness.")
                .symptomsReported("Mild headache, no visual disturbance, no confusion. " +
                        "Cleared for return to play after 30-minute observation.")
                .severity("MILD")
                .confirmedDiagnosis("Minor head contusion — no concussion criteria met.")
                .initialResponse("Removed from practice for 30 minutes. " +
                        "Monitored under concussion protocol as precaution.")
                .rehabilitationStatus("Completed")
                .status(com.feadev.eaids.medical.model.enums.InjuryStatus.RESOLVED.name())
                .reportedBy(adminUser.getId())
                .recordedBy(adminUser.getId())
                .build());

        // ── Athlete 2: knee sprain (2 months ago, UNDER_TREATMENT — active injury) ──
        extras.add(com.feadev.eaids.medical.model.AthleteInjury.builder()
                .athleteUserId(athlete2User.getId())
                .incidentDate(LocalDateTime.now().minusMonths(2))
                .reportedDate(LocalDateTime.now().minusMonths(2).plusHours(3))
                .incidentLocation("National Stadium, Abuja — Division 1 training")
                .injuryContext(com.feadev.eaids.medical.model.enums.InjuryContext.TRAINING.name())
                .bodyRegion(com.feadev.eaids.medical.model.enums.BodyRegion.KNEE.name())
                .injuryCategory(com.feadev.eaids.medical.model.enums.InjuryCategory.SPRAIN.name())
                .isSuspectedConcussion(false)
                .mechanismOfInjury(
                        "Hyperextension during a jump ball contest. " +
                        "Landed awkwardly on straight leg.")
                .symptomsReported(
                        "Posterior knee pain, mild swelling. " +
                        "Stable Lachman test. Unable to complete training session.")
                .severity("MODERATE")
                .confirmedDiagnosis("Grade I posterior cruciate ligament sprain, right knee.")
                .initialResponse("RICE protocol. NSAIDs prescribed. Crutches issued.")
                .treatmentPlan(
                        "3–4 week structured physio programme. " +
                        "Focus on quadriceps strengthening and proprioception.")
                .rehabilitationStatus("Ongoing — week 3 of 4")
                .participationRestrictions("Non-contact training only. No competition.")
                .status(com.feadev.eaids.medical.model.enums.InjuryStatus.UNDER_TREATMENT.name())
                .reportedBy(adminUser.getId())
                .recordedBy(adminUser.getId())
                .build());

        // Athlete 2 now has:
        //  - CONCUSSION_HISTORY       (existing suspected concussion, < 24 months)  → +3
        //  - ACTIVE_INJURY            (knee, UNDER_TREATMENT)                       → +1
        //  - RECURRENT_BODY_REGION:HEAD_NECK (2 head/neck injuries)                 → +1
        //  - PRE_EXISTING_CONDITIONS  (exercise-induced bronchoconstriction)         → +1
        //  - CARDIAC_RISK_FLAG        (familyCardiacHistory = true on profile)       → +1
        //  - ONGOING_REHABILITATION   (knee injury has rehabilitationStatus)         → +1
        //  Total score = 8 → CRITICAL

        athleteInjuryRepository.saveAll(extras);
        log.info("Seeded {} additional injury records "
                + "(athlete1: shoulder strain + ankle sprain → MEDIUM risk; "
                + "athlete2: head contusion + knee sprain → CRITICAL risk)",
                extras.size());
    }
}
