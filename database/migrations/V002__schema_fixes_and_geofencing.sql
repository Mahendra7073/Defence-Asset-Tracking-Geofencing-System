-- ================================================================
-- DEFENCE GIS TRACKING SYSTEM
-- Database Migration: V002 — Schema Fixes & Geofencing Enhancements
-- ================================================================

-- 1. Add 'tank' to asset types and add CHECK constraint
ALTER TABLE assets
    ADD CONSTRAINT chk_asset_type
    CHECK (asset_type IN ('vehicle', 'tank', 'drone', 'person', 'vessel', 'equipment'));

-- 2. Add zone type constraint
ALTER TABLE geofence_zones
    ADD CONSTRAINT chk_zone_type
    CHECK (zone_type IN ('restricted', 'safe', 'warning', 'monitored'));

-- 3. Add updated_at columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE assets ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
ALTER TABLE geofence_zones ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- 4. Add SOS alert type support — extend alert_type for SOS/EMERGENCY alerts
-- The alert_type column is VARCHAR(20) and can already hold 'SOS' values.
-- We add a CHECK constraint to formalize the allowed values:
ALTER TABLE alerts
    ADD CONSTRAINT chk_alert_type
    CHECK (alert_type IN ('ENTER', 'EXIT', 'SOS', 'SPEED', 'OFFLINE'));

-- 5. Add severity constraint
ALTER TABLE alerts
    ADD CONSTRAINT chk_alert_severity
    CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW'));

-- 6. Convert alerts lat/lng to PostGIS geometry
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS geom GEOMETRY(Point, 4326);

-- Migrate existing lat/lng data to geometry column (if any rows exist)
UPDATE alerts
SET geom = ST_SetSRID(ST_Point(lng, lat), 4326)
WHERE lat IS NOT NULL AND lng IS NOT NULL AND geom IS NULL;

-- 7. Create spatial index on alerts geometry
CREATE INDEX IF NOT EXISTS idx_alerts_geom ON alerts USING GIST(geom);

-- 8. Create audit_log table
CREATE TABLE IF NOT EXISTS audit_log (
    id          SERIAL PRIMARY KEY,
    user_id     INT REFERENCES users(id),
    action      VARCHAR(50) NOT NULL,
    entity      VARCHAR(50),
    entity_id   INT,
    details     JSONB,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_log (user_id, created_at DESC);

-- 9. Add a tank asset to seed data
INSERT INTO assets (asset_name, asset_type, asset_code, status)
VALUES ('Tank-01', 'tank', 'TK-001', 'active')
ON CONFLICT (asset_code) DO NOTHING;

-- ================================================================
-- 10. GEOFENCE BREACH DETECTION — Stored Function
-- ================================================================
-- This function checks if a given point (lng, lat) falls inside
-- any active geofence zone. Returns a table of matched zones.

CREATE OR REPLACE FUNCTION check_geofence_breach(
    p_lng DOUBLE PRECISION,
    p_lat DOUBLE PRECISION
)
RETURNS TABLE (
    zone_id     INT,
    zone_name   VARCHAR(100),
    zone_type   VARCHAR(20)
)
AS $$
BEGIN
    RETURN QUERY
    SELECT gz.id, gz.zone_name, gz.zone_type
    FROM geofence_zones gz
    WHERE gz.is_active = TRUE
      AND ST_Contains(gz.geom, ST_SetSRID(ST_Point(p_lng, p_lat), 4326));
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- 11. TRACK HISTORY BUILDER — Stored Function
-- ================================================================
-- Aggregates asset positions into a LineString route for a given
-- asset and time range.

CREATE OR REPLACE FUNCTION build_track_history(
    p_asset_id INT,
    p_start    TIMESTAMP,
    p_end      TIMESTAMP
)
RETURNS TABLE (
    route_path   GEOMETRY,
    point_count  INT,
    distance_m   DOUBLE PRECISION,
    avg_speed    DOUBLE PRECISION
)
AS $$
BEGIN
    RETURN QUERY
    SELECT
        ST_MakeLine(ap.geom ORDER BY ap.recorded_at) AS route_path,
        COUNT(*)::INT AS point_count,
        ST_Length(ST_MakeLine(ap.geom ORDER BY ap.recorded_at)::geography) AS distance_m,
        AVG(ap.speed) AS avg_speed
    FROM asset_positions ap
    WHERE ap.asset_id = p_asset_id
      AND ap.recorded_at BETWEEN p_start AND p_end;
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- 12. AUTO-ALERT TRIGGER — fires on new position INSERT
-- ================================================================
-- When a new GPS position is inserted, this trigger checks if the
-- asset has entered any restricted geofence zone and automatically
-- generates an alert.

CREATE OR REPLACE FUNCTION trigger_geofence_check()
RETURNS TRIGGER AS $$
DECLARE
    breach RECORD;
BEGIN
    FOR breach IN
        SELECT gz.id AS zone_id, gz.zone_name, gz.zone_type
        FROM geofence_zones gz
        WHERE gz.is_active = TRUE
          AND gz.zone_type = 'restricted'
          AND ST_Contains(gz.geom, NEW.geom)
    LOOP
        -- Check if an ENTER alert already exists for this asset+zone (not acknowledged)
        IF NOT EXISTS (
            SELECT 1 FROM alerts
            WHERE asset_id = NEW.asset_id
              AND zone_id = breach.zone_id
              AND alert_type = 'ENTER'
              AND acknowledged = FALSE
        ) THEN
            INSERT INTO alerts (asset_id, zone_id, alert_type, severity, geom)
            VALUES (
                NEW.asset_id,
                breach.zone_id,
                'ENTER',
                CASE breach.zone_type
                    WHEN 'restricted' THEN 'HIGH'
                    WHEN 'warning' THEN 'MEDIUM'
                    ELSE 'LOW'
                END,
                NEW.geom
            );
        END IF;
    END LOOP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS trg_geofence_check ON asset_positions;

CREATE TRIGGER trg_geofence_check
    AFTER INSERT ON asset_positions
    FOR EACH ROW
    EXECUTE FUNCTION trigger_geofence_check();

-- ================================================================
-- VERIFICATION QUERIES
-- ================================================================
SELECT 'Migration V002 applied successfully' AS status;

SELECT routine_name
FROM information_schema.routines
WHERE routine_schema = 'public'
  AND routine_type = 'FUNCTION'
ORDER BY routine_name;

SELECT trigger_name, event_manipulation, event_object_table
FROM information_schema.triggers
WHERE trigger_schema = 'public';
