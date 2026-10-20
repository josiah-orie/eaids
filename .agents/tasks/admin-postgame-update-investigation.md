# Investigation Report: Admin Post-Game Stat Update Feature

## Summary Answer

The feature is **partially implemented**. The core computation pipeline works correctly
(auto-trigger on COMPLETED + manual recompute endpoint), and the security rules are
correct. However, two key capabilities are **missing**:

1. **No direct stat editing endpoint** — an Admin cannot manually set or correct
   individual stat values on a `PostGameSummary` row. The only update path is to
   modify raw `LeagueStatEntry` records first, then call recompute. There is no
   `PUT /api/v1/admin/fixtures/{fixtureId}/post-game/{summaryId}` (or equivalent).

2. **No update path for IN_PROGRESS fixtures** — `computeAndStore` in the service does
   not check fixture status at all, which means the recompute endpoint technically
   works for any fixture status. However, the *automatic* trigger only fires on
   `COMPLETED`, and there is no explicit "compute interim snapshot during a live
   match" workflow exposed to the Admin. The raw-stat recording endpoint
   (`POST .../league-stats`) does allow `IN_PROGRESS`, but there is no corresponding
   "refresh the post-game summary now" trigger during a live game.

3. **No admin UI** — the `templates/` directory contains only a `.DS_Store` file.
   There are zero Thymeleaf templates. The application is API-only with no
   admin-facing form for editing or viewing post-game stats.

---

## Evidence

### 1. PostGameStatsController — endpoints

**File:** `src/main/java/com/feadev/eaids/league/controller/PostGameStatsController.java`

| # | Method | Path | Role Guard | Purpose |
|---|--------|------|------------|---------|
| 1 | GET    | `/api/v1/fixtures/{fixtureId}/post-game` | authenticated | Read full fixture breakdown |
| 2 | GET    | `/api/v1/fixtures/{fixtureId}/post-game/{teamPlayerId}` | authenticated | Read single player summary |
| 3 | POST   | `/api/v1/admin/fixtures/{fixtureId}/post-game/recompute` | `@PreAuthorize("hasRole('ADMIN')")` | Trigger recomputation |
| 4 | GET    | `/api/v1/athletes/{athleteId}/post-game-history` | authenticated | Paginated athlete history |

There is **no** `PUT`, `PATCH`, or `POST` endpoint for directly editing a
`PostGameSummary` row. Endpoint 3 is the only write path, and it re-derives all
values from raw `LeagueStatEntry` rows — the Admin cannot override an individual
computed stat.

### 2. PostGameStatsServiceImpl — computeAndStore

**File:** `src/main/java/com/feadev/eaids/league/service/impl/PostGameStatsServiceImpl.java`
Lines 57–127

```
@Async
@Transactional
public void computeAndStore(UUID fixtureId) { ... }
```

Key observations:
- `computeAndStore` does **not validate fixture status**. It will run for a fixture
  in any state: `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, etc.
- The upsert pattern (lines 102–119) means re-running is safe and idempotent.
- The method is annotated `@Async`, so calling the recompute endpoint during
  `IN_PROGRESS` *would technically work* if stat entries exist — but this workflow
  is undocumented and there is no explicit "live snapshot" business concept.

**Conclusion:** The service does not block updates after completion, but it equally
does not explicitly support IN_PROGRESS as a first-class use case.

### 3. Auto-trigger is COMPLETED-only

**File:** `src/main/java/com/feadev/eaids/league/service/impl/MatchFixtureServiceImpl.java`
Lines 145–157 (`transitionStatus` method):

```java
if (newStatus == FixtureStatus.COMPLETED) {
    postGameStatsService.computeAndStore(fixtureId);
}
```

No equivalent trigger fires when a fixture transitions to `IN_PROGRESS`. The
Admin must manually call the recompute endpoint for any mid-game snapshot.

### 4. FixtureStatus enum

**File:** `src/main/java/com/feadev/eaids/league/model/enums/FixtureStatus.java`

```java
public enum FixtureStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED, POSTPONED, CANCELLED
}
```

Both `IN_PROGRESS` and `COMPLETED` statuses exist in the domain model.

### 5. Raw stat recording allows IN_PROGRESS and COMPLETED

**File:** `src/main/java/com/feadev/eaids/league/service/impl/LeaguePreGameStatsServiceImpl.java`
Lines 53–58 (`recordLeagueStats` method):

```java
if (fixture.getStatus() != FixtureStatus.IN_PROGRESS
        && fixture.getStatus() != FixtureStatus.COMPLETED) {
    throw new InvalidStateTransitionException(
        "Stats can only be recorded for IN_PROGRESS or COMPLETED fixtures.");
}
```

The underlying `LeagueStatEntry` recording endpoint already supports both
`IN_PROGRESS` and `COMPLETED`. Since `computeAndStore` reads these entries without
a status check, calling the admin recompute endpoint after recording stats on an
`IN_PROGRESS` fixture would produce a snapshot. This path is functional but
**implicit and undocumented**.

### 6. Security configuration

**File:** `src/main/java/com/feadev/eaids/config/SecurityConfig.java`
Lines 80–99

- `POST /api/v1/admin/**` is covered by the blanket rule:
  `.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")` (line 82)
- The recompute endpoint is additionally guarded at the method level with
  `@PreAuthorize("hasRole('ADMIN')")` (PostGameStatsController, line 48).
- Read endpoints require `authenticated()` only (lines 98–99) — any logged-in
  user can view post-game data. ✓

Security is **correctly configured** — recompute is ADMIN-only, reads are
open to all authenticated roles.

### 7. Admin UI / Thymeleaf templates

```
src/main/resources/templates/   ← contains only .DS_Store
```

No templates exist at all. The application has no server-rendered UI. There is
no admin form for viewing or editing post-game stats.

### 8. No direct edit DTO or endpoint

A search across all Java source for `UpdatePostGame`, `EditPostGame`,
`ManualPostGame`, `PostGameUpdateRequest` returned **zero results**. No DTO,
no controller method, and no service method exist for direct field-level editing
of a `PostGameSummary`.

---

## Gap Summary

| Requirement | Implemented? | Detail |
|-------------|:---:|--------|
| Admin can trigger recomputation after completion | ✅ | `POST /api/v1/admin/fixtures/{id}/post-game/recompute`, ADMIN role |
| Auto-compute on fixture completion | ✅ | Fires in `transitionStatus` when status → COMPLETED |
| Service guard: no re-compute after COMPLETED | ✅ (inverted) | No guard — updates allowed at any status |
| Service supports IN_PROGRESS compute | ⚠️ | Works technically (no status check), but not an explicit workflow |
| Admin can directly edit a PostGameSummary field | ❌ | No PUT/PATCH endpoint, no DTO, no service method |
| Admin-facing UI form for post-game stats | ❌ | No Thymeleaf templates exist at all |
| Separate "live snapshot" endpoint for IN_PROGRESS | ❌ | Only `recompute` endpoint, shared for all statuses |

---

## Conclusions & Recommendations

### What exists

The automated pipeline is solid: stat entries → `COMPLETED` transition → async
`computeAndStore` → upserted `PostGameSummary` rows. The recompute endpoint gives
the Admin a correction mechanism without the need to re-trigger the fixture
transition. Security is correctly layered (URL-level + method-level `@PreAuthorize`).

### What is missing

**Gap 1 — No direct edit of PostGameSummary fields.**
The current model forces corrections through raw `LeagueStatEntry` data followed by
a recompute. If an Admin wants to override a computed stat without touching the
underlying entries (e.g., a data-entry error not recoverable by re-aggregation),
there is no path. Add:

- A `UpdatePostGameSummaryRequest` DTO with nullable stat fields.
- A `PATCH /api/v1/admin/fixtures/{fixtureId}/post-game/{summaryId}` endpoint
  in `PostGameStatsController`, guarded with `@PreAuthorize("hasRole('ADMIN')")`.
- A corresponding service method `updateSummary(UUID summaryId, UpdatePostGameSummaryRequest)`.

**Gap 2 — IN_PROGRESS snapshot is implicit, not a named workflow.**
Record live stats via `POST .../league-stats`, then call the recompute endpoint.
This works today but is invisible to consumers of the API. Options:
- Document this pattern explicitly in the user guide.
- Or add a distinct `POST /api/v1/admin/fixtures/{fixtureId}/post-game/snapshot`
  endpoint scoped to `IN_PROGRESS` fixtures that makes the intent explicit.

**Gap 3 — No admin UI.**
The project is currently API-only. If a browser-based admin tool is needed, a
Thymeleaf template (or a separate frontend page) for viewing and triggering
recomputation would need to be added. This is a product decision, not a bug.

**Gap 4 — No status guard on `computeAndStore`.**
While permissive behaviour is currently acceptable (idempotent upsert), adding an
explicit check (`FixtureStatus.IN_PROGRESS` or `FixtureStatus.COMPLETED` only)
would prevent accidental computation runs on `SCHEDULED` fixtures that have no
stat entries yet, and makes intent clearer in the code.
