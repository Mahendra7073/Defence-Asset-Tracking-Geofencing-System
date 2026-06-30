# Database Documentation - PostgreSQL & PostGIS Schema

This document details the tables, schemas, relations, indices, and triggers configured inside the PostgreSQL instance running PostGIS extensions.

## ER Diagram Concept
```
  [users] 
     ^ (ack_by)
     |
  [alerts] --------> [assets] <-------- [asset_positions]
     | (zone_id)        ^
     v                  | (asset_id)
  [geofence_zones]   [track_history]
```

---

## Database Tables

### 1. `users`
Saves administrator credentials and audit logs details.

* **Primary Key:** `id` (SERIAL)
* **Columns:**
  * `id` (INT): Unique user key.
  * `username` (VARCHAR(50)): NOT NULL, UNIQUE login account identifier.
  * `password_hash` (VARCHAR(255)): NOT NULL, BCrypt-hashed password.
  * `full_name` (VARCHAR(100)): Readable profile name.
  * `role` (VARCHAR(20)): User authorization level ('admin', 'operator', 'viewer').
  * `email` (VARCHAR(100)): UNIQUE contact email.
  * `is_active` (BOOLEAN): DEFAULT TRUE. Account operational status.
  * `last_login` (TIMESTAMP): Recorded login authentication timestamp.
  * `created_at` (TIMESTAMP): Default database row timestamp.

* **Seed Credentials:**
  * `drdo` (`drdo2026`) - hashed using BCrypt cost strength `12`.
  * `admin` (`admin123`) - hashed using BCrypt.
  * `mahendra` (`mahendra123`) - hashed using BCrypt.

---

### 2. `assets`
Contains fleet log registry parameters.

* **Primary Key:** `id` (SERIAL)
* **Columns:**
  * `id` (INT): Unique asset log key.
  * `asset_name` (VARCHAR(100)): Operational vehicle or drone descriptor (e.g. UAV-Echo).
  * `asset_type` (VARCHAR(50)): Type filter (e.g. vehicle, drone, person, tank).
  * `asset_code` (VARCHAR(30)): UNIQUE alphanumeric identifier code (e.g. DH-005).
  * `description` (TEXT): Description summary.
  * `status` (VARCHAR(20)): State ('active', 'maintenance').
  * `created_at` (TIMESTAMP): Default creation timestamp.

---

### 3. `asset_positions`
Raw breadcrumb emissions from field GPS tracking units.

* **Primary Key:** `id` (SERIAL)
* **Foreign Key:** `asset_id` REFERENCES `assets(id)` (ON DELETE CASCADE)
* **Columns:**
  * `id` (INT): Unique position key.
  * `asset_id` (INT): Linked asset ID.
  * `geom` (GEOMETRY(Point, 4326)): GIS Point representing `(longitude, latitude)`.
  * `speed` (FLOAT): Speed in km/h.
  * `heading` (FLOAT): Direct bearing degrees (0 - 360).
  * `altitude` (FLOAT): Height in meters.
  * `accuracy` (FLOAT): Accuracy bounds radius.
  * `recorded_at` (TIMESTAMP): Telemetry emission timestamp.
  * `received_at` (TIMESTAMP): Database ingestion timestamp.

---

### 4. `geofence_zones`
Polygons defining perimeters.

* **Primary Key:** `id` (SERIAL)
* **Foreign Key:** `created_by` REFERENCES `users(id)`
* **Columns:**
  * `id` (INT): Unique perimeter key.
  * `zone_name` (VARCHAR(100)): Perimeter name (e.g. Ammo Depot Zone).
  * `zone_type` (VARCHAR(20)): Alarm type classification ('safe', 'warning', 'restricted').
  * `description` (TEXT): Boundary notes.
  * `color` (VARCHAR(10)): CSS hexadecimal rendering string color.
  * `geom` (GEOMETRY(Polygon, 4326)): PostGIS coordinates boundary structure.
  * `created_by` (INT): Author user reference.
  * `created_at` (TIMESTAMP): Creation date.
  * `is_active` (BOOLEAN): State trigger.
  * `status` (VARCHAR(20)): Sync text parameter ('active' / 'inactive').
  * `polygon` (GEOMETRY(Polygon, 4326)): Geometry mirror column.
  * `coordinates` (TEXT): Plain text translation (WKT) of boundaries.

---

### 5. `alerts`
Warnings logged when asset coordinates violate geofence boundaries.

* **Primary Key:** `id` (SERIAL)
* **Foreign Keys:**
  * `asset_id` REFERENCES `assets(id)`
  * `zone_id` REFERENCES `geofence_zones(id)`
  * `ack_by` REFERENCES `users(id)`
* **Columns:**
  * `id` (INT): Unique warning identifier.
  * `asset_id` (INT): Violating vehicle ID.
  * `zone_id` (INT): Violated polygon perimeter ID.
  * `alert_type` (VARCHAR(20)): Breach category ('ENTER', 'EXIT').
  * `severity` (VARCHAR(20)): Priority level ('HIGH', 'MEDIUM', 'LOW').
  * `lat` (FLOAT): Breach Latitude.
  * `lng` (FLOAT): Breach Longitude.
  * `acknowledged` (BOOLEAN): DEFAULT FALSE.
  * `ack_by` (INT): Operator user reference.
  * `ack_at` (TIMESTAMP): Acknowledged timestamp.
  * `triggered_at` (TIMESTAMP): DB insertion breach detection time.

---

### 6. `track_history`
Aggregated paths for historical route visualization.

* **Primary Key:** `id` (SERIAL)
* **Foreign Key:** `asset_id` REFERENCES `assets(id)`
* **Columns:**
  * `id` (INT): Track identifier key.
  * `asset_id` (INT): Linked asset.
  * `path` (GEOMETRY(LineString, 4326)): Combined LineString path coordinates.
  * `started_at` (TIMESTAMP): Track beginning timestamp.
  * `ended_at` (TIMESTAMP): Track termination timestamp.
  * `distance_m` (FLOAT): Combined path distance (meters).
  * `avg_speed` (FLOAT): Average speed.
  * `point_count` (INT): Count of raw emissions merged.

---

## Spatial Indexes
Spatial index calculations optimize geographic intersects:
```sql
CREATE INDEX idx_positions_geom ON asset_positions USING GIST(geom);
CREATE INDEX idx_zones_geom ON geofence_zones USING GIST(geom);
CREATE INDEX idx_track_geom ON track_history USING GIST(path);
```

## Performance Indexing
```sql
CREATE INDEX idx_positions_asset_time ON asset_positions (asset_id, recorded_at DESC);
CREATE INDEX idx_alerts_unack ON alerts (acknowledged) WHERE acknowledged = FALSE;
CREATE INDEX idx_track_asset ON track_history (asset_id, started_at DESC);
```

---

## Column Synchronization Trigger
Triggers are implemented in `geofence_zones` to keep polygon WKT strings, geometry values, and status strings aligned:

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

CREATE TRIGGER trg_sync_geofence_fields
BEFORE INSERT OR UPDATE ON geofence_zones
FOR EACH ROW EXECUTE FUNCTION sync_geofence_fields();
```
