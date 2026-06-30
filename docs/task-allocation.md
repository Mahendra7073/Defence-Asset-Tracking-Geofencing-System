# Task Allocation — Defence GIS Tracking System

## Team Roles

| Role                 | Focus Area                                       |
| -------------------- | ------------------------------------------------ |
| **Project Lead**     | Architecture, integration, reviews, documentation|
| **Frontend Dev**     | HTML/CSS/JS, Leaflet, UI pages, user experience  |
| **Backend Dev**      | Java servlets, DAOs, services, REST API endpoints|
| **GIS Developer**    | GeoServer, WMS/WFS, SLD styles, spatial queries  |
| **Database Engineer**| Schema, migrations, stored procs, performance    |
| **Tester**           | Test cases, QA, integration testing, bug reports |

---

## Sprint 1: Foundation (Week 1)

### Project Lead
- [x] Create `.gitignore`
- [x] Create `LICENSE`
- [x] Create `CONTRIBUTING.md`
- [x] Create `architecture.md`
- [x] Create `api-specification.md`
- [x] Create `deployment-guide.md`
- [x] Create `branch-strategy.md`
- [ ] Set up GitHub Issues & Milestones
- [ ] Set up branch protection rules on `main` and `develop`

### Frontend Developer
- [x] Create frontend folder structure
- [x] Build CSS design system (variables, base, layout, components)
- [x] Create login page
- [x] Create dashboard page with sidebar + stat cards
- [x] Create tracking page with Leaflet container
- [x] Create geofence page with map + table
- [x] Create alerts page with table
- [x] Create JS stubs (app.js, auth.js, map.js, utils.js)

### Backend Developer
- [x] Create Maven project with `pom.xml`
- [x] Create `web.xml` with servlet mappings
- [x] Create all model POJOs
- [x] Create all DAO placeholders
- [x] Create all servlet placeholders
- [x] Create filter, service, utility placeholders
- [x] Create `db.properties.example`

### GIS Developer
- [x] Create `geoserver/` directory with README
- [x] Create expanded `setup_guide.md`
- [x] Create `geoserver/styles/` directory
- [ ] Install GeoServer locally
- [ ] Configure workspace and PostGIS store
- [ ] Publish 3 layers

### Database Engineer
- [ ] Apply schema fixes (updated_at columns, CHECK constraints)
- [ ] Convert `alerts.lat/lng` to PostGIS geometry
- [ ] Create `audit_log` table
- [ ] Write comprehensive seed/demo data
- [ ] Set up Flyway migration framework

### Tester
- [x] Create `test-cases/` directory
- [ ] Write test cases for login functionality
- [ ] Write test cases for asset CRUD
- [ ] Write test cases for geofence breach detection

---

## Sprint 2: Core Implementation (Week 2)

### Frontend Developer
- [ ] Initialize Leaflet map with OpenStreetMap tiles
- [ ] Render asset markers from API
- [ ] Render geofence polygons from API
- [ ] Connect dashboard KPI cards to API

### Backend Developer
- [ ] Implement `DatabaseConfig.java` with HikariCP
- [ ] Implement `UserDAO` + `LoginServlet`
- [ ] Implement `AuthFilter`
- [ ] Implement `AssetDAO` + `AssetServlet`
- [ ] Implement `PositionDAO` + `PositionServlet`

### GIS Developer
- [ ] Create SLD styles for all 3 layers
- [ ] Verify WMS/WFS endpoints
- [ ] Help integrate WMS tiles into Leaflet
- [ ] Write GPS simulator script

### Database Engineer
- [ ] Create geofence breach detection stored procedure
- [ ] Create track history builder stored procedure
- [ ] Performance test with 10K+ position rows

---

## Sprint 3: Features (Week 3)

### Frontend Developer
- [ ] Asset management CRUD page
- [ ] Geofence draw/edit with Leaflet.Draw
- [ ] Alert monitor with acknowledge buttons
- [ ] Route history replay

### Backend Developer
- [ ] Implement `GeofenceDAO` + `GeofenceServlet`
- [ ] Implement `GeofenceService.checkBreach()`
- [ ] Implement `AlertDAO` + `AlertServlet`
- [ ] Implement `TrackService` + `DashboardServlet`

### GIS Developer
- [ ] Optimize spatial queries
- [ ] GeoServer REST API automation script

---

## Sprint 4: Integration & Polish (Week 4)

### All Team Members
- [ ] End-to-end integration testing
- [ ] Bug fixes and UI polish
- [ ] Responsive design verification
- [ ] Deployment guide verification
- [ ] Demo preparation
- [ ] Final presentation
