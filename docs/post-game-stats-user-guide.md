# Post-Game Athlete Statistics

## Overview

The Post-Game Stats module generates a per-athlete performance snapshot after every completed fixture. Snapshots are computed automatically when a fixture transitions to `COMPLETED` and are available within seconds. Admins can also trigger a manual recomputation at any time.

**Key capabilities:**
- Automatic computation from raw `LeagueStatEntry` data on fixture completion
- Per-athlete snapshots grouped by home/away team for easy match review
- Typed columns for the nine core American football stats; non-standard stats overflow to a `customStats` map
- Paginated per-athlete career history across all fixtures
- Admin-triggered recomputation for corrections after stat entry updates

**Base URL:** `http://localhost:8080`

---

## Who Can Do What

| Action | Unauthenticated | ATHLETE / COACH / SCOUT | ADMIN |
|--------|:--------------:|:-----------------------:|:-----:|
| Get fixture post-game breakdown | — | ✓ | ✓ |
| Get per-player post-game summary | — | ✓ | ✓ |
| Get athlete's career post-game history | — | ✓ | ✓ |
| Trigger manual recomputation | — | — | ✓ |
| Update player stats (field-level) | — | — | ✓ |

---

## Authentication

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"coach@eaids.com","password":"Password@123"}'
```

Include the token in every authenticated request:
```
Authorization: Bearer <accessToken>
```

---

## Automatic Computation Flow

When a fixture is completed, post-game summaries are computed automatically:

```
Admin completes fixture
      ↓
POST /admin/leagues/{leagueId}/fixtures/{fixtureId}/status  {"status":"COMPLETED","homeScore":28,"awayScore":21}
      ↓
Fixture status saved → async background job starts
      ↓
LeagueStatEntry rows for this fixture loaded and grouped by athlete
      ↓
PostGameSummary upserted per athlete (one row per fixture per player)
      ↓
GET /fixtures/{fixtureId}/post-game returns populated data
```

The computation runs **asynchronously** — it does not delay the fixture-completion response. Allow a few seconds for the data to appear.

---

## Get Post-Game Breakdown for a Fixture

Returns per-athlete statistics grouped into home team and away team sections.

```bash
curl -X GET http://localhost:8080/api/v1/fixtures/{fixtureId}/post-game \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "fixtureId": "f1a2b3c4-d5e6-7890-fabc-123456789012",
  "homeTeamId": "e5f6a7b8-c9d0-1234-efab-567890123456",
  "homeTeamName": "Lagos Lions",
  "awayTeamId": "b8c9d0e1-f2a3-4567-bcde-890123456789",
  "awayTeamName": "Abuja Eagles",
  "homeScore": 28,
  "awayScore": 21,
  "homeTeamStats": [
    {
      "id": "s1a2b3c4-d5e6-7890-sabc-123456789012",
      "fixtureId": "f1a2b3c4-d5e6-7890-fabc-123456789012",
      "teamPlayerId": "pp1a2b3c-d4e5-6789-abcd-ef1234567890",
      "athleteUserId": "au1b2c3d-e4f5-6789-bcde-f12345678901",
      "teamId": "e5f6a7b8-c9d0-1234-efab-567890123456",
      "playerName": "emeka.obi@example.com",
      "jerseyNumber": 12,
      "position": "QUARTERBACK",
      "totalPassingYards": 285.00,
      "totalRushingYards": null,
      "totalReceivingYards": null,
      "totalTackles": null,
      "touchdowns": 3.00,
      "interceptions": 1.00,
      "sacks": null,
      "fumbles": null,
      "penalties": null,
      "customStats": {},
      "computedAt": "2026-10-12T17:45:02",
      "createdDate": "2026-10-12T17:45:02"
    },
    {
      "id": "s2b3c4d5-e6f7-8901-sbcd-234567890123",
      "fixtureId": "f1a2b3c4-d5e6-7890-fabc-123456789012",
      "teamPlayerId": "pp2b3c4d-e5f6-7890-bcde-f23456789012",
      "athleteUserId": "au2c3d4e-f5a6-7890-cdef-123456789012",
      "teamId": "e5f6a7b8-c9d0-1234-efab-567890123456",
      "playerName": "biodun.ola@example.com",
      "jerseyNumber": 55,
      "position": "LINEBACKER",
      "totalPassingYards": null,
      "totalRushingYards": null,
      "totalReceivingYards": null,
      "totalTackles": 8.00,
      "touchdowns": null,
      "interceptions": 1.00,
      "sacks": 2.00,
      "fumbles": null,
      "penalties": null,
      "customStats": {},
      "computedAt": "2026-10-12T17:45:02",
      "createdDate": "2026-10-12T17:45:02"
    }
  ],
  "awayTeamStats": [
    {
      "playerName": "chukwudi.eze@example.com",
      "jerseyNumber": 7,
      "position": "QUARTERBACK",
      "totalPassingYards": 194.00,
      "touchdowns": 2.00,
      "interceptions": 2.00,
      "customStats": {},
      "computedAt": "2026-10-12T17:45:03"
    }
  ]
}
```

> `null` values mean no stat entries of that type were recorded for this player in this fixture. `customStats` is a map of non-standard stat names to their summed values.

---

## Get a Single Player's Post-Game Summary

```bash
curl -X GET http://localhost:8080/api/v1/fixtures/{fixtureId}/post-game/{teamPlayerId} \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):** A single `PostGameSummaryResponse` object (same shape as the individual objects above).

**Error (404):**
```json
{
  "status": 404,
  "message": "Post-game summary not found for fixture {fixtureId} and player {teamPlayerId}"
}
```

This 404 means either:
- The fixture is not yet `COMPLETED`
- The async computation has not finished yet (wait a few seconds and retry)
- The player had no stat entries recorded for this fixture

---

## Get an Athlete's Post-Game History

Returns a paginated list of post-game summaries across all fixtures the athlete has participated in, most recent first.

```bash
# Default page (page 0, 20 per page)
curl -X GET http://localhost:8080/api/v1/athletes/{athleteId}/post-game-history \
  -H "Authorization: Bearer <token>"

# Custom pagination
curl -X GET "http://localhost:8080/api/v1/athletes/{athleteId}/post-game-history?page=0&size=10&sort=createdDate,desc" \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):** A paginated wrapper:
```json
{
  "content": [
    {
      "id": "s1a2b3c4-...",
      "fixtureId": "f1a2b3c4-...",
      "teamId": "e5f6a7b8-...",
      "playerName": "emeka.obi@example.com",
      "position": "QUARTERBACK",
      "totalPassingYards": 285.00,
      "touchdowns": 3.00,
      "interceptions": 1.00,
      "computedAt": "2026-10-12T17:45:02"
    },
    {
      "fixtureId": "f0z9y8x7-...",
      "totalPassingYards": 312.00,
      "touchdowns": 2.00,
      "interceptions": 0.00,
      "computedAt": "2026-09-28T16:30:11"
    }
  ],
  "totalElements": 6,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

## Trigger Manual Recomputation (Admin)

Recomputes all player summaries for a fixture. Use this if stat entries were updated or added after the automatic computation ran.

```bash
curl -X POST http://localhost:8080/api/v1/admin/fixtures/{fixtureId}/post-game/recompute \
  -H "Authorization: Bearer <admin_token>"
```

**Response (202 Accepted)** — The job runs asynchronously. Refresh the post-game endpoint after a few seconds.

> **Idempotent:** Running recomputation multiple times is safe. Each run upserts existing rows rather than creating duplicates.

---

## Update a Player's Post-Game Stats (Field-Level, Admin)

Allows an admin to manually set or correct individual stat values for a specific player in a fixture. Only the fields you include in the request body are changed — omitted fields keep their existing values. Useful for corrections during a live game or after the automatic computation has run.

**Fixture must be `IN_PROGRESS` or `COMPLETED`.** Sending this request against a `SCHEDULED`, `POSTPONED`, or `CANCELLED` fixture returns `400 Bad Request`.

```bash
curl -X PUT "http://localhost:8080/api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "touchdowns": 4,
    "totalPassingYards": 312.50,
    "interceptions": 0
  }'
```

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

**Error (400 Bad Request)** — fixture status is `SCHEDULED`, `POSTPONED`, or `CANCELLED`, or a field value is negative:
```json
{
  "status": 400,
  "message": "Post-game stats can only be updated when the fixture is IN_PROGRESS or COMPLETED. Current status: SCHEDULED"
}
```

**Error (403 Forbidden)** — caller does not have the `ADMIN` role:
```json
{
  "status": 403,
  "message": "Forbidden: You do not have permission to access this resource"
}
```

**Error (404 Not Found)** — no `PostGameSummary` row exists yet for this fixture/player combination:
```json
{
  "status": 404,
  "message": "Post-game summary not found for fixture {fixtureId} and player {teamPlayerId}"
}
```

> **Note:** This endpoint writes directly to the `PostGameSummary` row — it does **not** re-aggregate from `LeagueStatEntry` records. If you later call the recompute endpoint, it will overwrite any manual changes with freshly aggregated values.

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
```

### Clearing a Stat Value

To zero out a stat (e.g., correct an erroneous interception count to zero):

```bash
curl -X PUT "http://localhost:8080/api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"interceptions": 0}'
```

To remove a stat entirely (set it back to `null` / "not recorded"), use the recompute endpoint after removing or correcting the underlying `LeagueStatEntry` records.

---

## Stat Column Reference

| JSON field | Stat name matched | Typical position |
|------------|-------------------|-----------------|
| `totalPassingYards` | "Passing Yards" | QB |
| `totalRushingYards` | "Rushing Yards" | RB |
| `totalReceivingYards` | "Receiving Yards" | WR / TE |
| `totalTackles` | "Tackles" | LB / DL / DB |
| `touchdowns` | "Touchdowns" | Any |
| `interceptions` | "Interceptions" | QB (thrown) or DB (caught) |
| `sacks` | "Sacks" | DL / LB |
| `fumbles` | "Fumbles" | Any ball-carrier |
| `penalties` | "Penalties" | Any |
| `customStats` | Everything else | Any |

Stat names are matched **case-insensitively** against the `MatchStatDefinition.statName` field. Any definition name not in the nine above appears as a key in `customStats`.

---

## Full Workflow Example

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin@eaids.com","password":"Password@123"}' | jq -r '.accessToken')

LEAGUE_ID="<your-league-id>"
FIXTURE_ID="<your-fixture-id>"

# 1. Start the fixture
curl -s -X PATCH "http://localhost:8080/api/v1/admin/leagues/$LEAGUE_ID/fixtures/$FIXTURE_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status":"IN_PROGRESS"}' > /dev/null

# 2. Record stats during the game
curl -s -X POST "http://localhost:8080/api/v1/admin/leagues/$LEAGUE_ID/fixtures/$FIXTURE_ID/league-stats" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "entries": [
      {"teamPlayerId":"<qb-player-id>","definitionId":"<passing-yards-def-id>","value":285},
      {"teamPlayerId":"<qb-player-id>","definitionId":"<touchdowns-def-id>","value":3},
      {"teamPlayerId":"<lb-player-id>","definitionId":"<tackles-def-id>","value":8},
      {"teamPlayerId":"<lb-player-id>","definitionId":"<sacks-def-id>","value":2}
    ]
  }' > /dev/null

# 3. Complete the fixture — triggers automatic post-game computation
curl -s -X PATCH "http://localhost:8080/api/v1/admin/leagues/$LEAGUE_ID/fixtures/$FIXTURE_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status":"COMPLETED","homeScore":28,"awayScore":21}' > /dev/null

# 4. Wait a moment for async computation, then view results
sleep 2

curl -X GET "http://localhost:8080/api/v1/fixtures/$FIXTURE_ID/post-game" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Error Reference

| Scenario | Status | Message |
|----------|--------|---------|
| Fixture not found | 404 | `Fixture not found: {id}` |
| Player summary not found | 404 | `Post-game summary not found for fixture {id} and player {id}` |
| Unauthenticated read | 401 | `Unauthorized: Authentication is required...` |
| Non-admin recompute or field-level update | 403 | `Forbidden: You do not have permission...` |
| Update on non-active fixture | 400 | `Post-game stats can only be updated when the fixture is IN_PROGRESS or COMPLETED. Current status: {status}` |
