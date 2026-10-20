# Field-Level Update Endpoint for Post-Game Stats

This change adds a `PUT /api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}` endpoint that lets an admin correct individual stat values on an existing `PostGameSummary` row without re-aggregating from raw `LeagueStatEntry` data. The approach is a standard partial-update pattern: a new `UpdatePostGameStatsRequest` DTO carries all nine typed stat fields as optional `BigDecimal` values, the service method copies only the non-null fields onto the loaded entity, then saves. The update is gated on fixture status — only `IN_PROGRESS` and `COMPLETED` pass through. Documentation and the "Who Can Do What" permissions table in the user guide are updated to reflect the new capability.

**Watch for:** The `customStats` clear-all path (`{}`) stores an empty string `""` in the database column rather than `null`, while `computeAndStore` explicitly sets `null` for an empty custom-stats map — a subtle divergence documented in more detail below.

**Verdict**: APPROVED

---

## High-level view

The partial update pattern is implemented correctly at every layer: `@DecimalMin("0.0")` on the DTO blocks negative values at the controller boundary, `@Valid` on the request body activates it, the service only overwrites fields that are non-null, and `@Transactional` ensures the save is atomic. The `leagueId` path variable is accepted for URL consistency with other admin fixture endpoints but is intentionally not forwarded to the service — the fixture already carries its league membership.

The fixture status guard uses enum identity comparison (`!=`) against `FixtureStatus.IN_PROGRESS` and `FixtureStatus.COMPLETED`, which correctly blocks `SCHEDULED`, `POSTPONED`, and `CANCELLED`. `InvalidStateTransitionException` is confirmed to map to HTTP 400 via the global handler.

Security is covered at two layers: the URL prefix `/api/v1/admin/**` restricts the path to `ADMIN` in `SecurityConfig`, and `@PreAuthorize("hasRole('ADMIN')")` on the controller method adds method-level enforcement consistent with the existing `recomputePostGame` endpoint.

The `customStats` replace-on-non-null behaviour has a gap: `serializeCustomStats` returns `""` for an empty map, so sending `"customStats": {}` stores `""` in the column instead of `null`. The response serialises both `null` and `""` identically (empty map via `deserializeCustomStats`), so API consumers see no difference — but the stored value is inconsistent with what `computeAndStore` writes when it finds no custom entries (it stores `null`). This is low severity but worth noting.

There are no tests for the new method. Given that the status guard has multiple branches, the partial-update field-coverage, and the `customStats` edge case, adding service-layer unit tests would meaningfully reduce regression risk.

---

<details>
<summary>Issues (2)</summary>

1. **`customStats: {}` stores `""` not `null`** — `serializeCustomStats` returns `""` for an empty map. The `updatePostGameStats` method calls it unconditionally when `request.getCustomStats() != null`, so sending `{}` writes `""` to the column while `computeAndStore` writes `null` for the same logical state. The fix is to store `null` when the serialised result is blank: `summary.setCustomStats(json.isBlank() ? null : json)` after the serialize call.

2. **No unit tests for `updatePostGameStats`** — the status guard has four distinct branches (two allowed, three blocked), the partial-update logic covers nine fields independently, and the `customStats` replace-vs-leave behaviour is non-obvious. None of these are covered by existing tests. Add a service-layer unit test class (or extend the existing one if it exists) covering: fixture-not-found, invalid status (SCHEDULED, CANCELLED), summary-not-found, partial field update (only non-null fields change), and the `customStats` null-vs-empty distinction.

</details>

---

<details>
<summary>Details</summary>

### `customStats` empty-map vs null storage (confirmed)

`serializeCustomStats` returns `""` for an empty map:

```java
private String serializeCustomStats(Map<String, BigDecimal> map) {
    if (map.isEmpty()) return "";
    // ...
}
```

In `updatePostGameStats`:

```java
if (request.getCustomStats() != null) {
    summary.setCustomStats(serializeCustomStats(request.getCustomStats()));
}
```

Sending `"customStats": {}` passes a non-null empty map, serialises to `""`, and stores `""` in the column. `computeAndStore` handles the same logical state differently:

```java
summary.setCustomStats(customStatsJson.isEmpty() ? null : customStatsJson);
```

`deserializeCustomStats` treats both `null` and blank as an empty map, so API responses are identical — but direct column reads or schema constraints see inconsistent values between rows written by the two code paths. Fix: after the serialize call, assign `json.isBlank() ? null : json`.

### Test coverage

No tests exist for `updatePostGameStats` in the service or controller layers — confirmed by searching the test source tree. The branches that warrant coverage: fixture not found (404), fixture in blocked status (SCHEDULED/POSTPONED/CANCELLED → 400), summary not found (404), partial field update (only non-null fields change), `computedAt` is refreshed on every call, and the `customStats` null-vs-empty-map replace behaviour.

</details>

---

<details>
<summary>File map</summary>

| File | What changed |
|------|-------------|
| `league/dto/request/UpdatePostGameStatsRequest.java` | New DTO — nine optional `BigDecimal` fields with `@DecimalMin("0.0")`, optional `customStats` map |
| `league/service/PostGameStatsService.java` | New `updatePostGameStats` method signature added to interface |
| `league/service/impl/PostGameStatsServiceImpl.java` | `updatePostGameStats` implemented — status guard, partial field apply, save |
| `league/controller/PostGameStatsController.java` | New `PUT` endpoint with `@PreAuthorize("hasRole('ADMIN')")` and `@Valid` |
| `docs/post-game-stats-user-guide.md` | New permissions row, new "Update a Player's Post-Game Stats" section, new error reference row |
| `config/SecurityConfig.java` | No change — existing `/api/v1/admin/**` rule covers the new URL |

Full diff: `git diff main -- src/main/java/com/feadev/eaids/league/ docs/post-game-stats-user-guide.md`

</details>
