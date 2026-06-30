# Database Documentation

This document describes the PostgreSQL + PostGIS schema, tables, indices, and triggers for the Defence Asset Tracking & Geofencing System.

## Database Extensions
- **PostGIS:** Required for spatial geometries, calculations, and intersections.
```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

---

## Database Tables

### 1. `users`
Tracks registered accounts with BCrypt-hashed passwords.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | SERIAL | PRIMARY KEY | Unique ID |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | Sign in login username |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| `full_name` | VARCHAR(100) | | Developer / Officer name |
| `role` | VARCHAR(20) | DEFAULT 'operator' | Access levels ('admin', 'operator', 'viewer') |
| `email` | VARCHAR(100) | UNIQUE | Contact email address |
| `is_active` | BOOLEAN | DEFAULT TRUE | Account state |
| `last_login` | TIMESTAMP | | Track login times |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Record creation date |

---

### 2. `assets`
Contains the details of active defence hardware and personnel.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | SERIAL | PRIMARY KEY | Unique asset ID |
| `asset_name` | VARCHAR(100) | NOT NULL | Readable name (e.g. Tank-01) |
| `asset_type` | VARCHAR(50) | NOT NULL | Type filter (e.g. vehicle, drone, tank, person) |
| `asset_code` | VARCHAR(30) | UNIQUE | Unique identifier (e.g. VH-001) |
| `description` | TEXT | | Operational summary |
| `status` | VARCHAR(20) | DEFAULT 'active' | Operational status ('active', 'maintenance') |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Registration date |

---

### 3. `asset_positions`
Saves historical spatial breadcrumbs emitted by assets.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | SERIAL | PRIMARY KEY | Unique coordinate identifier |
| `asset_id` | INT | NOT NULL, REFERENCES assets(id) | Linked asset |
| `geom` | GEOMETRY(Point, 4326) | NOT NULL | PostGIS Spatial Point (Lng, Lat) |
| `speed` | FLOAT | | Speed in km/h |
| `heading` | FLOAT | | Direction bearing in degrees (0 - 360) |
| `altitude` | FLOAT | | Altitude in meters |
| `accuracy` | FLOAT | | GPS sensor precision radius |
| `recorded_at` | TIMESTAMP | DEFAULT NOW() | Emitted timestamp from GPS device |
| `received_at` | TIMESTAMP | DEFAULT NOW() | Database ingestion timestamp |

---

### 4. `geofence_zones`
Contains polygons denoting safe, warning, or restricted coordinates.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | SERIAL | PRIMARY KEY | Unique geofence ID |
| `zone_name` | VARCHAR(100) | NOT NULL | Polygon identifier (e.g. Command Sector) |
| `zone_type` | VARCHAR(20) | DEFAULT 'restricted' | Zone severity ('safe', 'warning', 'restricted') |
| `description` | TEXT | | Brief overview of boundary purpose |
| `color` | VARCHAR(10) | DEFAULT '#FF0000' | Hex visual representation on Leaflet |
| `geom` | GEOMETRY(Polygon, 4326) | NOT NULL | PostGIS polygon boundaries |
| `created_by` | INT | REFERENCES users(id) | User who drew the zone |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Date zone created |
| `is_active` | BOOLEAN | DEFAULT TRUE | Zone operational state |
| `status` | VARCHAR(20) | DEFAULT 'active' | Text state representation |
| `polygon` | GEOMETRY(Polygon, 4326) | | Geometry mirror column |
| `coordinates` | TEXT | | Text representation (WKT) of boundaries |

---

### 5. `alerts`
Logs boundary breach warnings (ENTER / EXIT events).

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | SERIAL | PRIMARY KEY | Warning log ID |
| `asset_id` | INT | NOT NULL, REFERENCES assets(id) | Violating hardware |
| `zone_id` | INT | NOT NULL, REFERENCES geofence_zones(id) | Violates geofence |
| `alert_type` | VARCHAR(20) | NOT NULL | Event classification ('ENTER', 'EXIT') |
| `severity` | VARCHAR(20) | DEFAULT 'HIGH' | Priority level ('HIGH', 'MEDIUM', 'LOW') |
| `lat` | FLOAT | | Violation Latitude |
| `lng` | FLOAT | | Violation Longitude |
| `acknowledged` | BOOLEAN | DEFAULT FALSE | Operator approval state |
| `ack_by` | INT | REFERENCES users(id) | Operator who acknowledged warning |
| `ack_at` | TIMESTAMP | | Time of acknowledgment |
| `triggered_at` | TIMESTAMP | DEFAULT NOW() | Ingest breach detection time |

---

### 6. `track_history`
Aggregated tracks summary table.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| `id` | SERIAL | PRIMARY KEY | Track history identifier |
| `asset_id` | INT | NOT NULL, REFERENCES assets(id) | Linked asset |
| `path` | GEOMETRY(LineString, 4326) | | Aggregated PostGIS line coordinates |
| `started_at` | TIMESTAMP | | Track start time |
| `ended_at` | TIMESTAMP | | Track end time |
| `distance_m` | FLOAT | | Total distance traveled (meters) |
| `avg_speed` | FLOAT | | Average speed recorded |
| `point_count` | INT | | Counts of raw GPS updates |

---

## Spatial Indexes
To ensure fast execution of spatial containment and mapping intersections:
```sql
CREATE INDEX idx_positions_geom ON asset_positions USING GIST(geom);
CREATE INDEX idx_zones_geom ON geofence_zones USING GIST(geom);
CREATE INDEX idx_track_geom ON track_history USING GIST(path);
```

## Performance Indexes
```sql
CREATE INDEX idx_positions_asset_time ON asset_positions (asset_id, recorded_at DESC);
CREATE INDEX idx_alerts_asset ON alerts (asset_id, triggered_at DESC);
CREATE INDEX idx_alerts_zone ON alerts (zone_id, triggered_at DESC);
CREATE INDEX idx_alerts_unack ON alerts (acknowledged) WHERE acknowledged = FALSE;
CREATE INDEX idx_track_asset ON track_history (asset_id, started_at DESC);
```

---

## Database Triggers

### 1. `trg_geofence_check` (Geofence Breach Ingestion)
- **Table:** `asset_positions`
- **Timing:** `AFTER INSERT`
- **Functionality:** Calls `trigger_geofence_check` stored procedure which delegates to the GIS tracking system or executes spatial queries (the actual tracking logic is backed by Java service filters calling `checkBreach` on coordinates insertion).

### 2. `trg_sync_geofence_fields` (Geofence Columns Synchronization)
- **Table:** `geofence_zones`
- **Timing:** `BEFORE INSERT OR UPDATE`
- **Functionality:** Copies `geom` to `polygon`, translates `geom` to WKT text string inside `coordinates`, and matches `is_active` boolean to `status` text ('active' / 'inactive').
```sql
CREATE OR REPLACE FUNCTION sync_geofence_fields()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.geom IS NOT NULL THEN
        NEW.polygon := NEW.geom;
        NEW.coordinates := ST_AsText(NEW.geom);
    ELSIF NEW.polygon IS NOT NULL THEN
        NEW.geom := NEW.polygon;
        NEW.coordinates := ST_AsText(NEW.polygon);
    END IF;
    
    IF NEW.is_active = TRUE THEN
        NEW.status := 'active';
    ELSIF NEW.is_active = FALSE THEN
        NEW.status := 'inactive';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```
