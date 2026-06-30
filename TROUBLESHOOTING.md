# Troubleshooting Guide

This guide describes solutions for common errors encountered during the installation, build, or runtime of the Defence GIS Tracking System.

## 1. Database Connection Failures
**Error:** `FATAL: password authentication failed for user "postgres"` or `Connection refused`.
* **Solutions:**
  1. Open `backend/src/main/resources/db.properties` and verify `db.password` matches your local PostgreSQL instance password.
  2. Ensure the PostgreSQL service is active:
     - On Windows: Run `services.msc` and verify `postgresql` service is running.
     - On Linux: Run `sudo systemctl status postgresql`.
  3. Verify the database port (default: `5432`).

---

## 2. PostGIS Extension Missing
**Error:** `ERROR: extension "postgis" is not available` or `type "geometry" does not exist`.
* **Solutions:**
  1. You must install the PostGIS spatial bundle. Verify the installation instructions in `INSTALLATION.md`.
  2. Ensure you have run:
     ```sql
     CREATE EXTENSION IF NOT EXISTS postgis;
     ```
     in the active `defence_gis` database.

---

## 3. Tomcat Port Conflicts (8080)
**Error:** `Address already in use` or Tomcat fails to startup.
* **Solutions:**
  1. Identify which process is holding port 8080:
     - On Windows: `netstat -ano | findstr 8080`
     - On Linux: `sudo lsof -i :8080`
  2. Kill the process, or change Tomcat's HTTP port:
     - Open `%CATALINA_HOME%\conf\server.xml`.
     - Find `<Connector port="8080" protocol="HTTP/1.1" ... />`.
     - Change `port="8080"` to `port="8081"` (or another free port).
     - Restart Tomcat.

---

## 4. Map Tiles Failing to Load
**Error:** Leaflet map displays grid lines instead of standard streets map.
* **Solutions:**
  1. Leaflet fetches tile coordinates online from OpenStreetMap. Verify your developer machine is connected to the internet.
  2. Check for browser console errors: press `F12` and inspect network / console requests to `tile.openstreetmap.org`.

---

## 5. Geoserver WMS Layer 404
**Error:** Map loads tiles but doesn't overlays asset positions or path traces.
* **Solutions:**
  1. GeoServer is an optional service for rendering heavy maps layers. If you did not install GeoServer, this is expected; the native Leaflet GeoJSON layer is active by default and does not require GeoServer.
  2. If using GeoServer, verify that GeoServer is active and that your WMS layer name matches the definition in `map.js` (default: `http://localhost:8080/geoserver/defence/wms`).
