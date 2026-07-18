# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Lunatech Chef is a full-stack meal planning application for Lunatech offices. Users can view upcoming meals and sign up/unsign for them. Admin users can manage offices, dishes, menus, and schedules.

## Build and Run Commands

```bash
# Build everything (backend + frontend)
gradle buildAll

# Start Postgres + Keycloak containers, then run the app (serves on http://localhost:8080)
gradle devRun

# Run backend only (expects containers already running; this is also the Clever Cloud deploy command)
gradle run

# Run backend tests (requires Docker: testcontainers boots Postgres and Keycloak)
gradle test

# Lint / format Kotlin code
gradle ktlintCheck
gradle ktlintFormat

# Frontend commands (from /frontend directory)
npm ci           # Install dependencies
npm start        # Dev server on http://localhost:3000
npm run build    # Production build
npm test         # Run tests
```

IMPORTANT: Clever Cloud deploys with `gradle --no-daemon run` and builds with `buildAll` (see `clevercloud/gradle.json`). Never make `run` or `buildAll` depend on Docker Compose or tests; dev-only wiring belongs in `devRun`.

## Architecture

### Tech Stack
- **Backend**: Kotlin + Ktor 3.3, Ktorm ORM, PostgreSQL, Flyway migrations
- **Frontend**: React 18 + Redux Toolkit, React Router, React Bootstrap
- **Auth**: Keycloak. The frontend sends the ID token to `/login`; the backend verifies signature (JWKS), issuer, and audience (`jwt.clientId`, `JWT_CLIENT_ID=lunachef` in prod, default `lunachef-local`). Admin = the app-specific `admin` client role, delivered as a flat `roles` claim by a User Client Role mapper (Token Claim Name `roles`, Client ID set, Add to ID token ON); the `backoffice` and `hrm` groups hold that role. Roles are per-client in Keycloak.
- **Scheduling**: Quartz for recurring tasks (auto-schedule creation, monthly reports, Slack lunch reminders)

### Backend Structure (`src/main/com/lunatech/chef/api/`)
- `Application.kt` - Main entry point, Ktor server setup with all routes
- `routes/` - HTTP route handlers (Offices, Dishes, Menus, Schedules, Attendances, Users, Reports, etc.)
- `persistence/schemas/` - Ktorm ORM entity mappings
- `persistence/services/` - Business logic layer
- `schedulers/` - Quartz jobs for recurring schedules, monthly reports, and Slack lunch reminders
- `auth/` - Authorization logic and role handling
- `config/` - Configuration classes

### Frontend Structure (`frontend/src/`)
- `components/` - React components organized by feature (auth/, admin/, shared/)
- `redux/` - Redux Toolkit slices for state management (attendance, schedules, offices, users, menus, dishes, reports)
- `App.js` - Main app component with routing

### Database
- PostgreSQL with Flyway migrations in `src/main/resources/db/migration/`
- Local dev: `docker compose up -d` starts Postgres and a Keycloak pre-loaded from `dockerdev/keycloak/lunatech-realm.json` (also used as the fixture in `KeycloakLoginIntegrationTest`). Test users: `admin.user@lunatech.nl` (admin) and `normal.user@lunatech.nl`, password `lunachef`.

## Configuration

### Backend
- Main config: `src/main/resources/application.conf`
- Local overrides: `src/main/resources/override.conf` (create this file, see CONTRIBUTING.md for required variables)

### Frontend
- Create `frontend/.env.development` and `frontend/.env.production` with:
  ```
  REACT_APP_BASE_URL=http://localhost:8080
  REACT_APP_REALMS_URL=http://localhost:8081/realms/lunatech
  REACT_APP_CLIENT_ID=lunachef-local
  ```

## API Testing

The `requests/` folder contains HTTP client files for testing the API. Requires authentication token setup in `http-client.private.env.json` (see sample file).

## Deployment

Hosted on Clever-Cloud. Deploy by rebasing `production` branch on `master` (fast-forward only):
```bash
git checkout production && git pull && git rebase master && git push
```
