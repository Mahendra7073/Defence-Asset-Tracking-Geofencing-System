# Database Module - Defence GIS Tracking System

## Overview

This repository contains the PostgreSQL + PostGIS database schema for the Defence GIS Tracking and Geofencing System.

The database is designed to store and manage:

* Defence Assets
* GPS Positions
* Geofence Zones
* Alerts
* Route History
* System Users

---

## Technologies Used

* PostgreSQL 18
* PostGIS 3.6

---

## Database Setup

### Step 1: Create Database

```sql
CREATE DATABASE defence_gis;
```

### Step 2: Connect to Database

Open pgAdmin and connect to:

```text
defence_gis
```

### Step 3: Enable PostGIS

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

Verify installation:

```sql
SELECT PostGIS_Version();
```

### Step 4: Execute SQL Script

Run:

```text
database/defence_gis.sql
```

This script automatically creates all required tables, indexes, and test data.

---

## Database Tables

### users

Stores application users and administrators.

| Column        | Description               |
| ------------- | ------------------------- |
| id            | User ID                   |
| username      | Login username            |
| password_hash | Encrypted password        |
| role          | Admin / Operator / Viewer |

---

### assets

Stores information about tracked assets.

| Column     | Description              |
| ---------- | ------------------------ |
| id         | Asset ID                 |
| asset_name | Asset Name               |
| asset_type | Vehicle / Drone / Person |
| asset_code | Unique Asset Code        |
| status     | Active / Inactive        |

---

### asset_positions

Stores GPS location updates received from assets.

| Column   | Description      |
| -------- | ---------------- |
| id       | Position ID      |
| asset_id | Asset Reference  |
| geom     | Geographic Point |
| speed    | Asset Speed      |
| heading  | Direction        |
| altitude | Height           |

---

### geofence_zones

Stores GIS polygon boundaries used for geofencing.

| Column    | Description       |
| --------- | ----------------- |
| id        | Zone ID           |
| zone_name | Zone Name         |
| zone_type | Restricted / Safe |
| geom      | Polygon Geometry  |

---

### alerts

Stores geofence-related alerts.

| Column     | Description     |
| ---------- | --------------- |
| id         | Alert ID        |
| asset_id   | Asset Reference |
| zone_id    | Zone Reference  |
| alert_type | ENTER / EXIT    |
| severity   | Alert Level     |

---

### track_history

Stores completed asset movement paths.

| Column     | Description         |
| ---------- | ------------------- |
| id         | Track ID            |
| asset_id   | Asset Reference     |
| path       | LineString Geometry |
| distance_m | Total Distance      |
| avg_speed  | Average Speed       |

---

## Spatial Features

The database uses PostGIS geometry types:

### Point Geometry

Used for asset locations.

```sql
GEOMETRY(Point,4326)
```

### Polygon Geometry

Used for geofence zones.

```sql
GEOMETRY(Polygon,4326)
```

### LineString Geometry

Used for route history.

```sql
GEOMETRY(LineString,4326)
```

---

## Spatial Indexes

The following GiST indexes are created for performance optimization:

* idx_positions_geom
* idx_zones_geom
* idx_track_geom

These indexes improve:

* Spatial Queries
* Geofence Detection
* Asset Tracking
* Route Analysis

---

## Sample Data

The database includes sample records for:

### Assets

* Vehicle-01
* Vehicle-02
* Drone-01
* Personnel-01

### Geofence Zones

* Zone-Alpha-Restricted
* Zone-Bravo-Safe

### GPS Position

Sample coordinate:

```text
Latitude  : 26.2389
Longitude : 73.0243
```


