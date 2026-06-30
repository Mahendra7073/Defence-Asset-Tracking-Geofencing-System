# System Architecture — Defence GIS Tracking System

## Overview

The Defence Asset Tracking and Geofencing System is a web-based GIS application designed for real-time tracking of defence assets (vehicles, drones, personnel), geofence zone management, and automated breach alert monitoring.

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐   │
│  │  Dashboard   │  │  Live Map   │  │  Geofence   │  │   Alerts     │   │
│  │  (HTML/JS)   │  │ (Leaflet)   │  │  (Draw)     │  │  (Monitor)   │   │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬───────┘   │
│         │                │                │                 │            │
│         └────────────────┴────────────────┴─────────────────┘            │
│                                   │                                      │
│                          jQuery AJAX / Fetch                             │
└──────────────────────────────────┬───────────────────────────────────────┘
                                   │ HTTP REST (JSON)
                                   ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         APPLICATION LAYER                                │
│                       (Apache Tomcat 9/10)                               │
│                                                                          │
│  ┌──────────┐  ┌──────────┐  ┌────────────┐  ┌────────────────────┐     │
│  │ AuthFilter│→│ Servlets  │→│  Services   │→│       DAOs         │     │
│  │ (Session) │  │ (REST)   │  │ (Business) │  │ (PreparedStatement)│     │
│  └──────────┘  └──────────┘  └────────────┘  └─────────┬──────────┘     │
│                                                         │                │
│                                                    HikariCP              │
└────────────────────────────────────────────────────────┬─────────────────┘
                                                         │ JDBC
                                                         ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                           DATA LAYER                                     │
│                                                                          │
│  ┌─────────────────────────┐        ┌──────────────────────────┐        │
│  │  PostgreSQL 18          │        │  GeoServer 2.25          │        │
│  │  + PostGIS 3.6          │◄──────►│  (WMS / WFS)             │        │
│  │                         │PostGIS │                          │        │
│  │  Tables:                │ Store  │  Layers:                 │        │
│  │  - users                │        │  - asset_positions       │        │
│  │  - assets               │        │  - geofence_zones        │        │
│  │  - asset_positions      │        │  - track_history         │        │
│  │  - geofence_zones       │        │                          │        │
│  │  - alerts               │        │  Services:               │        │
│  │  - track_history        │        │  - WMS (tile rendering)  │        │
│  │                         │        │  - WFS (feature queries) │        │
│  └─────────────────────────┘        └──────────────────────────┘        │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Layer       | Technology                          | Version   |
| ----------- | ----------------------------------- | --------- |
| Frontend    | HTML5, CSS3, JavaScript, jQuery     | —         |
| Mapping     | Leaflet.js                          | 1.9.4     |
| Backend     | Java Servlets, JSP                  | Java 17   |
| Server      | Apache Tomcat                       | 9.x/10.x  |
| Database    | PostgreSQL + PostGIS                | 18 + 3.6  |
| GIS Server  | GeoServer (WMS/WFS)                 | 2.25.x    |
| Build Tool  | Apache Maven                        | 3.9.x     |

---

## Data Flow

### 1. GPS Position Ingestion
```
GPS Device → POST /api/positions → PositionServlet
    → PositionDAO.insert() → asset_positions table
    → GeofenceService.checkBreach() → ST_Contains query
    → If breach detected → AlertDAO.insert() → alerts table
```

### 2. Geofence Breach Detection
```
PositionServlet calls GeofenceService.checkBreach(lng, lat, assetId):
    1. SELECT zones WHERE ST_Contains(zone.geom, Point(lng, lat))
    2. Compare with last known zone state for asset
    3. ENTER event: asset was NOT in zone, now IS in zone
    4. EXIT event: asset WAS in zone, now NOT in zone
    5. Insert alert with severity based on zone type
```

### 3. Dashboard Data
```
Frontend → GET /api/dashboard → DashboardServlet
    → Aggregates: AssetDAO.count(), AlertDAO.countUnack(),
      GeofenceDAO.countActive() → JSON response
```

### 4. Map Rendering
```
Leaflet Map:
    - Base tiles: OpenStreetMap
    - Asset markers: GET /api/positions/latest → GeoJSON
    - Geofence zones: GeoServer WFS → GeoJSON OR GET /api/geofences
    - Route history: GET /api/tracks → GeoJSON LineString
    - WMS overlay: GeoServer WMS tile layer (optional)
```

---

## Security Model

| Mechanism       | Implementation                                   |
| --------------- | ------------------------------------------------ |
| Authentication  | Session-based (HttpSession)                      |
| Password Storage| BCrypt hash (cost factor 12)                     |
| Route Protection| AuthFilter on /api/* (excludes /api/auth/login)  |
| RBAC            | admin, operator, viewer roles                    |
| Session Timeout | 30 minutes                                       |

---

## Deployment Topology

```
┌─────────────────────────────────────────────┐
│              Production Server               │
│                                              │
│  Port 8080: Apache Tomcat (DefenceGIS.war)   │
│  Port 8081: GeoServer                        │
│  Port 5432: PostgreSQL + PostGIS             │
│                                              │
│  Reverse Proxy: Nginx (port 80/443)          │
└─────────────────────────────────────────────┘
```
