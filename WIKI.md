# Project Wiki & Knowledge Base
## Defence Asset Tracking & Geofencing System

Welcome to the **Defence GIS Wiki**. This document serves as a comprehensive knowledge base for system operators, security administrators, and software developers working on the platform.

---

## 📖 Table of Contents
1. [User Roles & RBAC](#1-user-roles--rbac)
2. [GIS Data Pipeline & GeoServer Integration](#2-gis-data-pipeline--geoserver-integration)
3. [Real-time GPS Simulation Engine](#3-real-time-gps-simulation-engine)
4. [Geofencing Intersect Logic](#4-geofencing-intersect-logic)
5. [Operational Runbooks](#5-operational-runbooks)

---

## 1. User Roles & RBAC

The system utilizes a secure role-based access control (RBAC) gatekeeper implemented as a Java EE `AuthFilter` intercepting all calls to the `/api/*` endpoints.

### Available Roles
- **ADMIN**: Access to user management, fleet configuration, geofence editing, live dashboards, historic route playback, and alert controls.
- **OPERATOR**: Access to dashboards, live tracking, routes playback, reports, and alerts acknowledgement. No user administration or base asset deletion.
- **VIEWER**: Read-only access to dashboards, live asset status, and reports.

### Authorized Credentials
- `drdo` / `drdo2026` (Admin Role)
- `admin` / `admin123` (Admin Role)
- `mahendra` / `mahendra123` (Admin Role)

---

## 2. GIS Data Pipeline & GeoServer Integration

```
  [Raw GPS Telemetry] ────► [Postgres/PostGIS] ◄──── [GeoServer GIS Server]
                                                    │ (WMS/WFS Services)
                                                    ▼
                                            [Leaflet JS Map Client]
```

### Layer Services
1. **WMS (Web Map Service):** Renders heavy spatial layers on the server side as visual map tiles and serves them to the Leaflet client. Used for base map grids and terrain vectors.
2. **WFS (Web Feature Service):** Serves raw coordinate geometry (GeoJSON) directly to Leaflet for client-side rendering. Used for interactive geofence boundaries and asset points.

---

## 3. Real-time GPS Simulation Engine

The background GPS and alert simulation starts automatically when the Tomcat web context is initialized.
- **Service Class:** `GpsSimulatorService`
- **Thread Model:** Spawns a daemon thread executing coordinate updates every 5 seconds.
- **Path Generation:** Assets patrol around Jodhpur sector grids. The simulation calculates headings and speed variations, applying them as coordinate changes in spatial space.
- **Status Simulation:** Randomly generates blackout events, low battery alerts, and simulated vehicle stops.

---

## 4. Geofencing Intersect Logic

Geofencing calculations are offloaded entirely to the database layer for mathematical accuracy and high performance.
- When `PositionServlet` receives a coordinate `(lng, lat)`, it executes a PostGIS point-in-polygon query.
- Uses `ST_Contains(zone.geom, ST_SetSRID(ST_Point(lng, lat), 4326))` to identify active geofence breaches.
- If a breach is found, an entry is inserted into the `alerts` table and real-time clients are notified via WebSocket or high-frequency polling.

---

## 5. Operational Runbooks

### Tailing System Logs
To monitor live database connection pools and simulator telemetry inside the Docker container:
```bash
docker compose logs -f tomcat
```

### Manually Re-seeding Database
If the PostgreSQL tables need a complete flush and reset to seed credentials:
```bash
docker exec -i defence-postgres psql -U postgres -d defence_gis < database/migrations/V003__bcrypt_passwords_and_seed_data.sql
```
