-- DEFENCE GIS TRACKING SYSTEM
-- Database: defence_gis
-- PostgreSQL + PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;


CREATE TABLE users (
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100),
    role          VARCHAR(20)  DEFAULT 'operator',  -- 'admin','operator','viewer'
    email         VARCHAR(100) UNIQUE,
    is_active     BOOLEAN      DEFAULT TRUE,
    last_login    TIMESTAMP,
    created_at    TIMESTAMP    DEFAULT NOW()
);
CREATE TABLE assets (
    id          SERIAL PRIMARY KEY,
    asset_name  VARCHAR(100) NOT NULL,
    asset_type  VARCHAR(50)  NOT NULL,
    asset_code  VARCHAR(30)  UNIQUE,
    description TEXT,
    status      VARCHAR(20)  DEFAULT 'active',
    created_at  TIMESTAMP    DEFAULT NOW()
);


CREATE TABLE asset_positions (
    id          SERIAL PRIMARY KEY,
    asset_id    INT     NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    geom        GEOMETRY(Point, 4326) NOT NULL,
    speed       FLOAT,
    heading     FLOAT,
    altitude    FLOAT,
    accuracy    FLOAT,
    recorded_at TIMESTAMP DEFAULT NOW(),
    received_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE geofence_zones (
    id          SERIAL PRIMARY KEY,
    zone_name   VARCHAR(100) NOT NULL,
    zone_type   VARCHAR(20)  DEFAULT 'restricted',
    description TEXT,
    color       VARCHAR(10)  DEFAULT '#FF0000',
    geom        GEOMETRY(Polygon, 4326) NOT NULL,
    created_by  INT REFERENCES users(id),     
    created_at  TIMESTAMP DEFAULT NOW(),
    is_active   BOOLEAN DEFAULT TRUE
);

CREATE TABLE alerts (
    id           SERIAL PRIMARY KEY,
    asset_id     INT     NOT NULL REFERENCES assets(id)         ON DELETE CASCADE,
    zone_id      INT     NOT NULL REFERENCES geofence_zones(id) ON DELETE CASCADE,
    alert_type   VARCHAR(20) NOT NULL,
    severity     VARCHAR(20) DEFAULT 'HIGH',
    lat          FLOAT,
    lng          FLOAT,
    acknowledged BOOLEAN   DEFAULT FALSE,
    ack_by       INT REFERENCES users(id),
    ack_at       TIMESTAMP,
    triggered_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE track_history (
    id          SERIAL PRIMARY KEY,
    asset_id    INT     NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    path        GEOMETRY(LineString, 4326),
    started_at  TIMESTAMP,
    ended_at    TIMESTAMP,
    distance_m  FLOAT,
    avg_speed   FLOAT,
    point_count INT
);


CREATE INDEX idx_positions_geom      ON asset_positions USING GIST(geom);
CREATE INDEX idx_zones_geom          ON geofence_zones  USING GIST(geom);
CREATE INDEX idx_positions_asset_time ON asset_positions (asset_id, recorded_at DESC);
CREATE INDEX idx_alerts_asset        ON alerts (asset_id, triggered_at DESC);
CREATE INDEX idx_alerts_zone         ON alerts (zone_id,  triggered_at DESC);
CREATE INDEX idx_alerts_unack        ON alerts (acknowledged) WHERE acknowledged = FALSE;
CREATE INDEX idx_track_asset         ON track_history (asset_id, started_at DESC);
CREATE INDEX idx_track_geom          ON track_history USING GIST(path);

-- test data
INSERT INTO users (username, password_hash, full_name, role, email)
VALUES ('raj', '1234_hash', 'System Admin', 'admin', 'admin@defence.gis');

INSERT INTO assets (asset_name, asset_type, asset_code, status) VALUES
    ('Vehicle-01',   'vehicle', 'VH-001', 'active'),
    ('Vehicle-02',   'vehicle', 'VH-002', 'active'),
    ('Drone-01',     'drone',   'DR-001', 'active'),
    ('Personnel-01', 'person',  'PR-001', 'active');

INSERT INTO asset_positions (asset_id, geom, speed, heading, altitude)VALUES (
    1,
    ST_SetSRID(ST_Point(73.0243, 26.2389), 4326),
    45.0, 90.0, 320.0
);

INSERT INTO geofence_zones (zone_name, zone_type, color, geom, created_by)
VALUES (
    'Zone-Alpha-Restricted', 'restricted', '#FF0000',
    ST_SetSRID(ST_GeomFromText('POLYGON((
        73.010 26.230,
        73.050 26.230,
        73.050 26.260,
        73.010 26.260,
        73.010 26.230
    ))'), 4326),
    1   
);

INSERT INTO geofence_zones (zone_name, zone_type, color, geom, created_by)
VALUES (
    'Zone-Bravo-Safe', 'safe', '#00AA00',
    ST_SetSRID(ST_GeomFromText('POLYGON((
        73.060 26.220,
        73.100 26.220,
        73.100 26.250,
        73.060 26.250,
        73.060 26.220
    ))'), 4326),
    1
);

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

SELECT indexname
FROM pg_indexes
WHERE tablename IN (
    'asset_positions', 'geofence_zones',
    'alerts', 'track_history'
)
ORDER BY indexname;

SELECT
asset_id,
ST_X(geom) AS longitude,
ST_Y(geom) AS latitude
FROM asset_positions;
