package com.feadev.eaids.registrations.model;

import com.feadev.eaids.registrations.model.enums.UserRole;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.AfterContainer;
import net.jqwik.api.lifecycle.BeforeContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Property-based test (jqwik) for Bug Condition: ROLE_SCOUT Rejected by ENUM Columns.
 *
 * <p>This test attempts to persist a User with role=ROLE_SCOUT and a CampEvaluator with
 * role=ROLE_SCOUT into a MySQL database that was created from schema.sql WITHOUT the V3
 * migration applied. On unfixed code, the test is EXPECTED TO FAIL with DataTruncation
 * ("Data truncated for column 'role' at row 1"), confirming the bug exists.</p>
 *
 * <p>After the V3 migration is implemented and applied, this test will PASS, confirming
 * the fix resolves the bug.</p>
 *
 * <p><b>Validates: Requirements 1.1, 1.2, 1.3</b></p>
 *
 * <p>Property 1: Bug Condition - ROLE_SCOUT Accepted by ENUM Columns</p>
 */
@Tag("docker")
class RoleEnumBugConditionPropertyTest {

    private static final String CONTAINER_NAME = "eaids-role-enum-bug-test-mysql";
    private static final int HOST_PORT = 13307;
    private static final String DB_NAME = "eaids_role_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static String jdbcUrl;

    @BeforeContainer
    static void startContainer() throws Exception {
        // Stop and remove any existing container from a previous run
        exec("docker", "rm", "-f", CONTAINER_NAME);

        // Start a MySQL container directly via Docker CLI
        exec("docker", "run", "-d",
                "--name", CONTAINER_NAME,
                "-e", "MYSQL_DATABASE=" + DB_NAME,
                "-e", "MYSQL_USER=" + DB_USER,
                "-e", "MYSQL_PASSWORD=" + DB_PASS,
                "-e", "MYSQL_ROOT_PASSWORD=root",
                "-p", HOST_PORT + ":3306",
                "mysql:8.0");

        jdbcUrl = "jdbc:mysql://localhost:" + HOST_PORT + "/" + DB_NAME;

        // Wait for MySQL to be ready
        waitForMysql();

        // Initialize schema from schema.sql (this creates the OLD ENUM without ROLE_SCOUT
        // if schema.sql is being tested as-is, OR with ROLE_SCOUT if schema.sql already has it).
        // The bug is that CREATE TABLE IF NOT EXISTS does NOT modify existing columns,
        // so we simulate the OLD state by creating the table without ROLE_SCOUT first.
        try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {
            // Create users table with OLD ENUM definition (without ROLE_SCOUT)
            String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id BINARY(16) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        phone_number VARCHAR(20) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role ENUM('ROLE_ATHLETE', 'ROLE_COACH', 'ROLE_ADMIN') NOT NULL,
                        status ENUM('PENDING_VERIFICATION', 'PENDING_CONSENT', 'UNDER_REVIEW', 'ACTIVE', 'REJECTED', 'SUSPENDED', 'ARCHIVED') NOT NULL,
                        email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                        phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
                        enabled BOOLEAN NOT NULL DEFAULT FALSE,
                        created_date DATETIME(6) NOT NULL,
                        last_modified_date DATETIME(6) NOT NULL,
                        created_by VARCHAR(255),
                        last_modified_by VARCHAR(255),
                        PRIMARY KEY (id),
                        CONSTRAINT uk_users_email UNIQUE (email),
                        CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createUsersTable);
            }

            // Create camps table (needed as FK target for camp_evaluators)
            String createCampsTable = """
                    CREATE TABLE IF NOT EXISTS camps (
                        id BINARY(16) NOT NULL,
                        name VARCHAR(200) NOT NULL,
                        description TEXT,
                        camp_type ENUM('IDENTIFICATION', 'DEVELOPMENT', 'ELITE_SELECTION', 'REGIONAL', 'NATIONAL', 'INTERNATIONAL') NOT NULL,
                        status ENUM('DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
                        age_category ENUM('UNDER_14', 'UNDER_16', 'UNDER_18', 'UNDER_21', 'OPEN') NOT NULL,
                        location VARCHAR(200) NOT NULL,
                        country VARCHAR(100) NOT NULL,
                        state VARCHAR(100),
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL,
                        registration_deadline DATE,
                        capacity INT NOT NULL,
                        current_enrollment INT NOT NULL DEFAULT 0,
                        aggregation_strategy ENUM('AVERAGE', 'WEIGHTED_AVERAGE', 'MEDIAN') NOT NULL DEFAULT 'AVERAGE',
                        created_date DATETIME(6) NOT NULL,
                        last_modified_date DATETIME(6) NOT NULL,
                        created_by VARCHAR(255),
                        last_modified_by VARCHAR(255),
                        PRIMARY KEY (id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createCampsTable);
            }

            // Create camp_evaluators table with OLD ENUM definition (only ROLE_COACH, no ROLE_SCOUT)
            String createCampEvaluatorsTable = """
                    CREATE TABLE IF NOT EXISTS camp_evaluators (
                        id BINARY(16) NOT NULL,
                        camp_id BINARY(16) NOT NULL,
                        user_id BINARY(16) NOT NULL,
                        role ENUM('ROLE_COACH') NOT NULL,
                        is_lead BOOLEAN NOT NULL DEFAULT FALSE,
                        created_date DATETIME(6) NOT NULL,
                        PRIMARY KEY (id),
                        CONSTRAINT uk_evaluator_camp_user UNIQUE (camp_id, user_id),
                        CONSTRAINT fk_evaluators_camp FOREIGN KEY (camp_id) REFERENCES camps (id),
                        CONSTRAINT fk_evaluators_user FOREIGN KEY (user_id) REFERENCES users (id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createCampEvaluatorsTable);
            }
        }
    }

    @AfterContainer
    static void stopContainer() {
        try {
            exec("docker", "rm", "-f", CONTAINER_NAME);
        } catch (Exception e) {
            // Best effort cleanup
        }
    }

    /**
     * Property: Inserting a user with role=ROLE_SCOUT into the users table should succeed.
     *
     * <p>On UNFIXED code (without V3 migration), this test will FAIL because the MySQL ENUM
     * column for users.role does not include ROLE_SCOUT, causing "Data truncated for column 'role'".</p>
     *
     * <p>After the V3 migration is applied, this test will PASS.</p>
     *
     * <p><b>Validates: Requirements 1.1, 1.2</b></p>
     */
    @Property(tries = 5)
    void roleScoutCanBePersistedInUsersTable(@ForAll("scoutUserData") UserInsertData userData) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {

            String sql = "INSERT INTO users (id, email, phone_number, password, role, status, " +
                    "email_verified, phone_verified, enabled, created_date, last_modified_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            LocalDateTime now = LocalDateTime.now();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBytes(1, uuidToBytes(userData.id()));
                ps.setString(2, userData.email());
                ps.setString(3, userData.phoneNumber());
                ps.setString(4, "encoded-password-hash");
                ps.setString(5, UserRole.ROLE_SCOUT.name());
                ps.setString(6, "ACTIVE");
                ps.setBoolean(7, true);
                ps.setBoolean(8, false);
                ps.setBoolean(9, true);
                ps.setTimestamp(10, Timestamp.valueOf(now));
                ps.setTimestamp(11, Timestamp.valueOf(now));

                int rowsInserted = ps.executeUpdate();
                assert rowsInserted == 1 :
                        "Expected 1 row inserted for ROLE_SCOUT user, got " + rowsInserted;
            }

            // Verify the record was persisted correctly
            String verifySql = "SELECT role FROM users WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                ps.setBytes(1, uuidToBytes(userData.id()));
                try (ResultSet rs = ps.executeQuery()) {
                    assert rs.next() : "Inserted user with ROLE_SCOUT should be retrievable";
                    String storedRole = rs.getString("role");
                    assert UserRole.ROLE_SCOUT.name().equals(storedRole) :
                            "Stored role should be ROLE_SCOUT. Got: " + storedRole;
                }
            }

        } catch (SQLException e) {
            throw new AssertionError(
                    "Failed to persist User with role=ROLE_SCOUT into users table. " +
                            "Bug condition confirmed: " + e.getMessage(), e);
        }
    }

    /**
     * Property: Inserting a camp evaluator with role=ROLE_SCOUT into the camp_evaluators table
     * should succeed.
     *
     * <p>On UNFIXED code (without V3 migration), this test will FAIL because the MySQL ENUM
     * column for camp_evaluators.role does not include ROLE_SCOUT.</p>
     *
     * <p>After the V3 migration is applied, this test will PASS.</p>
     *
     * <p><b>Validates: Requirements 1.3</b></p>
     */
    @Property(tries = 5)
    void roleScoutCanBePersistedInCampEvaluatorsTable(
            @ForAll("scoutEvaluatorData") EvaluatorInsertData evaluatorData) {

        try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {

            // First, ensure the referenced user and camp exist
            insertUserIfNotExists(conn, evaluatorData.userId(), evaluatorData.userEmail(),
                    evaluatorData.userPhone());
            insertCampIfNotExists(conn, evaluatorData.campId());

            String sql = "INSERT INTO camp_evaluators (id, camp_id, user_id, role, is_lead, created_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            LocalDateTime now = LocalDateTime.now();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBytes(1, uuidToBytes(evaluatorData.evaluatorId()));
                ps.setBytes(2, uuidToBytes(evaluatorData.campId()));
                ps.setBytes(3, uuidToBytes(evaluatorData.userId()));
                ps.setString(4, UserRole.ROLE_SCOUT.name());
                ps.setBoolean(5, false);
                ps.setTimestamp(6, Timestamp.valueOf(now));

                int rowsInserted = ps.executeUpdate();
                assert rowsInserted == 1 :
                        "Expected 1 row inserted for ROLE_SCOUT evaluator, got " + rowsInserted;
            }

            // Verify the record was persisted correctly
            String verifySql = "SELECT role FROM camp_evaluators WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                ps.setBytes(1, uuidToBytes(evaluatorData.evaluatorId()));
                try (ResultSet rs = ps.executeQuery()) {
                    assert rs.next() : "Inserted camp_evaluator with ROLE_SCOUT should be retrievable";
                    String storedRole = rs.getString("role");
                    assert UserRole.ROLE_SCOUT.name().equals(storedRole) :
                            "Stored role should be ROLE_SCOUT. Got: " + storedRole;
                }
            }

        } catch (SQLException e) {
            throw new AssertionError(
                    "Failed to persist CampEvaluator with role=ROLE_SCOUT into camp_evaluators table. " +
                            "Bug condition confirmed: " + e.getMessage(), e);
        }
    }

    /**
     * Provides random user data for ROLE_SCOUT insertion tests.
     */
    @Provide
    Arbitrary<UserInsertData> scoutUserData() {
        return Arbitraries.integers().between(1, 99999).map(i ->
                new UserInsertData(
                        UUID.randomUUID(),
                        "scout-test-" + i + "@eaids.com",
                        "+234800" + String.format("%07d", i)
                )
        );
    }

    /**
     * Provides random evaluator data for ROLE_SCOUT camp_evaluators insertion tests.
     */
    @Provide
    Arbitrary<EvaluatorInsertData> scoutEvaluatorData() {
        return Arbitraries.integers().between(1, 99999).map(i ->
                new EvaluatorInsertData(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "eval-user-" + i + "@eaids.com",
                        "+234900" + String.format("%07d", i)
                )
        );
    }

    // --- Helper Methods ---

    private void insertUserIfNotExists(Connection conn, UUID userId, String email, String phone)
            throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setBytes(1, uuidToBytes(userId));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) return;
            }
        }

        String sql = "INSERT INTO users (id, email, phone_number, password, role, status, " +
                "email_verified, phone_verified, enabled, created_date, last_modified_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, uuidToBytes(userId));
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, "encoded-password");
            ps.setString(5, "ROLE_COACH"); // Use existing valid role for the FK user
            ps.setString(6, "ACTIVE");
            ps.setBoolean(7, true);
            ps.setBoolean(8, false);
            ps.setBoolean(9, true);
            ps.setTimestamp(10, Timestamp.valueOf(now));
            ps.setTimestamp(11, Timestamp.valueOf(now));
            ps.executeUpdate();
        }
    }

    private void insertCampIfNotExists(Connection conn, UUID campId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM camps WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setBytes(1, uuidToBytes(campId));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) return;
            }
        }

        String sql = "INSERT INTO camps (id, name, camp_type, status, age_category, location, country, " +
                "start_date, end_date, capacity, current_enrollment, aggregation_strategy, " +
                "created_date, last_modified_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, uuidToBytes(campId));
            ps.setString(2, "Test Camp for Evaluator");
            ps.setString(3, "IDENTIFICATION");
            ps.setString(4, "DRAFT");
            ps.setString(5, "OPEN");
            ps.setString(6, "Test Location");
            ps.setString(7, "Nigeria");
            ps.setDate(8, Date.valueOf("2026-01-01"));
            ps.setDate(9, Date.valueOf("2026-01-07"));
            ps.setInt(10, 50);
            ps.setInt(11, 0);
            ps.setString(12, "AVERAGE");
            ps.setTimestamp(13, Timestamp.valueOf(now));
            ps.setTimestamp(14, Timestamp.valueOf(now));
            ps.executeUpdate();
        }
    }

    private static void waitForMysql() throws Exception {
        int maxAttempts = 60;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("SELECT 1");
                    }
                    return;
                }
            } catch (SQLException e) {
                if (i == maxAttempts - 1) {
                    throw new RuntimeException("MySQL container did not become ready in time", e);
                }
                Thread.sleep(1000);
            }
        }
    }

    private static String exec(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().reduce("", (a, b) -> a + "\n" + b).trim();
        }
        process.waitFor(30, TimeUnit.SECONDS);
        return output;
    }

    private static byte[] uuidToBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >>> (8 * (7 - i)));
        }
        for (int i = 0; i < 8; i++) {
            bytes[8 + i] = (byte) (lsb >>> (8 * (7 - i)));
        }
        return bytes;
    }

    // --- Data Records ---

    record UserInsertData(UUID id, String email, String phoneNumber) {}

    record EvaluatorInsertData(UUID evaluatorId, UUID campId, UUID userId,
                               String userEmail, String userPhone) {}
}
