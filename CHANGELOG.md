# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-07-08

### Fixed
- **Deployment Audit**: Identified and resolved browser cache mismatch caused by static `?v=20260707` query strings — bumped to `?v=20260708` across all 10 HTML pages (`index.html`, `login.html`, `dashboard.html`, `assets.html`, `tracking.html`, `routes.html`, `reports.html`, `alerts.html`, `geofence.html`, `users.html`).
- **Missing CSS Variable**: Added `--color-card-bg` as an alias for `--color-card` in `variables.css` — was referenced in `routes.html` inline styles but undefined, causing CSS fallback to transparent.
- **Sidebar Toggle Non-functional**: Added `.sidebar.collapsed` CSS ruleset to `layout.css`, restoring the icon-only sidebar collapse mode triggered by the ☰ button.
- **Missing Route Status Element**: Added `<div id="route-status">` to `routes.html` — `tracking.js` calls `Tracking.setStatus()` which writes to this element; it was absent, causing silent JS errors and no playback status feedback.
- **Alert Polling Override Bug**: Added `startAutoRefresh` alias to `alerts.js` — `app.js` referenced `Alerts.startAutoRefresh` but `alerts.js` only exported `startPolling`, so enhanced 5-second toast polling never activated.
- **Dead CSS Removed**: Removed `.asset-item` CSS block from `components.css` — the old live-tracking asset list panel it styled was removed during UI restoration.
- **Dead Inline CSS Removed**: Removed dead `.playback-bar`, `#route-status`, `.live-stat*` inline `<style>` block from `routes.html` (components never used in current layout).
- **Leaflet Popup Inconsistency**: Removed duplicate Leaflet popup dark-theme inline CSS from `geofence.html` and moved it globally to `components.css`, so all map pages (Dashboard, Tracking, Routes, Geofence) now render consistent dark-themed popups.

### Restored (UI)
- Restored original Enterprise Defence GIS UI layout from pre-redesign commit `ac83f22`:
  - `assets.html`: Restored card layout, statistics cards, table layout, and "+ Register Asset" header button
  - `tracking.html`: Restored full-width map without search sidebar or detail card overlay
  - `routes.html`: Restored controls in top card, playback slider in Track Details card, removed legend status panel
  - `reports.html`: Fixed header button alignment and overflow under 768px viewport
  - `layout.css`, `components.css`, `variables.css`: Reverted spacing, border radius, card shadows, and stat card paddings

### Verified
- Docker multi-stage build pipeline: Workspace → Maven Build → WAR → Tomcat → Browser (all checksums match)
- Browser verified via automated subagent: Login, Dashboard, Assets, Tracking, Routes, Geofence, Alerts, Reports, Users

## [1.1.0] - 2026-06-30


### Refactored
- Cleaned up backend project by removing dead/unused classes:
  - Deleted `TrackService.java`
  - Deleted `GeoJsonUtil.java`
- Cleaned up unused DAO methods:
  - Removed `TrackDAO.buildTrack(int, Timestamp, Timestamp)`
  - Removed `GeofenceDAO.findContainingZoneIds(double, double)`
  - Removed `UserDAO.findById(int)`
- Cleaned up servlet fields:
  - Removed unused SLF4J logger instances from `DashboardServlet.java`, `AssetServlet.java`, `AlertServlet.java`, and `GeofenceServlet.java`.
- Cleaned up unused imports across all modified Java files.

### Added
- Added comprehensive repository documentation:
  - `CHANGELOG.md`
  - `PROJECT_STRUCTURE.md`
  - `API_DOCUMENTATION.md`
  - `DATABASE_DOCUMENTATION.md`
  - `DEPLOYMENT.md`
  - `INSTALLATION.md`
  - `TROUBLESHOOTING.md`
  - `SECURITY.md`

## [1.0.1] - 2026-06-29

### Fixed
- **User Management Cleanup:** Removed all references to the `raj` demo account, including database schema, SQL seed scripts, migrations, setup scripts, and documents.
- Seeding configuration now secures only three active ADMIN accounts: `drdo`, `admin`, and `mahendra`, with correct BCrypt hashes.
- **Geofence View Button:** Fixed broken View button action. It now centers/zooms the map onto the zone bounds, applies pulse animations, brightens the active zone's opacity, fades out inactive zones, and displays a rich Dark-themed popup window containing the zone details (Area, Assets inside, and Alerts).
- **Database Schema Validation:** Added migration `V005__add_missing_geofence_fields.sql` to add `status`, `polygon`, and `coordinates` columns to the `geofence_zones` table, with an auto-sync database trigger.

## [1.0.0] - 2026-06-22

### Added
- Initial release of Defence Asset Tracking & Geofencing System.
- Basic live tracking, dashboard counts, alerts, reports, and asset logs.
