# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
