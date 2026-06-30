-- ====================================================================
-- V005: Add missing geofence columns (status, polygon, coordinates)
-- Defence Asset Tracking & Geofencing System
-- ====================================================================

-- 1. Add missing columns to geofence_zones
ALTER TABLE geofence_zones ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'active';
ALTER TABLE geofence_zones ADD COLUMN IF NOT EXISTS polygon GEOMETRY(Polygon, 4326);
ALTER TABLE geofence_zones ADD COLUMN IF NOT EXISTS coordinates TEXT;

-- 2. Populate existing rows with data derived from is_active and geom
UPDATE geofence_zones SET status = 'active' WHERE is_active = TRUE;
UPDATE geofence_zones SET status = 'inactive' WHERE is_active = FALSE;
UPDATE geofence_zones SET polygon = geom WHERE polygon IS NULL;
UPDATE geofence_zones SET coordinates = ST_AsText(geom) WHERE coordinates IS NULL;

-- 3. Create a function to automatically synchronize fields on insert or update
CREATE OR REPLACE FUNCTION sync_geofence_fields()
RETURNS TRIGGER AS $$
BEGIN
    -- Synchronize geometry/polygon
    IF NEW.geom IS NOT NULL THEN
        NEW.polygon := NEW.geom;
        NEW.coordinates := ST_AsText(NEW.geom);
    ELSIF NEW.polygon IS NOT NULL THEN
        NEW.geom := NEW.polygon;
        NEW.coordinates := ST_AsText(NEW.polygon);
    END IF;
    
    -- Synchronize is_active and status
    IF NEW.is_active = TRUE THEN
        NEW.status := 'active';
    ELSIF NEW.is_active = FALSE THEN
        NEW.status := 'inactive';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. Create trigger to automatically call synchronization function
DROP TRIGGER IF EXISTS trg_sync_geofence_fields ON geofence_zones;
CREATE TRIGGER trg_sync_geofence_fields
BEFORE INSERT OR UPDATE ON geofence_zones
FOR EACH ROW
EXECUTE FUNCTION sync_geofence_fields();
