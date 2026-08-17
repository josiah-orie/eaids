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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Property-based test (jqwik) for Preservation: Original AuditAction Values Insert and Query Correctly.
 *
 * <p>This test generates AuditAction values from the ORIGINAL set defined in the MySQL schema.sql ENUM
 * and verifies that they can be inserted, retrieved by ID, and queried by action value. These tests
 * should PASS on the unfixed code, confirming baseline behavior that must be preserved after the fix.</p>
 *
 * <p><b>Validates: Requirements 3.1, 3.2, 3.3</b></p>
 *
 * <p>Property 2: Preservation - Original AuditAction Values Insert and Query Correctly</p>
 */
class AuditLogPreservationPropertyTest {

    /**
     * The set of original AuditAction values that ARE defined in the MySQL ENUM column in schema.sql.
     * These are the values that work on unfixed code and must continue to work after the fix.
     */
    private static final Set<AuditAction> ORIGINAL_ENUM_VALUES = Set.of(
            AuditAction.REGISTRATION,
            AuditAction.LOGIN_SUCCESS,
            AuditAction.LOGIN_FAILURE,
            AuditAction.LOGOUT,
            AuditAction.PASSWORD_CHANGE,
            AuditAction.PROFILE_UPDATE,
            AuditAction.EMAIL_VERIFICATION,
            AuditAction.COACH_APPROVAL,
            AuditAction.ATHLETE_APPROVAL
    );

    private static final String CONTAINER_NAME = "eaids-preservation-test-mysql";
    private static final int HOST_PORT = 13307;
    private static final String DB_NAME = "eaids_test";
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
     * Property: For any original AuditAction value from the set {REGISTRATION, LOGIN_SUCCESS,
     * LOGIN_FAILURE, LOGOUT, PASSWORD_CHANGE, PROFILE_UPDATE, EMAIL_VERIFICATION, COACH_APPROVAL,
     * ATHLETE_APPROVAL}, persisting an AuditLog entity should succeed and the record should be
     * retrievable by ID with the action matching the inserted value.
     *
     * <p>On UNFIXED code, this test should PASS (confirms baseline behavior to preserve).</p>
     *
     * <p><b>Validates: Requirements 3.1, 3.2</b></p>
     */
    @Property(tries = 20)
    void originalActionValuesCanBePersistedAndRetrieved(@ForAll("originalActions") AuditAction action) {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {

            // Insert the audit log record
            String insertSql = "INSERT INTO audit_logs (id, action, actor_identifier, ip_address, user_agent, details, created_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setBytes(1, uuidToBytes(id));
                ps.setString(2, action.name());
                ps.setString(3, "preservation-test-actor@eaids.com");
                ps.setString(4, "127.0.0.1");
                ps.setString(5, "jqwik-preservation-test");
                ps.setString(6, "Preservation test for action: " + action.name());
                ps.setTimestamp(7, Timestamp.valueOf(now));

                // Assert (1): no exception thrown - insert succeeds
                int rowsInserted = ps.executeUpdate();
                assert rowsInserted == 1 : "Expected 1 row inserted for action " + action.name() + ", got " + rowsInserted;
            }

            // Assert (2): record can be retrieved by ID
            String selectSql = "SELECT action, actor_identifier, ip_address, user_agent, details FROM audit_logs WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setBytes(1, uuidToBytes(id));
                try (ResultSet rs = ps.executeQuery()) {
                    assert rs.next() : "Inserted audit log record should be retrievable for action: " + action.name();

                    // Assert (3): retrieved action matches inserted action
                    String storedAction = rs.getString("action");
                    assert action.name().equals(storedAction) :
                            "Stored action should match inserted action. Expected: " + action.name() + ", Got: " + storedAction;

                    // Verify other fields are intact
                    assert "preservation-test-actor@eaids.com".equals(rs.getString("actor_identifier")) :
                            "Actor identifier should be preserved";
                    assert "127.0.0.1".equals(rs.getString("ip_address")) :
                            "IP address should be preserved";
                }
            }

        } catch (SQLException e) {
            throw new AssertionError(
                    "Failed to persist/retrieve AuditLog with original action=" + action.name() +
                            ". This should NEVER fail on unfixed code: " + e.getMessage(), e);
        }
    }

    /**
     * Property: Inserting multiple audit log records with different original action values and
     * querying by a specific action returns only the records with that action (correct filtering).
     *
     * <p>On UNFIXED code, this test should PASS (confirms query behavior to preserve).</p>
     *
     * <p><b>Validates: Requirements 3.3</b></p>
     */
    @Property(tries = 15)
    void queryByOriginalActionReturnsCorrectResults(@ForAll("originalActions") AuditAction targetAction) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASS)) {

            // Insert records with the target action
            int targetCount = 3;
            List<UUID> targetIds = new ArrayList<>();
            for (int i = 0; i < targetCount; i++) {
                UUID id = UUID.randomUUID();
                targetIds.add(id);
                insertAuditLog(conn, id, targetAction, "query-test-target-" + i + "@eaids.com");
            }

            // Insert records with a different original action to act as noise
            AuditAction noiseAction = pickDifferentAction(targetAction);
            int noiseCount = 2;
            for (int i = 0; i < noiseCount; i++) {
                UUID id = UUID.randomUUID();
                insertAuditLog(conn, id, noiseAction, "query-test-noise-" + i + "@eaids.com");
            }

            // Query by target action - should return at least our target records
            String querySql = "SELECT id, action FROM audit_logs WHERE action = ?";
            try (PreparedStatement ps = conn.prepareStatement(querySql)) {
                ps.setString(1, targetAction.name());
                try (ResultSet rs = ps.executeQuery()) {
                    int matchCount = 0;
                    Set<UUID> foundTargetIds = new HashSet<>();
                    while (rs.next()) {
                        String foundAction = rs.getString("action");
                        // Every returned record must have the target action
                        assert targetAction.name().equals(foundAction) :
                                "Query by action=" + targetAction.name() + " returned record with action=" + foundAction;

                        UUID foundId = bytesToUuid(rs.getBytes("id"));
                        if (targetIds.contains(foundId)) {
                            foundTargetIds.add(foundId);
                        }
                        matchCount++;
                    }

                    // All our inserted target records should be found
                    assert foundTargetIds.size() == targetCount :
                            "Expected to find all " + targetCount + " inserted records with action=" + targetAction.name() +
                                    ", but found " + foundTargetIds.size();

                    // Total results should be at least our target count (may include records from other test runs)
                    assert matchCount >= targetCount :
                            "Query should return at least " + targetCount + " records for action=" + targetAction.name() +
                                    ", but returned " + matchCount;
                }
            }

        } catch (SQLException e) {
            throw new AssertionError(
                    "Failed to query AuditLog by original action=" + targetAction.name() +
                            ". This should NEVER fail on unfixed code: " + e.getMessage(), e);
        }
    }

    /**
     * Provides AuditAction values from the original set defined in schema.sql ENUM.
     * These are the values that work on the current (unfixed) code.
     */
    @Provide
    Arbitrary<AuditAction> originalActions() {
        return Arbitraries.of(ORIGINAL_ENUM_VALUES.toArray(new AuditAction[0]));
    }

    /**
     * Pick a different action from the original set (for use as noise in query tests).
     */
    private AuditAction pickDifferentAction(AuditAction avoid) {
        for (AuditAction action : ORIGINAL_ENUM_VALUES) {
            if (action != avoid) {
                return action;
            }
        }
        // Fallback (should never happen since we have 9 values)
        return avoid;
    }

    /**
     * Helper to insert an audit log record.
     */
    private void insertAuditLog(Connection conn, UUID id, AuditAction action, String actorIdentifier) throws SQLException {
        String sql = "INSERT INTO audit_logs (id, action, actor_identifier, ip_address, user_agent, details, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, uuidToBytes(id));
            ps.setString(2, action.name());
            ps.setString(3, actorIdentifier);
            ps.setString(4, "127.0.0.1");
            ps.setString(5, "jqwik-preservation-test");
            ps.setString(6, "Query preservation test for action: " + action.name());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
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
        try (InputStream is = AuditLogPreservationPropertyTest.class
                .getClassLoader().getResourceAsStream("db/schema.sql")) {
            if (is == null) {
                throw new IOException("Cannot find db/schema.sql on classpath");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String loadMigration(String path) throws IOException {
        try (InputStream is = AuditLogPreservationPropertyTest.class
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

    /**
     * Convert byte array from MySQL BINARY(16) column back to UUID.
     */
    private static UUID bytesToUuid(byte[] bytes) {
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 0; i < 8; i++) {
            lsb = (lsb << 8) | (bytes[8 + i] & 0xff);
        }
        return new UUID(msb, lsb);
    }
}
