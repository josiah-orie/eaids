# EAIDS

**Elite Athlete Identification, Development & Scouting**

A structured, data-driven platform for discovering and developing elite American football talent across Nigeria and other participating countries.

## Tech Stack

- **Java 21** / **Spring Boot 4.1.0**
- **MySQL** with Spring Data JPA
- **Flyway** for database migrations
- **Spring Security** with JWT authentication
- **Maven** (with Maven Wrapper)
- **Lombok** for reducing boilerplate
- **SpringDoc OpenAPI** for API documentation

## Prerequisites

- Java 21+
- MySQL 8+ (running locally on port 3306)
- Maven 3.9+ (or use the included `./mvnw` wrapper)

## Getting Started

### 1. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE eaidsdb;
```

### 2. Environment Variables

Set the following environment variables (or rely on defaults in `application-dev.properties`):

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_PASSWORD` | MySQL root password | _(empty)_ |
| `GMAIL_USERNAME` | SMTP email address | `jovine360helpdesk@gmail.com` |
| `GMAIL_PASSWORD` | SMTP app password | _(configured in properties)_ |

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

The application starts on **port 8082** with the `dev` profile active.

### 4. Access the API

- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8082/v3/api-docs

## Common Commands

```bash
# Compile
./mvnw compile

# Run tests
./mvnw test

# Package (build WAR)
./mvnw package

# Run the application
./mvnw spring-boot:run

# Clean build
./mvnw clean install

# Skip tests during build
./mvnw package -DskipTests
```

## Project Structure

```
src/main/java/com/feadev/eaids/
├── registrations/       # IAM: registration, auth, user management
├── scouting/            # Scouting portal, scout profiles, bookmarks
├── camp/                # Camp lifecycle management
├── enrollment/          # Athlete camp enrollment
├── assessment/          # Assessment templates
├── physical/            # Physical measurements & benchmarks
├── technical/           # Technical skill scoring
├── tactical/            # Tactical evaluation scoring
├── evaluation/          # Coach/scout evaluations
├── scoring/             # Composite score calculation engine
├── ranking/             # Camp rankings
├── selection/           # Athlete selection decisions
├── nationalranking/     # National ranking system
├── analytics/           # Performance analytics & predictions
├── development/         # Development plans
├── trainingload/        # Training load monitoring
├── matchstats/          # Match statistics
├── reporting/           # PDF/Excel report generation
├── attendance/          # Camp attendance tracking
├── discipline/          # Discipline records
├── videolibrary/        # Video library management
├── dashboard/           # Role-specific dashboards
├── adminprofile/        # Admin profile management
├── positionprofile/     # Football position profiles
├── assessmenttemplate/  # Assessment template management
├── history/             # Historical data
├── config/              # Spring configuration
├── security/            # JWT filter, token provider
└── audit/               # JPA auditing infrastructure
```

## Modules Overview

### Identity & Access Management

Multi-step registration for athletes and coaches, admin-created scout accounts, JWT authentication, role-based access control (ROLE_ATHLETE, ROLE_COACH, ROLE_SCOUT, ROLE_ADMIN), email verification, password reset via OTP.

### Core Assessment Engine

Camps with configurable stages, physical/technical/tactical metrics, benchmarks, weight profiles, composite scoring, and automated ranking.

### Scouting

Admin-created scout accounts with password setup flow, scout profile completion gate, athlete search with access scopes, bookmarking, interview requests, and report generation.

### Analytics & Development

Position recommendations, potential predictions, injury risk assessment, national rankings, development plans, and training load monitoring.

## User Roles

| Role | Registration | Key Capabilities |
|------|-------------|-----------------|
| Athlete | Self-registration | Profile, videos, view evaluations |
| Coach | Self-registration | Evaluate athletes, development plans |
| Scout | Admin-created | Search athletes, bookmarks, interviews |
| Admin | Pre-seeded/internal | Full platform management |

## API Documentation

Detailed user guides by role are available in the `docs/` directory:

- `docs/USER_GUIDE-ADMIN.md` — Admin API reference
- `docs/USER_GUIDE-ATHLETE.md` — Athlete API reference
- `docs/USER_GUIDE-COACH.md` — Coach API reference
- `docs/USER_GUIDE-SCOUT.md` — Scout API reference
- `docs/seed-data-reference.md` — Seed data credentials for testing

## Seed Data

The application includes seed data for development. See `docs/seed-data-reference.md` for test account credentials.

## License

Proprietary. All rights reserved.
