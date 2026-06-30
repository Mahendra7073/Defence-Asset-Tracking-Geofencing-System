# Contributing Guidelines - Defence GIS Tracking System

Thank you for contributing to this project! This document outlines coding conventions, branch management strategies, and review procedures to ensure code quality is preserved.

---

## Code of Conduct
All developers are expected to maintain professional communication, write high-quality code, and participate constructively in code reviews.

---

## Development Setup

1. **System Setup:** Ensure JDK 17, Maven 3.8+, and PostgreSQL with PostGIS are active.
2. **Clone Project:**
   ```bash
   git clone https://github.com/Mahendra7073/Defence-Asset-Tracking-Geofencing-System.git
   ```
3. **Database Configuration:** Setup `db.properties` using the example template:
   ```bash
   cp backend/src/main/resources/db.properties.example backend/src/main/resources/db.properties
   ```
4. **Compile Build:** Verify compilation passes before staging work:
   ```bash
   cd backend
   mvn clean package
   ```

---

## Git Workflow & Branch Naming

All changes must be developed in dedicated feature branches branched off `develop` (or `main` if direct feature merge):

```
Branch Formats:
  feature/<module>-<short-description>
  bugfix/<module>-<short-description>
  hotfix/<description>

Examples:
  feature/backend-login-servlet
  feature/frontend-geofence-animation
  bugfix/database-gist-index
```

---

## Commit Message Format

Commit messages must indicate the modified component:

```
Format:
  [Component] Descriptive summary of changes

Examples:
  [Backend] Remove unused class TrackService and optimize imports
  [Database] Add trg_sync_geofence_fields trigger to geofence_zones
  [Frontend] Highlight active geofence and pulse boundaries on view action
  [Docs] Add REST API endpoints specifications and sequence diagrams
```

---

## Coding Standards

### 1. Java Backend
- Use CamelCase naming for variables and methods.
- Free database connections using `try-with-resources`.
- Never execute raw string concatenations inside database queries (use `PreparedStatement` boundaries).
- Use SLF4J logger instances for system auditing; `System.out.println` is forbidden.

### 2. Frontend Client
- Maintain dark-theme palette styling tokens from `frontend/assets/css/variables.css`.
- Ensure Leaflet mapping controllers utilize parameters efficiently to minimize frame rate drop.
- Provide descriptive `id` fields for interactive elements.

### 3. SQL & Migrations
- Use snake_case format for tables and fields.
- Coordinate systems must enforce SRID 4326 for GIS projections.
- Add performance indices for columns frequently matching filter criteria.

---

## Pull Request Guidelines
- All PRs must pass automated build pipelines (`mvn clean package`).
- Ensure no untracked temp files, IDE files, or binaries are committed.
- Request review from at least one repository maintainer.
