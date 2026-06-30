# Backend Development Roadmap

## Phase 1: Project Foundation (Days 1–3)
- [x] Create Maven project with `pom.xml`
- [x] Configure dependencies (Servlet, PostgreSQL, HikariCP, Gson, BCrypt)
- [x] Create `web.xml` with servlet mappings (commented)
- [x] Create package structure: config, model, dao, servlet, filter, service, util
- [x] Create all model POJO placeholders
- [x] Create all DAO placeholders
- [x] Create all servlet placeholders
- [x] Create `db.properties.example`

## Phase 2: Database Connectivity (Days 4–5)
- [ ] Implement `DatabaseConfig.java` with HikariCP pool
- [ ] Load `db.properties` from classpath
- [ ] Test connection to `defence_gis` database
- [ ] Implement `PasswordUtil.java` with BCrypt

## Phase 3: Authentication (Days 6–8)
- [ ] Implement `UserDAO` (findByUsername, create, updateLastLogin)
- [ ] Implement `LoginServlet` (POST login, POST logout, GET session)
- [ ] Implement `AuthFilter` (session validation, 401 on unauthorized)
- [ ] Uncomment servlet mappings in `web.xml`
- [ ] Test login/logout flow

## Phase 4: Core CRUD APIs (Days 9–14)
- [ ] Implement `AssetDAO` (CRUD + count)
- [ ] Implement `AssetServlet` (GET, POST, PUT, DELETE)
- [ ] Implement `PositionDAO` (insert with PostGIS, findLatest)
- [ ] Implement `PositionServlet` (POST ingest, GET latest)
- [ ] Implement `GeoJsonUtil` (Point, Polygon, LineString → GeoJSON)
- [ ] Implement `GeofenceDAO` (CRUD + findContaining + GeoJSON export)
- [ ] Implement `GeofenceServlet` (GET GeoJSON, POST with WKT)

## Phase 5: Business Logic (Days 15–19)
- [ ] Implement `GeofenceService.checkBreach()` — ST_Contains detection
- [ ] Integrate breach detection into `PositionServlet.doPost()`
- [ ] Implement `AlertDAO` (insert, findUnack, acknowledge)
- [ ] Implement `AlertServlet` (GET list, PUT acknowledge)
- [ ] Implement `TrackService.buildTrack()` — ST_MakeLine aggregation
- [ ] Implement `TrackDAO` (buildTrack, findByAsset, findAsGeoJson)

## Phase 6: Dashboard & Hardening (Days 20–24)
- [ ] Implement `DashboardServlet` (aggregate stats from all DAOs)
- [ ] Add centralized error handling (JSON error responses)
- [ ] Add SLF4J logging to all servlets and services
- [ ] Write JUnit tests for DAOs
- [ ] Write integration tests for servlets
