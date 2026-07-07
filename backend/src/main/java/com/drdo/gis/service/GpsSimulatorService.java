package com.drdo.gis.service;

import com.drdo.gis.config.DatabaseConfig;
import com.drdo.gis.dao.AlertDAO;
import com.drdo.gis.dao.PositionDAO;
import com.drdo.gis.model.Alert;
import com.drdo.gis.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GpsSimulatorService — generates realistic GPS movement and event-driven alerts.
 *
 * Runs on a fixed schedule (every 30 seconds by default).
 * Updates positions, checks geofences, and triggers alerts for actual events.
 */
public class GpsSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(GpsSimulatorService.class);

    // ── Operational bounding box: ~10 km around Jodhpur base ──────────────────
    private static final double LAT_MIN = 26.18;
    private static final double LAT_MAX = 26.32;
    private static final double LNG_MIN = 72.96;
    private static final double LNG_MAX = 73.10;

    // ── In-memory state per asset ──────────────────────────────────────────────
    private static final class AssetState {
        int    id;
        String type;
        double lat;
        double lng;
        double heading;   // degrees 0–360
        double speed;     // km/h
        int    patrolStep;// for drone patrol pattern

        AssetState(int id, String type, double lat, double lng) {
            this.id        = id;
            this.type      = type;
            this.lat       = lat;
            this.lng       = lng;
            this.heading   = new Random().nextDouble() * 360;
            this.speed     = initialSpeed(type);
            this.patrolStep= 0;
        }

        static double initialSpeed(String type) {
            switch (type) {
                case "drone":   return 80 + Math.random() * 40;
                case "vehicle": return 30 + Math.random() * 30;
                case "tank":    return 10 + Math.random() * 20;
                case "person":  return 3  + Math.random() * 4;
                default:        return 0;
            }
        }
    }

    private final Map<Integer, AssetState> states = new ConcurrentHashMap<>();
    private final PositionDAO  positionDAO  = new PositionDAO();
    private final GeofenceService geofence  = new GeofenceService();
    private final AlertDAO     alertDAO     = new AlertDAO();
    private final Random       rng          = new Random();
    private boolean            initialised  = false;
    private int                fallbackZoneId = -1;

    // ── Event-Driven Simulation States ─────────────────────────────────────────
    private static final Map<Integer, Integer> droneBattery = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> offlineTicks = new ConcurrentHashMap<>();
    private static final Set<Integer> speedingAssets = ConcurrentHashMap.newKeySet();
    private static final Set<Integer> unauthorizedAssets = ConcurrentHashMap.newKeySet();

    // Base coordinate for recharging drones
    private static final double BASE_LAT = 26.2389;
    private static final double BASE_LNG = 73.0243;

    // ── Drone patrol waypoints (box around base) ────────────────────────────
    private static final double[][] DRONE_PATROL = {
        {26.260, 73.010}, {26.260, 73.080},
        {26.200, 73.080}, {26.200, 73.010}
    };

    /** Called by the scheduler every tick. */
    public void tick() {
        ensureInitialised();
        for (AssetState s : states.values()) {
            if ("radar_station".equalsIgnoreCase(s.type) || "radar".equalsIgnoreCase(s.type)) {
                // Radar stations are fixed, but they can trigger communication/online/offline events!
                checkOfflineEvent(s);
                continue; 
            }

            // Check if asset is currently offline
            if (offlineTicks.containsKey(s.id)) {
                int remaining = offlineTicks.get(s.id);
                if (remaining <= 1) {
                    offlineTicks.remove(s.id);
                    log.info("Communication RESTORED for asset id={}", s.id);
                } else {
                    offlineTicks.put(s.id, remaining - 1);
                    // Skip GPS coordinate updates in DB while offline (no telemetry)
                    continue;
                }
            }

            // Randomly trigger offline event (communication lost)
            if (rng.nextDouble() < 0.008) {
                int ticks = 5 + rng.nextInt(6); // offline for 5-10 ticks
                offlineTicks.put(s.id, ticks);
                triggerEventAlert(s.id, s.lat, s.lng, "OFFLINE", "HIGH");
                log.warn("Communication LOST for asset id={} (offline for {} ticks)", s.id, ticks);
                continue;
            }

            // Normal movement
            updatePosition(s);
            insertPosition(s);

            // Check event triggers
            checkSpeedLimit(s);
            checkDroneBattery(s);
            checkSosEvent(s);
            checkUnauthorizedMove(s);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Initialisation — load latest DB positions
    // ────────────────────────────────────────────────────────────────────────

    private synchronized void ensureInitialised() {
        if (initialised) return;
        try (Connection c  = DatabaseConfig.getConnection();
             Statement  st = c.createStatement()) {

            // Get a fallback zone ID to satisfy Foreign Key constraints on alerts
            try (ResultSet rs = st.executeQuery("SELECT id FROM geofence_zones LIMIT 1")) {
                if (rs.next()) fallbackZoneId = rs.getInt("id");
            }

            try (ResultSet rs = st.executeQuery(
                 "SELECT DISTINCT ON (a.id) a.id, a.asset_type, " +
                 "  COALESCE(ST_Y(ap.geom), 26.2389) AS lat, " +
                 "  COALESCE(ST_X(ap.geom), 73.0243) AS lng " +
                 "FROM assets a " +
                 "LEFT JOIN asset_positions ap ON ap.asset_id = a.id " +
                 "WHERE a.status = 'active' " +
                 "ORDER BY a.id, ap.recorded_at DESC NULLS LAST")) {

                while (rs.next()) {
                    int    id   = rs.getInt("id");
                    String type = rs.getString("asset_type");
                    double lat  = rs.getDouble("lat");
                    double lng  = rs.getDouble("lng");
                    states.put(id, new AssetState(id, type, lat, lng));
                    
                    if ("drone".equalsIgnoreCase(type)) {
                        droneBattery.put(id, 100);
                    }
                    log.info("GPS Simulator loaded asset id={} type={} at [{},{}]", id, type, lat, lng);
                }
            }
            initialised = true;
            log.info("GPS Simulator initialised with {} assets", states.size());
        } catch (SQLException e) {
            log.error("GPS Simulator init failed: {}", e.getMessage(), e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Event-Driven Alert Evaluation
    // ────────────────────────────────────────────────────────────────────────

    private void checkSpeedLimit(AssetState s) {
        double limit = 55.0; // vehicle default
        if ("drone".equalsIgnoreCase(s.type)) limit = 110.0;
        else if ("tank".equalsIgnoreCase(s.type)) limit = 25.0;
        else if ("person".equalsIgnoreCase(s.type)) limit = 6.0;

        if (s.speed > limit) {
            if (!speedingAssets.contains(s.id)) {
                speedingAssets.add(s.id);
                triggerEventAlert(s.id, s.lat, s.lng, "SPEED", "HIGH");
                log.warn("SPEED BREACH: Asset id={} speed={}/limit={} km/h", s.id, s.speed, limit);
            }
        } else {
            speedingAssets.remove(s.id);
        }
    }

    private void checkDroneBattery(AssetState s) {
        if (!"drone".equalsIgnoreCase(s.type)) return;

        // Decrease battery by 1% per tick
        int battery = droneBattery.getOrDefault(s.id, 100);
        
        // Recharge if close to base
        double dlat = s.lat - BASE_LAT;
        double dlng = s.lng - BASE_LNG;
        if (Math.sqrt(dlat*dlat + dlng*dlng) < 0.01) {
            battery = 100;
            log.info("Drone id={} recharged at Jodhpur base.", s.id);
        } else {
            battery = Math.max(0, battery - 1);
        }
        droneBattery.put(s.id, battery);

        if (battery <= 15) {
            // Trigger BATTERY alert
            if (!alertDAO.existsUnackForAssetZone(s.id, getZoneId(s), "BATTERY")) {
                triggerEventAlert(s.id, s.lat, s.lng, "BATTERY", "MEDIUM");
                log.warn("LOW BATTERY: Drone id={} battery={}%", s.id, battery);
            }
        }
    }

    private void checkSosEvent(AssetState s) {
        // 0.4% chance of random emergency / SOS event
        if (rng.nextDouble() < 0.004) {
            if (!alertDAO.existsUnackForAssetZone(s.id, getZoneId(s), "SOS")) {
                triggerEventAlert(s.id, s.lat, s.lng, "SOS", "CRITICAL");
                log.warn("SOS EVENT: Asset id={} triggered emergency beacon", s.id);
            }
        }
    }

    private void checkUnauthorizedMove(AssetState s) {
        // If an asset moves into a specific restricted zone (e.g. Zone-Echo-Radar-Restricted)
        // or a simulated night patrol, trigger UNAUTHORIZED alert type.
        // We can simulate an unauthorized state if personnel (PR-001/PR-002) is inside warning/restricted zones
        // and rng triggers unauthorized movement, or if asset id=13 (Tank-02 reserve heavy armor) moves at all!
        if (s.id == 13 && s.speed > 5) {
            if (!unauthorizedAssets.contains(s.id)) {
                unauthorizedAssets.add(s.id);
                triggerEventAlert(s.id, s.lat, s.lng, "UNAUTHORIZED", "HIGH");
                log.warn("UNAUTHORIZED MOVEMENT: Reserve armour Tank-02 started moving without clearance.");
            }
        } else if (s.id != 13) {
            unauthorizedAssets.remove(s.id);
        }
    }

    private void checkOfflineEvent(AssetState s) {
        // Radar stations can go offline randomly (communication lost)
        if (offlineTicks.containsKey(s.id)) {
            int remaining = offlineTicks.get(s.id);
            if (remaining <= 1) {
                offlineTicks.remove(s.id);
                log.info("Radar Station id={} is back online", s.id);
            } else {
                offlineTicks.put(s.id, remaining - 1);
            }
        } else {
            if (rng.nextDouble() < 0.004) {
                int ticks = 10 + rng.nextInt(10); // radar stays offline longer
                offlineTicks.put(s.id, ticks);
                triggerEventAlert(s.id, s.lat, s.lng, "OFFLINE", "HIGH");
                log.warn("RADAR OFFLINE: Radar Station id={} lost contact", s.id);
            }
        }
    }

    private void triggerEventAlert(int assetId, double lat, double lng, String alertType, String severity) {
        try {
            Alert alert = new Alert();
            alert.setAssetId(assetId);
            alert.setZoneId(getZoneId(states.get(assetId)));
            alert.setAlertType(alertType);
            alert.setSeverity(severity);
            alert.setLat(lat);
            alert.setLng(lng);
            alertDAO.insert(alert);
        } catch (Exception e) {
            log.error("Failed to insert event alert: {}", e.getMessage());
        }
    }

    private int getZoneId(AssetState s) {
        if (s == null) return fallbackZoneId;
        // Search if asset is currently tracked in a zone, else fallback
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT zone_id FROM alerts WHERE asset_id = ? ORDER BY triggered_at DESC LIMIT 1")) {
            ps.setInt(1, s.id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("zone_id");
            }
        } catch (SQLException e) {
            // silent fallback
        }
        return fallbackZoneId > 0 ? fallbackZoneId : 1;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Position update logic per asset type
    // ────────────────────────────────────────────────────────────────────────

    private void updatePosition(AssetState s) {
        switch (s.type.toLowerCase()) {
            case "vehicle": moveVehicle(s); break;
            case "drone":   moveDrone(s);   break;
            case "person":  movePerson(s);  break;
            case "tank":    moveTank(s);    break;
            default:        moveVehicle(s); break;
        }
        // Clamp to operational bounding box
        s.lat = clamp(s.lat, LAT_MIN, LAT_MAX);
        s.lng = clamp(s.lng, LNG_MIN, LNG_MAX);
        // Bounce heading if hitting boundary
        if (s.lat <= LAT_MIN || s.lat >= LAT_MAX) s.heading = 360 - s.heading;
        if (s.lng <= LNG_MIN || s.lng >= LNG_MAX) s.heading = (180 + s.heading) % 360;
    }

    private void moveVehicle(AssetState s) {
        s.heading = (s.heading + (rng.nextDouble() - 0.5) * 20 + 360) % 360;
        s.speed = clamp(s.speed + (rng.nextDouble() - 0.5) * 8, 20, 60);
        double step = 0.0004 + rng.nextDouble() * 0.0002;
        s.lat += step * Math.cos(Math.toRadians(s.heading));
        s.lng += step * Math.sin(Math.toRadians(s.heading));
    }

    private void moveDrone(AssetState s) {
        double[] wp = DRONE_PATROL[s.patrolStep % DRONE_PATROL.length];
        double dlat = wp[0] - s.lat;
        double dlng = wp[1] - s.lng;
        double dist = Math.sqrt(dlat * dlat + dlng * dlng);

        if (dist < 0.002) {
            s.patrolStep++;
        }

        double step = 0.0015 + rng.nextDouble() * 0.0005;
        if (dist > 0.001) {
            s.lat += (dlat / dist) * step + (rng.nextDouble() - 0.5) * 0.0003;
            s.lng += (dlng / dist) * step + (rng.nextDouble() - 0.5) * 0.0003;
        } else {
            s.lat += (rng.nextDouble() - 0.5) * 0.001;
            s.lng += (rng.nextDouble() - 0.5) * 0.001;
        }
        s.heading = (Math.toDegrees(Math.atan2(dlng, dlat)) + 360) % 360;
        s.speed   = 80 + rng.nextDouble() * 40;
    }

    private void movePerson(AssetState s) {
        s.heading = (s.heading + (rng.nextDouble() - 0.5) * 60 + 360) % 360;
        s.speed   = clamp(s.speed + (rng.nextDouble() - 0.5) * 2, 2, 7);
        double step = 0.00003 + rng.nextDouble() * 0.00003;
        s.lat += step * Math.cos(Math.toRadians(s.heading));
        s.lng += step * Math.sin(Math.toRadians(s.heading));
    }

    private void moveTank(AssetState s) {
        s.heading = (s.heading + (rng.nextDouble() - 0.5) * 8 + 360) % 360;
        s.speed   = clamp(s.speed + (rng.nextDouble() - 0.5) * 4, 10, 30);
        double step = 0.0002 + rng.nextDouble() * 0.0002;
        s.lat += step * Math.cos(Math.toRadians(s.heading));
        s.lng += step * Math.sin(Math.toRadians(s.heading));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Write position to DB and check geofence
    // ────────────────────────────────────────────────────────────────────────

    private void insertPosition(AssetState s) {
        try {
            Position p = new Position();
            p.setAssetId(s.id);
            p.setLatitude(s.lat);
            p.setLongitude(s.lng);
            p.setSpeed(s.speed);
            p.setHeading(s.heading);
            p.setAltitude(s.type.equals("drone") ? 80 + rng.nextDouble() * 120 : 320);
            p.setAccuracy(3.0 + rng.nextDouble() * 7.0);

            int posId = positionDAO.insert(p);
            if (posId > 0) {
                geofence.checkBreach(s.lng, s.lat, s.id);
            }
        } catch (Exception e) {
            log.error("GPS tick insert failed for asset {}: {}", s.id, e.getMessage());
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
