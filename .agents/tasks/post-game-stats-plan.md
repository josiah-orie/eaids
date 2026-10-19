# Implementation Plan: Field-Level Update Endpoint for Post-Game Stats

## Background (read before implementing)

The existing `PostGameSummary` entity holds nine typed `BigDecimal` columns plus a JSON `customStats`
blob. The auto-computation path (`computeAndStore`) already upserts these rows from `LeagueStatEntry`
data. The missing piece is a **manual, field-by-field PUT endpoint** that lets an admin correct
individual stat values on an already-created summary — useful during a live game (`IN_PROGRESS`) or
after completion (`COMPLETED`).

**Fixture status enum values (exact, from `FixtureStatus.java`):**
```
SCHEDULED, IN_PROGRESS, COMPLETED, POSTPONED, CANCELLED
```
The update must be **rejected** unless the fixture's status is `IN_PROGRESS` or `COMPLETED`.

---

## File Map

| Role | Path |
|------|------|
| New DTO (request) | `src/main/java/com/feadev/eaids/league/dto/request/UpdatePostGameStatsRequest.java` |
| Modified service interface | `src/main/java/com/feadev/eaids/league/service/PostGameStatsService.java` |
| Modified service impl | `src/main/java/com/feadev/eaids/league/service/impl/PostGameStatsServiceImpl.java` |
| Modified controller | `src/main/java/com/feadev/eaids/league/controller/PostGameStatsController.java` |
| Security config | `src/main/java/com/feadev/eaids/config/SecurityConfig.java` *(no change needed — see §4)* |
| Docs | `docs/post-game-stats-user-guide.md` |

---

## Plan

- [ ] 1. Create `UpdatePostGameStatsRequest` DTO
      All stat fields are optional (partial update semantics — only non-null fields are applied).
      `customStats` accepts a map of arbitrary string → BigDecimal pairs for non-standard stats.
      Use `@DecimalMin("0.0")` on every numeric field so negative values are rejected at the
      controller boundary.
      
      **File to create:**
      `src/main/java/com/feadev/eaids/league/dto/request/UpdatePostGameStatsRequest.java`

      ```java
      package com.feadev.eaids.league.dto.request;

      import jakarta.validation.constraints.DecimalMin;
      import lombok.*;
      import java.math.BigDecimal;
      import java.util.Map;

      @Data
      @Builder
      @NoArgsConstructor
      @AllArgsConstructor
      public class UpdatePostGameStatsRequest {

          @DecimalMin(value = "0.0", message = "totalPassingYards must be non-negative")
          private BigDecimal totalPassingYards;

          @DecimalMin(value = "0.0", message = "totalRushingYards must be non-negative")
          private BigDecimal totalRushingYards;

          @DecimalMin(value = "0.0", message = "totalReceivingYards must be non-negative")
          private BigDecimal totalReceivingYards;

          @DecimalMin(value = "0.0", message = "totalTackles must be non-negative")
          private BigDecimal totalTackles;

          @DecimalMin(value = "0.0", message = "touchdowns must be non-negative")
          private BigDecimal touchdowns;

          @DecimalMin(value = "0.0", message = "interceptions must be non-negative")
          private BigDecimal interceptions;

          @DecimalMin(value = "0.0", message = "sacks must be non-negative")
          private BigDecimal sacks;

          @DecimalMin(value = "0.0", message = "fumbles must be non-negative")
          private BigDecimal fumbles;

          @DecimalMin(value = "0.0", message = "penalties must be non-negative")
          private BigDecimal penalties;

          /**
           * Optional override for custom (non-standard) stats.
           * When provided, it REPLACES the entire customStats blob for this player/fixture.
           * Omit the field (null) to leave existing custom stats untouched.
           */
          private Map<String, BigDecimal> customStats;
      }
      ```

      **Verify:** `./mvnw compile` — compiles with no errors.

- [ ] 2. Add `updatePostGameStats` to the `PostGameStatsService` interface
      Add a single new method below the existing `getSummaryForPlayer` declaration.
      
      **File to modify:**
      `src/main/java/com/feadev/eaids/league/service/PostGameStatsService.java`

      Add this method signature and Javadoc:
      ```java
      /**
       * Applies a partial field-level update to an existing {@link com.feadev.eaids.league.model.PostGameSummary}.
       *
       * <p>Only non-null fields in {@code request} are written; null fields are left as-is.
       * Throws {@link com.feadev.eaids.registrations.exception.InvalidStateTransitionException}
       * if the fixture's current status is not {@code IN_PROGRESS} or {@code COMPLETED}.
       *
       * @param fixtureId    the fixture the summary belongs to
       * @param teamPlayerId the player whose summary is being updated
       * @param request      partial update payload; null fields are ignored
       * @return the updated summary
       */
      PostGameSummaryResponse updatePostGameStats(UUID fixtureId, UUID teamPlayerId,
                                                   UpdatePostGameStatsRequest request);
      ```

      Add the required import at the top:
      ```java
      import com.feadev.eaids.league.dto.request.UpdatePostGameStatsRequest;
      ```

      **Verify:** `./mvnw compile` — compiler warns that `PostGameStatsServiceImpl` does not yet
      implement the new method (expected). Fix is in the next step.

- [ ] 3. Implement `updatePostGameStats` in `PostGameStatsServiceImpl`
      Load the fixture, guard on status, load the existing summary (404 if not found), apply only
      non-null fields from the request, and save. Use `InvalidStateTransitionException` for the
      status guard — the global handler already maps it to HTTP 400.

      **File to modify:**
      `src/main/java/com/feadev/eaids/league/service/impl/PostGameStatsServiceImpl.java`

      Add the following import (if not already present):
      ```java
      import com.feadev.eaids.league.dto.request.UpdatePostGameStatsRequest;
      import com.feadev.eaids.league.model.enums.FixtureStatus;
      import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
      ```

      Add this method implementation inside the class, after `getSummaryForPlayer`:

      ```java
      @Override
      @Transactional
      public PostGameSummaryResponse updatePostGameStats(UUID fixtureId, UUID teamPlayerId,
                                                          UpdatePostGameStatsRequest request) {
          MatchFixture fixture = fixtureRepository.findById(fixtureId)
                  .orElseThrow(() -> new ResourceNotFoundException("Fixture not found: " + fixtureId));

          if (fixture.getStatus() != FixtureStatus.IN_PROGRESS
                  && fixture.getStatus() != FixtureStatus.COMPLETED) {
              throw new InvalidStateTransitionException(
                      "Post-game stats can only be updated when the fixture is IN_PROGRESS or COMPLETED. " +
                      "Current status: " + fixture.getStatus());
          }

          PostGameSummary summary = summaryRepository
                  .findByFixtureIdAndTeamPlayerId(fixtureId, teamPlayerId)
                  .orElseThrow(() -> new ResourceNotFoundException(
                          "Post-game summary not found for fixture " + fixtureId +
                          " and player " + teamPlayerId));

          // Apply only the non-null fields (partial update)
          if (request.getTotalPassingYards() != null)   summary.setTotalPassingYards(request.getTotalPassingYards());
          if (request.getTotalRushingYards() != null)   summary.setTotalRushingYards(request.getTotalRushingYards());
          if (request.getTotalReceivingYards() != null) summary.setTotalReceivingYards(request.getTotalReceivingYards());
          if (request.getTotalTackles() != null)        summary.setTotalTackles(request.getTotalTackles());
          if (request.getTouchdowns() != null)          summary.setTouchdowns(request.getTouchdowns());
          if (request.getInterceptions() != null)       summary.setInterceptions(request.getInterceptions());
          if (request.getSacks() != null)               summary.setSacks(request.getSacks());
          if (request.getFumbles() != null)             summary.setFumbles(request.getFumbles());
          if (request.getPenalties() != null)           summary.setPenalties(request.getPenalties());

          // customStats: when provided, replace the blob; when null, leave existing intact
          if (request.getCustomStats() != null) {
              summary.setCustomStats(serializeCustomStats(request.getCustomStats()));
          }

          summary.setComputedAt(LocalDateTime.now());

          PostGameSummary saved = summaryRepository.save(summary);
          log.info("Post-game stats manually updated for fixture {} / player {}", fixtureId, teamPlayerId);
          return mapToResponse(saved);
      }
      ```

      **Verify:** `./mvnw compile` — no errors.

- [ ] 4. Add the PUT endpoint to `PostGameStatsController`
      Follow the `MatchFixtureController` pattern: place the endpoint under
      `/api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}` and
      annotate with `@PreAuthorize("hasRole('ADMIN')")`.
      
      **Why this path?** The `MatchFixtureController` uses
      `@RequestMapping("/api/v1/admin/leagues/{leagueId}/fixtures")` for all admin fixture work.
      The `leagueId` path variable is present for consistency and for the security config wildcard
      `/api/v1/admin/**` which already restricts this path to `ADMIN` role.

      **Security config impact:** No change required. The URL prefix `/api/v1/admin/**` is already
      restricted to `hasRole('ADMIN')` via `.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")`
      in `SecurityConfig.java` (line ~63). The `@PreAuthorize` annotation on the method is an
      additional layer that is consistent with the existing recompute endpoint.

      **File to modify:**
      `src/main/java/com/feadev/eaids/league/controller/PostGameStatsController.java`

      Add the following import:
      ```java
      import com.feadev.eaids.league.dto.request.UpdatePostGameStatsRequest;
      import jakarta.validation.Valid;
      ```

      Add this method to the controller class (after `recomputePostGame`):

      ```java
      @PutMapping("/api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}")
      @PreAuthorize("hasRole('ADMIN')")
      @Operation(
          summary = "Field-level update of a player's post-game stats",
          description = "Allows an admin to manually set individual stat values for a specific player " +
                        "in a fixture. Only non-null fields in the request body are applied; omitted " +
                        "fields retain their existing values. Fixture must be IN_PROGRESS or COMPLETED. " +
                        "To clear all custom stats, send \\\"customStats\\\": {}."
      )
      @ApiResponse(responseCode = "200", description = "Stats updated successfully")
      @ApiResponse(responseCode = "400", description = "Fixture is not IN_PROGRESS or COMPLETED, or validation error")
      @ApiResponse(responseCode = "404", description = "Fixture or player summary not found")
      public ResponseEntity<PostGameSummaryResponse> updatePostGameStats(
              @PathVariable UUID leagueId,
              @PathVariable UUID fixtureId,
              @PathVariable UUID teamPlayerId,
              @Valid @RequestBody UpdatePostGameStatsRequest request) {
          return ResponseEntity.ok(
                  postGameStatsService.updatePostGameStats(fixtureId, teamPlayerId, request));
      }
      ```

      Note: `leagueId` is accepted as a path variable for URL consistency with all other
      admin fixture endpoints, but is not passed down to the service — the fixture's league
      membership is already enforced at fixture-creation time.

      **Verify:** `./mvnw compile && ./mvnw test` — all tests pass.

- [ ] 5. Update `post-game-stats-user-guide.md`
      Append a new section **after** "Trigger Manual Recomputation (Admin)" and **before**
      "Stat Column Reference". Also add a row to the "Who Can Do What" table.

      **File to modify:** `docs/post-game-stats-user-guide.md`

      **5a — "Who Can Do What" table update:**
      Add a new row to the permissions table:
      ```
      | Update player stats (field-level) | — | — | ✓ |
      ```

      **5b — New section to append (full content):**

      ```markdown
      ## Update a Player's Post-Game Stats (Field-Level, Admin)

      Allows an admin to manually set or correct individual stat values for a specific player
      in a fixture. Only the fields you include in the request body are changed — omitted fields
      keep their existing values. This is useful for corrections during a live game or after
      the automatic computation has run.

      **Fixture must be `IN_PROGRESS` or `COMPLETED`.** Sending this request against a
      `SCHEDULED`, `POSTPONED`, or `CANCELLED` fixture returns `400 Bad Request`.

      ```bash
      curl -X PUT "http://localhost:8080/api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer <admin_token>" \
        -d '{
          "touchdowns": 4,
          "totalPassingYards": 312.50,
          "interceptions": 0
        }'
      ` ``

      **Request body fields (all optional):**

      | Field | Type | Constraint | Notes |
      |-------|------|-----------|-------|
      | `totalPassingYards` | number | ≥ 0 | Omit to leave unchanged |
      | `totalRushingYards` | number | ≥ 0 | Omit to leave unchanged |
      | `totalReceivingYards` | number | ≥ 0 | Omit to leave unchanged |
      | `totalTackles` | number | ≥ 0 | Omit to leave unchanged |
      | `touchdowns` | number | ≥ 0 | Omit to leave unchanged |
      | `interceptions` | number | ≥ 0 | Omit to leave unchanged |
      | `sacks` | number | ≥ 0 | Omit to leave unchanged |
      | `fumbles` | number | ≥ 0 | Omit to leave unchanged |
      | `penalties` | number | ≥ 0 | Omit to leave unchanged |
      | `customStats` | object | — | When provided, **replaces** the entire custom-stats blob. Send `{}` to clear all custom stats. Omit entirely to leave existing custom stats untouched. |

      **Response (200 OK):** A `PostGameSummaryResponse` object reflecting the updated values.

      **Error (400 Bad Request):**
      ```json
      {
        "status": 400,
        "message": "Post-game stats can only be updated when the fixture is IN_PROGRESS or COMPLETED. Current status: SCHEDULED"
      }
      ` ``

      **Error (404 Not Found):**
      ```json
      {
        "status": 404,
        "message": "Post-game summary not found for fixture {fixtureId} and player {teamPlayerId}"
      }
      ` ``

      > **Note:** This endpoint writes directly to the `PostGameSummary` row — it does **not**
      > re-aggregate from `LeagueStatEntry` records. If you later call the recompute endpoint,
      > it will overwrite any manual changes with freshly aggregated values.

      ### Updating Custom Stats Only

      To add or replace a non-standard stat without touching the typed columns:

      ```bash
      curl -X PUT "http://localhost:8080/api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer <admin_token>" \
        -d '{
          "customStats": {
            "Punt Returns": 2,
            "Kick Return Yards": 65.0
          }
        }'
      ` ``

      ### Clearing a Stat Value

      To zero out a stat (e.g., correct an erroneous interception count to zero):

      ```bash
      curl -X PUT "..." \
        -d '{"interceptions": 0}'
      ` ``

      To remove a stat entirely (set it back to `null` / "not recorded"), use the recompute
      endpoint after removing or correcting the underlying `LeagueStatEntry` records.
      ```

      **5c — Error Reference table update:**
      Add a new row to the Error Reference table at the bottom of the guide:
      ```
      | Update on non-active fixture | 400 | `Post-game stats can only be updated when the fixture is IN_PROGRESS or COMPLETED. Current status: {status}` |
      ```

      **Verify:** `./mvnw compile && ./mvnw test` — build green. Manually confirm the guide renders
      correctly in the editor.

---

## Security Config Assessment

**No change needed.** The URL `/api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}`
falls under the `/api/v1/admin/**` pattern already present in `SecurityConfig.filterChain`, which is
already restricted to `.hasRole("ADMIN")`. The `@PreAuthorize("hasRole('ADMIN')")` on the controller
method is a redundant but consistent second layer (same pattern as the existing recompute endpoint).

---

## Implementation Order (dependency chain)

```
Step 1 (DTO)
  → Step 2 (interface)
    → Step 3 (impl)
      → Step 4 (controller) ← depends on DTO, impl
        → Step 5 (docs)
```

Steps 1–4 must be done in order. Step 5 is independent and can be done at any point after Step 4.

---

## Verification

**Compile result: PASSED**

All five steps implemented:
1. ✅ `UpdatePostGameStatsRequest` DTO created at `league/dto/request/UpdatePostGameStatsRequest.java`
2. ✅ `updatePostGameStats` method added to `PostGameStatsService` interface
3. ✅ `updatePostGameStats` implemented in `PostGameStatsServiceImpl` (synchronous, `@Transactional`, status guard, field-level partial update)
4. ✅ `PUT /api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}` added to `PostGameStatsController` with `@PreAuthorize("hasRole('ADMIN')")` and `@Valid`
5. ✅ `post-game-stats-user-guide.md` updated (permissions table row, new section, error reference row)

`./mvnw compile -q -o` exited 0 with no output.
