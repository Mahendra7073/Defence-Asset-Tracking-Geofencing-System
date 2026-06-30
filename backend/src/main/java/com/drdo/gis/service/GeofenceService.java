package com.drdo.gis.service;

import com.drdo.gis.dao.AlertDAO;
import com.drdo.gis.dao.GeofenceDAO;
import com.drdo.gis.dao.GeofenceDAO.ZoneHit;
import com.drdo.gis.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * GeofenceService — detects geofence ENTER and EXIT using PostGIS ST_Contains.
 *
 * Maintains in-memory state of which zones each asset is currently inside.
 * On each GPS update:
 *   1. Query all active zones that contain the point (current zones)
 *   2. Compare against previous zones for this asset
 *   3. New zones → generate ENTER alerts (with deduplication)
 *   4. Missing zones → generate EXIT alerts
 *   5. Update state
 */
public class GeofenceService {

    private static final Logger log = LoggerFactory.getLogger(GeofenceService.class);
    private final GeofenceDAO geofenceDAO = new GeofenceDAO();
    private final AlertDAO alertDAO = new AlertDAO();

    /**
     * In-memory tracking: assetId → set of zone IDs the asset is currently inside.
     * ConcurrentHashMap for thread safety across servlet requests.
     */
    private static final ConcurrentHashMap<Integer, Set<Integer>> assetZoneState = new ConcurrentHashMap<>();

    /**
     * Cache zone metadata for EXIT alerts (so we have the name+type even after leaving).
     */
    private static final ConcurrentHashMap<Integer, ZoneHit> zoneCache = new ConcurrentHashMap<>();

    /**
     * Check position against geofence zones. Generates ENTER and EXIT alerts.
     */
    public List<Alert> checkBreach(double lng, double lat, int assetId) {
        List<Alert> generated = new ArrayList<>();

        // 1. Find all zones currently containing this point
        List<ZoneHit> currentHits = geofenceDAO.findContaining(lng, lat);
        Set<Integer> currentZoneIds = new HashSet<>();
        for (ZoneHit hit : currentHits) {
            currentZoneIds.add(hit.id);
            zoneCache.put(hit.id, hit); // Cache zone metadata
        }

        // 2. Get previous zone set for this asset
        Set<Integer> previousZoneIds = assetZoneState.getOrDefault(assetId, Collections.emptySet());

        // 3. ENTER detection: zones in current but NOT in previous
        for (ZoneHit hit : currentHits) {
            if (previousZoneIds.contains(hit.id)) {
                continue; // Already inside, no new alert
            }
            // Only alert for restricted and warning zones
            if (!"restricted".equals(hit.zoneType) && !"warning".equals(hit.zoneType)) {
                continue;
            }
            // Deduplicate: skip if an unacknowledged ENTER alert exists
            if (alertDAO.existsUnackForAssetZone(assetId, hit.id, "ENTER")) {
                continue;
            }

            Alert alert = new Alert();
            alert.setAssetId(assetId);
            alert.setZoneId(hit.id);
            alert.setAlertType("ENTER");
            alert.setLat(lat);
            alert.setLng(lng);
            alert.setSeverity("restricted".equals(hit.zoneType) ? "HIGH" : "MEDIUM");

            int alertId = alertDAO.insert(alert);
            if (alertId > 0) {
                alert.setId(alertId);
                alert.setZoneName(hit.zoneName);
                generated.add(alert);
                log.warn("ENTER DETECTED: asset={} entered zone='{}' (type={}, severity={})",
                         assetId, hit.zoneName, hit.zoneType, alert.getSeverity());
            }
        }

        // 4. EXIT detection: zones in previous but NOT in current
        for (Integer prevZoneId : previousZoneIds) {
            if (currentZoneIds.contains(prevZoneId)) {
                continue; // Still inside, no exit
            }

            ZoneHit cachedZone = zoneCache.get(prevZoneId);
            String zoneName = cachedZone != null ? cachedZone.zoneName : "Zone-" + prevZoneId;
            String zoneType = cachedZone != null ? cachedZone.zoneType : "unknown";

            // Only generate EXIT for restricted and warning zones
            if (!"restricted".equals(zoneType) && !"warning".equals(zoneType)) {
                continue;
            }

            Alert alert = new Alert();
            alert.setAssetId(assetId);
            alert.setZoneId(prevZoneId);
            alert.setAlertType("EXIT");
            alert.setLat(lat);
            alert.setLng(lng);
            alert.setSeverity("LOW"); // EXIT is informational

            int alertId = alertDAO.insert(alert);
            if (alertId > 0) {
                alert.setId(alertId);
                alert.setZoneName(zoneName);
                generated.add(alert);
                log.info("EXIT DETECTED: asset={} left zone='{}' (type={})",
                         assetId, zoneName, zoneType);
            }
        }

        // 5. Update state
        assetZoneState.put(assetId, currentZoneIds);

        return generated;
    }
}
