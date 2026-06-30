-- ====================================================================
-- V003: BCrypt Password + Additional Seed Data
-- Defence Asset Tracking & Geofencing System
-- ====================================================================

-- Update user 'admin' with BCrypt hash of 'admin123'
-- Generated with: BCrypt.hashpw("admin123", BCrypt.gensalt(12))
INSERT INTO users (username, password_hash, full_name, role, email, is_active)
VALUES ('admin', '$2a$12$Uv73dagSOPfPB.Uu1kvpWexApfBcfyeLVK1/bd9HQUZhunccEIjuG',
        'System Administrator', 'ADMIN', 'admin@drdo.gov.in', TRUE)
ON CONFLICT (username) DO UPDATE SET 
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role;

-- Add required DRDO login user with BCrypt hash of 'drdo2026'
-- Generated with: BCrypt.hashpw("drdo2026", BCrypt.gensalt(12))
INSERT INTO users (username, password_hash, full_name, role, email, is_active)
VALUES ('drdo', '$2a$12$TNwkEqPRxfxpgzc2dGRNeuqtw3ldSINQtj16H78ZUyaBHiJ4H7ORm',
        'DRDO Administrator', 'ADMIN', 'drdo@drdo.gov.in', TRUE)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    email = EXCLUDED.email,
    is_active = EXCLUDED.is_active;

-- Add required Mahendra login user with BCrypt hash of 'mahendra123'
-- Generated with: BCrypt.hashpw("mahendra123", BCrypt.gensalt(12))
INSERT INTO users (username, password_hash, full_name, role, email, is_active)
VALUES ('mahendra', '$2a$12$RIpCVqq2PZ2ervNKof6Nqupugg2fxhLTQ4iMtLOFQDUuoxXk1Nd.a',
        'Mahendra Administrator', 'ADMIN', 'mahendra@drdo.gov.in', TRUE)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    email = EXCLUDED.email,
    is_active = EXCLUDED.is_active;

-- Add more seed positions for demo (Vehicle-01 moving near Jodhpur)
INSERT INTO asset_positions (asset_id, altitude, speed, heading, geom) VALUES
(1, 250.0, 45.0, 90.0, ST_SetSRID(ST_Point(73.0243, 26.2389), 4326)),
(1, 250.0, 48.0, 85.0, ST_SetSRID(ST_Point(73.0250, 26.2395), 4326)),
(1, 251.0, 50.0, 80.0, ST_SetSRID(ST_Point(73.0260, 26.2402), 4326)),
(2, 248.0, 35.0, 180.0, ST_SetSRID(ST_Point(73.0300, 26.2400), 4326)),
(3, 300.0, 80.0, 270.0, ST_SetSRID(ST_Point(73.0200, 26.2450), 4326));

-- Verify
SELECT username, role, is_active FROM users;
SELECT a.asset_name, COUNT(p.id) as positions FROM assets a LEFT JOIN asset_positions p ON a.id = p.asset_id GROUP BY a.asset_name;
