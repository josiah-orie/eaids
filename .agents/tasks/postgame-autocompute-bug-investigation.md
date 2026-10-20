# Post-Game Stats Auto-Compute Bug Investigation

**Date:** Investigation of "no stats auto-computed on fixture COMPLETED transition"  
**Workspace:** `/Users/mac/Documents/intelliJProjects/vicado360/Clients Projects/feadev/eaids`

---

## Summary Answer

**There are two distinct root causes, and both must be fixed for auto-computation to work.**

1. **PRIMARY — Transaction boundary race (data invisible to async thread):** `MatchFixtureServiceImpl.transitionStatus()` is annotated `@Transactional` at the class level. `postGameStatsService.computeAndStore(fixtureId)` is called _inside_ that transaction, _before it commits_. The `@Async` proxy dispatches `computeAndStore` to a thread-pool thread immediately, but the async thread starts reading from the DB before the calling transaction has committed. `fixtureRepository.findById(fixtureId)` either sees the fixture in its old `IN_PROGRESS` state or (if the async thread races ahead of the commit) throws a stale read. More critically, `statEntryRepository.findByFixtureId(fixtureId)` may return the correct entries, but the fixture entity is **not yet visible as COMPLETED** from the async thread's perspective, depending on MySQL isolation level and timing. This is the canonical Spring `@Async` + `@Transactional` pitfall: the async method starts before the outer transaction flushes.

2. **SECONDARY — Silent early-exit when no `LeagueStatEntry` rows exist:** `PostGameStatsServiceImpl.computeAndStore()` calls `statEntryRepository.findByFixtureId(fixtureId)` and, if the result is empty, logs a single info message and returns silently. If no stats were recorded during the game via the league stat entry API, the method exits with zero writes and no error — the caller and the admin have no indication that the computation was a no-op because of missing input data. This is not a bug per se, but it is the most likely explanation for why the user "saw no stats" after completing the game.

Both issues compound: even if stat entries exist, the transaction race (issue 1) means the async thread may read an empty `entries` list because the parent transaction hasn't committed yet, triggering the silent early-exit (issue 2) on data that actually does exist.

---

## Evidence

### Root Cause 1 — Transaction Boundary Race

**File:** `src/main/java/com/feadev/eaids/league/service/impl/MatchFixtureServiceImpl.java`

```
Line 37-38:   @Transactional           ← class-level default transaction on ALL methods
              public class MatchFixtureServiceImpl ...

Line 176-205: public MatchFixtureResponse transitionStatus(...) {
              ...
              fixture.setStatus(newStatus);
Line 197:     MatchFixture saved = fixtureRepository.save(fixture);   // dirty, NOT yet committed
Line 202-204: if (newStatus == FixtureStatus.COMPLETED) {
                  postGameStatsService.computeAndStore(fixtureId);    // dispatches async thread HERE
              }                                                         // outer txn still open
              return mapToResponse(saved);                             // outer txn commits AFTER return
```

`transitionStatus` has no overriding `@Transactional` annotation of its own, so it inherits the class-level `@Transactional`. The call to `computeAndStore(fixtureId)` dispatches the async task to a thread-pool thread, but the outer transaction does not commit until `mapToResponse(saved)` is returned and the method exits its Spring AOP proxy. The async thread starts executing concurrently, calls `fixtureRepository.findById(fixtureId)` against MySQL in a **new transaction** (default `REQUIRED` propagation on a new thread), and reads the state that is currently committed. At this moment, the fixture row may still show `IN_PROGRESS` in MySQL (the committing thread hasn't released its write lock yet). More importantly, `LeagueStatEntry` rows added during the game via the same request or a previous HTTP call may be visible, but the fixture join used to return them could reflect a stale state.

**File:** `src/main/java/com/feadev/eaids/league/service/impl/PostGameStatsServiceImpl.java`

```
Line 57-67:   @Override
              @Async
              @Transactional                                          // opens a NEW transaction
              public void computeAndStore(UUID fixtureId) {
                  ...
                  List<LeagueStatEntry> entries = statEntryRepository.findByFixtureId(fixtureId);
                  if (entries.isEmpty()) {
                      log.info("No stat entries found for fixture {} — skipping ...", fixtureId);
                      return;                                         // SILENT EXIT — no error thrown
                  }
```

`@Async` causes Spring to wrap the method call via the async executor proxy. `@Transactional` on an `@Async` method opens a new transaction in the executor thread, which is correct. However, the async thread races the parent commit. In practice under MySQL's default `REPEATABLE READ` isolation, if the async transaction starts before the parent transaction commits, it reads a snapshot from before the COMPLETED transition — meaning `findByFixtureId` may return rows but the fixture-level state is stale.

**File:** `src/main/java/com/feadev/eaids/config/AsyncConfig.java`

```
@Configuration
@EnableAsync
public class AsyncConfig {
}                                                                     // No executor configured
```

`@EnableAsync` is present, so `@Async` methods do execute asynchronously (not synchronously). However, the default `SimpleAsyncTaskExecutor` creates a **new thread per call** and has no thread pool or queue limit. Exceptions thrown in the async method are logged by the default uncaught exception handler but produce no observable HTTP error — they are silently discarded from the caller's perspective.

No `AsyncUncaughtExceptionHandler` is configured anywhere in the codebase (grep confirms zero matches). This means if `computeAndStore` throws a `ResourceNotFoundException` or a `DataIntegrityViolationException`, the stack trace goes to the async thread's default logger and the admin gets no feedback.

**`@EnableAsync` is declared twice** — both in `AsyncConfig.java` (line 12) and in `EaidsApplication.java` (line 9). This is harmless but redundant.

---

### Root Cause 2 — Empty LeagueStatEntry Table (Most Probable Cause of "No Stats")

**File:** `src/main/java/com/feadev/eaids/league/service/impl/PostGameStatsServiceImpl.java`, lines 63–67

```java
List<LeagueStatEntry> entries = statEntryRepository.findByFixtureId(fixtureId);
if (entries.isEmpty()) {
    log.info("No stat entries found for fixture {} — skipping post-game computation", fixtureId);
    return;
}
```

`PostGameStatsServiceImpl` reads from `LeagueStatEntry` (table `league_stat_entries`, created in `V10__create_league_stat_entries_table.sql`). This table is populated by the in-game stat recording API — admins must call `POST /api/v1/league-stat-entries` (or equivalent) during the game to record per-player stat values.

The `MatchFixtureServiceImpl` class also injects `MatchStatEntryRepository` (`match_stat_entries` table — the camp-module table, an entirely different entity), which is used **only** for `getFixtureStats()`. This is not the same table. The post-game computation has no dependency on `MatchStatEntry`; it relies solely on `LeagueStatEntry`.

**If no league stat entries were recorded for the fixture during or before the game, the computation correctly early-exits with a log line and produces zero rows.** The admin has no API notification of this; the only evidence is the server log line at `INFO` level.

---

### Supporting Checks

**V13 migration vs. PostGameSummary entity — no mismatch found:**

| DB column (`V13`) | Entity field (`PostGameSummary.java`) | Type |
|---|---|---|
| `total_passing_yards` | `totalPassingYards` | `DECIMAL(10,2)` ✅ |
| `total_rushing_yards` | `totalRushingYards` | `DECIMAL(10,2)` ✅ |
| `total_receiving_yards` | `totalReceivingYards` | `DECIMAL(10,2)` ✅ |
| `total_tackles` | `totalTackles` | `DECIMAL(10,2)` ✅ |
| `touchdowns` | `touchdowns` | `DECIMAL(10,2)` ✅ |
| `interceptions` | `interceptions` | `DECIMAL(10,2)` ✅ |
| `sacks` | `sacks` | `DECIMAL(10,2)` ✅ |
| `fumbles` | `fumbles` | `DECIMAL(10,2)` ✅ |
| `penalties` | `penalties` | `DECIMAL(10,2)` ✅ |
| `custom_stats` | `customStats` | `JSON` ✅ |
| `computed_at` | `computedAt` | `DATETIME(6)` ✅ |

No schema-entity mismatch. This is not a cause of failure.

**No try/catch around `computeAndStore` call:** `MatchFixtureServiceImpl.transitionStatus()` (lines 202–205) has no try/catch wrapping the trigger call. Exceptions thrown synchronously during the proxy dispatch (not in the async thread) would propagate normally. Exceptions in the async thread are silently lost.

**Upsert logic is correct:** `PostGameSummaryRepository.findByFixtureIdAndTeamPlayerId()` is called before save. The unique constraint `uq_pgs_fixture_player` on `(fixture_id, team_player_id)` would cause a `DataIntegrityViolationException` only if two async threads ran simultaneously for the same fixture — unlikely but possible with the `SimpleAsyncTaskExecutor`. This is not the primary cause.

---

## Conclusions

| Issue | Severity | Cause |
|---|---|---|
| `computeAndStore` dispatched before parent transaction commits | **High** — async thread may read stale/pre-commit data | `@Transactional` class-level on caller + `@Async` method called inside that transaction |
| `LeagueStatEntry` table empty at compute time | **High** — silent no-op with no user feedback | No stats were recorded via the league stat entry API during the game |
| No `AsyncUncaughtExceptionHandler` | **Medium** — any async exception is silently discarded | `AsyncConfig` has no exception handler; default executor eats stack traces |
| `SimpleAsyncTaskExecutor` (no pool config) | **Low** — unbounded thread creation under load | `AsyncConfig` has no `@Bean ThreadPoolTaskExecutor` |
| Duplicate `@EnableAsync` | **Negligible** | Present in both `AsyncConfig` and `EaidsApplication` |

---

## Recommendations (do not implement — investigation only)

### Fix 1 — Move the async trigger outside the transaction

In `MatchFixtureServiceImpl.transitionStatus()`, the trigger to `computeAndStore` must fire **after** the transaction commits, not inside it. Two options:

**Option A (preferred) — Use a Spring `@TransactionalEventListener`:**  
Publish an application event (`FixtureCompletedEvent`) inside the transaction. Wire a `@TransactionalEventListener(phase = AFTER_COMMIT)` in a separate component (or `PostGameStatsServiceImpl` itself) to trigger `computeAndStore`. Spring guarantees the listener fires only after the transaction commits, so the async thread always reads committed data.

```java
// In transitionStatus(), after fixture.setStatus(newStatus):
if (newStatus == FixtureStatus.COMPLETED) {
    applicationEventPublisher.publishEvent(new FixtureCompletedEvent(fixtureId));
}

// In PostGameStatsServiceImpl or a listener class:
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Async
public void onFixtureCompleted(FixtureCompletedEvent event) {
    computeAndStore(event.fixtureId());
}
```

**Option B — Split the service method with `REQUIRES_NEW` propagation on the trigger:**  
Call a self-invocation wrapper bean that uses `Propagation.REQUIRES_NEW` so the fixture row is visible. This is more fragile and adds complexity; Option A is cleaner.

### Fix 2 — Add admin feedback when computation is skipped

Change the early-exit in `computeAndStore` from a silent `return` to a distinguishable log level or a dedicated status update:

```java
if (entries.isEmpty()) {
    log.warn("Post-game computation skipped for fixture {} — no LeagueStatEntry rows found. "
           + "Stats must be recorded via the league stat entry API before or during the game.",
             fixtureId);
    return;
}
```

Consider also exposing a `/api/v1/admin/fixtures/{fixtureId}/post-game/status` endpoint that tells the admin whether computation ran, when, and how many rows were produced. This makes the silent no-op observable without tailing logs.

### Fix 3 — Configure a proper async executor and exception handler

In `AsyncConfig.java`, replace the empty class with a configured `ThreadPoolTaskExecutor` and an `AsyncUncaughtExceptionHandler` so async failures appear in logs at ERROR level and are attributed to the invoking fixture:

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("eaids-async-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
            LoggerFactory.getLogger(AsyncConfig.class)
                .error("Async method {} threw an uncaught exception: {}", method.getName(), ex.getMessage(), ex);
    }
}
```

Remove the redundant `@EnableAsync` from `EaidsApplication.java`.

### Fix 4 — Verify league stat entries are being recorded correctly

Before investigating further code: confirm that the `league_stat_entries` table has rows for the fixture in question. If the table is empty for that fixture, the computation will always skip silently regardless of code fixes. The admin must use the league stat recording API during or after the game to populate entries. Consider documenting this dependency explicitly in the user guide.
