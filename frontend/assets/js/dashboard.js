/**
 * Defence GIS Tracking System — Dashboard Module
 *
 * Architecture note: The dashboard logic (loadStats, loadRecentAlerts,
 * startAutoRefresh) is defined inline inside dashboard.html and exposed
 * via the window.Dashboard namespace. This file is NOT loaded by dashboard.html
 * and exists as a placeholder for future module extraction.
 *
 * The app.js module may override Dashboard.startAutoRefresh to increase
 * polling speed (default: 30s → 5s on the dashboard page).
 */
