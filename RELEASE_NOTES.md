# Release Notes — Version 1.2.0 (Stable Production Release)
## Defence Asset Tracking & Geofencing System

We are pleased to announce the release of **v1.2.0** of the **Defence Asset Tracking & Geofencing System**. This release marks the transition of the project into the final production-ready state, with complete UI restoration, bug fixes, containerization improvements, and updated documentation.

---

## 🚀 Key Improvements & Fixes

### 1. UI Restoration (Enterprise Layout)
- Restored original Enterprise GIS layout for **Asset Management**, **Live Tracking**, **Route History**, and **Reports** to match the approved design.
- Re-implemented card layout styling, header/sidebar spacing, statistics counters, table structures, and map alignment.
- Maintained the dark military theme with consistent shadows, borders, padding, and typography.
- Centralized Leaflet map popups to dark-themed styles inside `components.css`.

### 2. Cache Invalidation
- Standardized a version query parameter `?v=20260708` on all static assets (`variables.css`, `base.css`, `layout.css`, `components.css`, `utils.js`, `auth.js`, `map.js`, `tracking.js`, `reports.js`, `alerts.js`, `users.js`, `assets.js`, `geofence.js`) across all HTML files. This prevents browser caching issues when fresh code is deployed to the Tomcat server.

### 3. Sidebar Collapse Functionality
- Restored the collapsible sidebar animation and layout class `.sidebar.collapsed` in `layout.css`.
- The sidebar toggle button (☰) now correctly collapses the navigation panel to an icon-only strip and automatically triggers Leaflet `invalidateSize()` to re-fit the active maps.

### 4. Bug Fixes
- **Missing CSS Variable:** Added `--color-card-bg` as an alias for `--color-card` in `variables.css`.
- **Routes Status Bar:** Added `<div id="route-status">` to `routes.html` so `tracking.js` status updates render correctly in the browser.
- **Enhanced Toast Polling:** Resolved name mismatch where `app.js` tried to override `Alerts.startAutoRefresh` but `alerts.js` only defined `startPolling`. An alias has been added to alerts.js.
- **Dead Code Cleanup:** Removed dead `.asset-item` styles from `components.css` and redundant inline style elements from `geofence.html` and `routes.html`.

---

## 🛠️ Verification Metrics
- **Docker Build:** Compiled successfully via Maven multi-stage package (`BUILD SUCCESS`).
- **QA Tour:** Verified on Chromium browser subagent across all pages: Login, Dashboard, Assets, Live Tracking, Route History, Geofences, Alerts, Reports, Users, and Logout.
- **Console Audit:** 0 JavaScript errors/exceptions across all active workflows.

---

## 👥 Authentication Reference
If credentials are required, use one of the following validated admin accounts:
1. `drdo` / `drdo2026` (Authorized DRDO superuser)
2. `admin` / `admin123` (System Administrator)
3. `mahendra` / `mahendra123` (Administrator)
