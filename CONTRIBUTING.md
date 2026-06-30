# Contributing to Defence GIS Tracking System

Thank you for contributing to this DRDO internship project!

---

## Team Structure

| Role                | Responsibility                                          |
| ------------------- | ------------------------------------------------------- |
| **Project Lead**    | Architecture, code reviews, integration, documentation  |
| **Frontend Dev**    | HTML/CSS/JS, Leaflet integration, UI pages              |
| **Backend Dev**     | Java servlets, DAOs, services, API endpoints            |
| **GIS Developer**   | GeoServer config, spatial queries, SLD styles, WMS/WFS  |
| **Database Engineer** | Schema management, migrations, stored procedures, tuning |
| **Tester**          | Test case writing, manual/automated testing, QA         |

---

## Development Workflow

### 1. Pick a Task
- Check `docs/task-allocation.md` for your assigned tasks.
- Create a branch from `develop` using the naming convention below.

### 2. Branch Naming
```
feature/<module>-<short-description>
bugfix/<module>-<short-description>
hotfix/<description>

Examples:
  feature/backend-login-servlet
  feature/frontend-dashboard-cards
  bugfix/gis-wms-layer-404
```

### 3. Commit Messages
Use clear, descriptive commit messages:
```
[MODULE] Short description

Examples:
  [Backend] Implement LoginServlet with BCrypt auth
  [Frontend] Add Leaflet map to tracking page
  [Database] Add audit_log table migration
  [GIS] Create geofence_zones SLD style
  [Docs] Update API specification with alerts endpoint
```

### 4. Pull Requests
- All code must be submitted via Pull Request.
- At least 1 reviewer must approve before merge.
- PR title should match commit convention: `[Module] Description`
- Link any related GitHub Issues.

### 5. Code Standards

#### Java (Backend)
- Follow Java naming conventions (camelCase methods, PascalCase classes).
- Use `PreparedStatement` for all SQL queries (prevent SQL injection).
- Never hardcode database credentials — use `db.properties`.
- Add Javadoc comments to all public methods.
- Use SLF4J for logging, never `System.out.println`.

#### HTML/CSS/JavaScript (Frontend)
- Use semantic HTML5 elements.
- Follow the CSS design system in `frontend/assets/css/variables.css`.
- Use `const`/`let`, never `var`.
- Add JSDoc comments to all JavaScript functions.
- Use unique `id` attributes on all interactive elements.

#### SQL (Database)
- Use lowercase for table and column names.
- Use `snake_case` naming convention.
- Always specify `SRID 4326` for PostGIS geometry columns.
- Add indexes for frequently queried columns.

---

## Getting Started

```bash
# Clone the repository
git clone https://github.com/Mahendra7073/Defence-Asset-Tracking-Geofencing-System.git

# Follow the deployment guide
# See: docs/deployment-guide.md
```

---

## Reporting Issues

Use GitHub Issues with these labels:
- `bug` — Something broken
- `feature` — New feature request
- `enhancement` — Improvement to existing feature
- `documentation` — Doc updates needed
- `question` — Need clarification
