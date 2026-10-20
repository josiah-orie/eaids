# Post-Game Stats Feature Investigation

**Date:** 2025-01-31  
**Workspace:** `/Users/mac/Documents/intelliJProjects/vicado360/Clients Projects/feadev/eaids`

---

## Summary Answer

**The post-game stats feature is fully implemented.** Every layer described in `docs/post-game-stats-user-guide.md` — database table, JPA entity, repository, service, controller, security rules, async trigger wiring, and seed data — is present in the codebase and matches the documented specification exactly. There are no missing pieces in the server-side implementation.

The only minor gap is the absence of automated unit/integration tests for the post-game stats module.

---

## Evidence

### 1. Database Migration

**File:** `src/main/resources/db/migration/V13__create_commentary_and_postgame_tables.sql`

The `post_game_summaries` table is created with:
- All nine typed stat columns (`total_passing_yards`, `total_rushing_yards`, `total_receiving_yards`, `total_tackles`, `touchdowns`, `interceptions`, `sacks`, `fumbles`, `penalties`) as `DECIMAL(10, 2)`
- A `custom_stats JSON` column for non-standard overflow stats
- A unique constraint `uq_pgs_fixture_player` on `(fixture_id, team_player_id)` — enforcing one row per player per fixture
- Foreign keys to `match_fixtures`, `team_players`, and `users`
- Performance indexes on `fixture_id`, `athlete_user_id`, and `team_id`
- `computed_at DATETIME(6)` and `created_date DATETIME(6)` audit columns

This exactly matches the spec in the user guide.

---

### 2. JPA Entity

**File:** `src/main/java/com/feadev/eaids/league/model/PostGameSummary.java`

- `@Entity`, `@Table(name = "post_game_summaries")` with the unique constraint declared
- All nine typed `BigDecimal` stat fields with matching column names
- `customStats` stored as a `JSON` `String` column, serialised/deserialised in the service layer
- `athleteUserId` and `teamId` denormalised for fast per-athlete and per-team queries
- `@ManyToOne` relationships to `MatchFixture` and `TeamPlayer`
- Lombok `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@CreatedDate` auditing via `AuditingEntityListener`

---

### 3. Repository

**File:** `src/main/java/com/feadev/eaids/league/repository/PostGameSummaryRepository.java`

Extends `JpaRepository<PostGameSummary, UUID>` with all four required query methods:

| Method | Purpose |
|--------|---------|
| `findByFixtureId(UUID)` | Load all summaries for a fixture (grouped into home/away) |
| `findByAthleteUserId(UUID, Pageable)` | Paginated career history per athlete |
| `findByFixtureIdAndTeamPlayerId(UUID, UUID)` | Single-player lookup for upsert and the individual endpoint |
| `findByTeamIdAndFixtureId(UUID, UUID)` | Per-team per-fixture lookup |

---

### 4. Service Interface & Implementation

**Files:**
- `src/main/java/com/feadev/eaids/league/service/PostGameStatsService.java`
- `src/main/java/com/feadev/eaids/league/service/impl/PostGameStatsServiceImpl.java`

Interface declares the four methods required by the spec:
- `void computeAndStore(UUID fixtureId)` — async aggregation + upsert
- `FixturePostGameResponse getSummaryByFixture(UUID fixtureId)`
- `Page<PostGameSummaryResponse> getSummaryForAthlete(UUID athleteUserId, Pageable pageable)`
- `PostGameSummaryResponse getSummaryForPlayer(UUID fixtureId, UUID teamPlayerId)`

Implementation details confirmed:
- `@Async` annotation on `computeAndStore` — runs asynchronously, does not block the fixture-complete response
- Groups `LeagueStatEntry` rows by `teamPlayerId`, case-insensitively matches the nine canonical stat names
- Overflow stats go into a `customMap` serialised as JSON via Jackson `ObjectMapper`
- **Upsert pattern**: loads existing `PostGameSummary` (if any) before saving — idempotent, safe to re-run
- `nullIfZero()` helper ensures zero-value stats are stored as `null` (matches the spec: "null means no entries of that type were recorded")

---

### 5. Controller

**File:** `src/main/java/com/feadev/eaids/league/controller/PostGameStatsController.java`

All four endpoints are registered and match the documented paths:

| Endpoint | Method | Path | Auth |
|----------|--------|------|------|
| Fixture post-game breakdown | `GET` | `/api/v1/fixtures/{fixtureId}/post-game` | Authenticated |
| Single player summary | `GET` | `/api/v1/fixtures/{fixtureId}/post-game/{teamPlayerId}` | Authenticated |
| Admin manual recompute | `POST` | `/api/v1/admin/fixtures/{fixtureId}/post-game/recompute` | `@PreAuthorize("hasRole('ADMIN')")` |
| Athlete career history | `GET` | `/api/v1/athletes/{athleteId}/post-game-history` | Authenticated |

The recompute endpoint returns `202 Accepted` (async, as specified) and is guarded by `@PreAuthorize("hasRole('ADMIN')")`.

---

### 6. Security Configuration

**File:** `src/main/java/com/feadev/eaids/config/SecurityConfig.java` (lines 97–100)

```java
// Post-game stats — read requires authentication; recompute is under /admin/** already
.requestMatchers(HttpMethod.GET, "/api/v1/fixtures/*/post-game").authenticated()
.requestMatchers(HttpMethod.GET, "/api/v1/fixtures/*/post-game/**").authenticated()
```

The `GET /api/v1/athletes/{athleteId}/post-game-history` endpoint is covered by the catch-all `.anyRequest().authenticated()` at the bottom of the security chain, which is the correct and sufficient rule since the feature guide specifies "Authenticated" access only.

The admin recompute `POST /api/v1/admin/fixtures/{fixtureId}/post-game/recompute` falls under the `/api/v1/admin/**` rule that requires `ROLE_ADMIN`, in addition to the `@PreAuthorize` annotation on the controller method.

---

### 7. Async Trigger from Fixture Completion

**File:** `src/main/java/com/feadev/eaids/league/service/impl/MatchFixtureServiceImpl.java` (lines 202–204)

```java
// Trigger async post-game snapshot computation when the fixture completes
if (newStatus == FixtureStatus.COMPLETED) {
    postGameStatsService.computeAndStore(fixtureId);
}
```

This confirms the automatic computation flow described in the user guide: completing a fixture fires the async post-game job without blocking the HTTP response.

**File:** `src/main/java/com/feadev/eaids/config/AsyncConfig.java`

`@EnableAsync` configuration is present and its JavaDoc explicitly names `PostGameStatsService.computeAndStore()` as the primary consumer.

---

### 8. DTOs

**Files:**
- `src/main/java/com/feadev/eaids/league/dto/response/PostGameSummaryResponse.java` — all 16 fields present (id, fixtureId, teamPlayerId, athleteUserId, teamId, playerName, jerseyNumber, position, nine stat fields, customStats map, computedAt, createdDate)
- `src/main/java/com/feadev/eaids/league/dto/response/FixturePostGameResponse.java` — fixtureId, homeTeamId/Name, awayTeamId/Name, homeScore, awayScore, homeTeamStats list, awayTeamStats list

Both match the JSON shapes shown in the user guide exactly.

---

### 9. Seed Data

**File:** `src/main/java/com/feadev/eaids/config/DataSeeder.java` (Phase 19)

The `seedPostGameSummaries()` method (line 3092) populates the `post_game_summaries` table on first startup by iterating over all completed fixtures and computing summaries directly (mirroring `PostGameStatsServiceImpl` logic). This ensures the API returns real data in a freshly started development environment without needing to replay the fixture-completion flow.

---

### 10. Templates

No Thymeleaf templates related to post-game stats were found in `src/main/resources/templates/`. This is expected — the feature is entirely REST/JSON API based (no server-side rendered UI). The project uses templates only for email and similar non-feature UIs. This is **not** a gap.

---

## Documentation Coverage

**`docs/match-fixture-user-guide.md`** — contains a "Post-Game Statistics" quick-reference table (the four endpoints) and a link to the dedicated `post-game-stats-user-guide.md`. No conflicts with the implementation.

**`docs/post-game-stats-user-guide.md`** — comprehensive guide covering all endpoints, automatic computation flow, stat column reference, authentication matrix, and error reference. Matches the implementation in all details.

---

## Conclusions

| Component | Status |
|-----------|--------|
| DB migration (`V13`) — `post_game_summaries` table | ✅ Implemented |
| JPA entity — `PostGameSummary` | ✅ Implemented |
| Repository — `PostGameSummaryRepository` | ✅ Implemented |
| Service interface — `PostGameStatsService` | ✅ Implemented |
| Service implementation — `PostGameStatsServiceImpl` | ✅ Implemented |
| Controller — `PostGameStatsController` (4 endpoints) | ✅ Implemented |
| Security rules in `SecurityConfig` | ✅ Implemented |
| Async trigger from fixture completion | ✅ Implemented |
| Async infrastructure — `AsyncConfig` | ✅ Implemented |
| Response DTOs — `PostGameSummaryResponse`, `FixturePostGameResponse` | ✅ Implemented |
| Seed data in `DataSeeder` (Phase 19) | ✅ Implemented |
| Thymeleaf templates | N/A (REST-only feature) |
| Unit / integration tests | ⚠️ Missing |

---

## Recommendations

1. **Add tests.** No test classes for `PostGameStatsServiceImpl` or `PostGameStatsController` were found under `src/test/`. Given the async computation logic, custom stat serialisation, and upsert behavior, the following tests would be valuable:
   - Unit tests for `PostGameStatsServiceImpl.computeAndStore()`: verify stat aggregation, custom-stat overflow, and the upsert (idempotency) path.
   - Unit tests for `mapToResponse()`: verify null-handling for players without certain stat types.
   - Integration/slice test for `PostGameStatsController`: verify that 404 is returned for a fixture with no summaries, and 202 is returned on recompute.
   - Verify that `computeAndStore` is actually called asynchronously on fixture completion (a Spring integration test with `@Async` enabled).

2. **Consider security rule explicitness for `/athletes/*/post-game-history`.** Currently this falls under the catch-all `anyRequest().authenticated()`. Adding an explicit matcher (as was done for the `/fixtures/*/post-game` paths) would make the intent clear in audits and avoid surprises if the catch-all rule changes.

3. No functional gaps were identified. The feature is production-ready modulo tests.
