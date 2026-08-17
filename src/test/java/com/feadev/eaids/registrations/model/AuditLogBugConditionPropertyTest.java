package com.feadev.eaids.registrations.model;

import com.feadev.eaids.registrations.model.enums.AuditAction;
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
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Property-based test (jqwik) for Bug Condition: New AuditAction Values Rejected by Native ENUM Column.
 *
 * <p>This test generates AuditAction values that are NOT in the MySQL schema.sql ENUM definition
 * and attempts to persist them via JDBC. On unfixed code, the test is EXPECTED TO FAIL with
 * DataTruncation ("Data truncated for column 'action' at row 1"), confirming the bug exists.</p>
 *
 * <p><b>Validates: Requirements 1.1, 1.2</b></p>
 *
 * <p>Property 1: Bug Condition - New AuditAction Values Rejected by Native ENUM Column</p>
 */
class AuditLogBugConditionPropertyTest {

    /**
     * The set of AuditAction values that ARE defined in the MySQL ENUM column in schema.sql.
     * Any AuditAction NOT in this set triggers the bug condition.
     */
    private static final Set<AuditAction> SCHEMA_ENUM_VALUES = Set.of(
            AuditAction.REGISTRATION,
            AuditAction.LOGIN_SUCCESS,
            AuditAction.LOGIN_FAILURE,
            AuditAction.LOGOUT,
            AuditAction.PASSWORD_CHANGE,
            AuditAction.PROFILE_UPDATE,
            AuditAction.EMAIL_VERIFICATION,
            AuditAction.COACH_APPROVAL,
            AuditAction.ATHLETE_APPROVAL,
            AuditAction.CAMP_CREATED,
            AuditAction.CAMP_UPDATED,
            AuditAction.CAMP_STATUS_CHANGED,
            AuditAction.CAMP_DELETED,
            AuditAction.ATHLETE_ENROLLED,
            AuditAction.ENROLLMENT_STATUS_CHANGED,
            AuditAction.ATTENDANCE_RECORDED,
            AuditAction.DISCIPLINE_RECORDED,
            AuditAction.MEASUREMENT_RECORDED,
            AuditAction.TECHNICAL_SCORE_RECORDED,
            AuditAction.TACTICAL_SCORE_RECORDED,
            AuditAction.EVALUATION_RECORDED,
            AuditAction.SCORES_CALCULATED,
            AuditAction.RANKINGS_GENERATED,
            AuditAction.SELECTION_GENERATED,
            AuditAction.SELECTION_APPROVED,
            AuditAction.SELECTION_REJECTED,
            AuditAction.TRAINING_LOAD_RECORDED,
            AuditAction.POSITION_RECOMMENDED,
            AuditAction.POTENTIAL_PREDICTED,
            AuditAction.INJURY_RISK_ASSESSED,
            AuditAction.NATIONAL_RANKING_GENERATED,
            AuditAction.BENCHMARK_CREATED,
            AuditAction.BENCHMARK_UPDATED,
            AuditAction.REPORT_DOWNLOADED,
            AuditAction.INTERVIEW_REQUESTED,
            AuditAction.INTERVIEW_APPROVED,
            AuditAction.INTERVIEW_REJECTED,
            AuditAction.SCOUT_SCOPE_GRANTED,
            AuditAction.SCOUT_SCOPE_REVOKED
    );

    private static final String CONTAINER_NAME = "eaids-bug-condition-test-mysql";
    private static final int HOST_PORT = 13306;
    private static final String DB_NAME = "eaids_test";
    private static final String DB_USER = "test";
    private static final String DB_PASS = "test";

    private static String jdbcUrl;

    @BeforeContainer
    static void startContainer() throws Exception {
        // Stop and remove any existing container from a previous run
        exec("docker", "rm", "-f", CONTAINER_NAME);

        // Start a MySQL container directly via Docker CLI (bypasses Testcontainers docker-java API issue)
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

        // Initialize schema from schema.sql
        try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {
            String schema = loadSchema();
            for (String statement : splitStatements(schema)) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
            }

            // Apply V2 migration: ALTER audit_logs action column from ENUM to VARCHAR(50)
            String v2Migration = loadMigration("db/migration/V2__alter_audit_logs_action_to_varchar.sql");
            for (String statement : splitStatements(v2Migration)) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
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
     * Property: For any AuditAction value NOT in the schema.sql ENUM definition,
     * persisting an AuditLog entity should succeed without exception.
     *
     * <p>On UNFIXED code, this test will FAIL because the MySQL ENUM column rejects
     * values not in its definition (specifically SCOUT_REGISTERED).</p>
     *
     * <p><b>Validates: Requirements 1.1, 1.2</b></p>
     */
    @Property(tries = 10)
    void allBugConditionActionsCanBePersisted(@ForAll("bugConditionActions") AuditAction action) {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {

            String sql = "INSERT INTO audit_logs (id, action, actor_identifier, ip_address, user_agent, details, created_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBytes(1, uuidToBytes(id));
                ps.setString(2, action.name());
                ps.setString(3, "test-actor@eaids.com");
                ps.setString(4, "127.0.0.1");
                ps.setString(5, "jqwik-test-agent");
                ps.setString(6, "Bug condition exploration test for action: " + action.name());
                ps.setTimestamp(7, Timestamp.valueOf(now));

                int rowsInserted = ps.executeUpdate();
                assert rowsInserted == 1 : "Expected 1 row inserted for action " + action.name() + ", got " + rowsInserted;
            }

            // Verify the record was persisted correctly
            String verifySql = "SELECT action FROM audit_logs WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(verifySql)) {
                ps.setBytes(1, uuidToBytes(id));
                try (ResultSet rs = ps.executeQuery()) {
                    assert rs.next() : "Inserted audit log record should be retrievable for action: " + action.name();
                    String storedAction = rs.getString("action");
                    assert action.name().equals(storedAction) :
                            "Stored action should match inserted action. Expected: " + action.name() + ", Got: " + storedAction;
                }
            }

        } catch (SQLException e) {
            throw new AssertionError(
                    "Failed to persist AuditLog with action=" + action.name() +
                            ". Bug condition confirmed: " + e.getMessage(), e);
        }
    }

    /**
     * Provides AuditAction values that are NOT in the schema.sql ENUM definition.
     * These are the values that trigger the bug (Data truncation on insert).
     */
    @Provide
    Arbitrary<AuditAction> bugConditionActions() {
        AuditAction[] bugActions = Arrays.stream(AuditAction.values())
                .filter(action -> !SCHEMA_ENUM_VALUES.contains(action))
                .toArray(AuditAction[]::new);

        if (bugActions.length == 0) {
            throw new IllegalStateException(
                    "No bug condition actions found - all AuditAction values are in schema.sql ENUM");
        }

        return Arbitraries.of(bugActions);
    }

    private static void waitForMysql() throws Exception {
        int maxAttempts = 60;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("SELECT 1");
                    }
                    return; // Connected successfully
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

    private static String loadSchema() throws IOException {
        try (InputStream is = AuditLogBugConditionPropertyTest.class
                .getClassLoader().getResourceAsStream("db/schema.sql")) {
            if (is == null) {
                throw new IOException("Cannot find db/schema.sql on classpath");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String loadMigration(String path) throws IOException {
        try (InputStream is = AuditLogBugConditionPropertyTest.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Cannot find " + path + " on classpath");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Split SQL statements by semicolons, handling simple cases.
     */
    private static String[] splitStatements(String sql) {
        return sql.split(";(?=([^']*'[^']*')*[^']*$)");
    }

    /**
     * Convert UUID to byte array for MySQL BINARY(16) column.
     */
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
}
