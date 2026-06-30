package com.drdo.gis.dao;

import com.drdo.gis.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GeofenceDAO — queries geofence_zones with PostGIS spatial functions.
 */
public class GeofenceDAO {

    private static final Logger log = LoggerFactory.getLogger(GeofenceDAO.class);



    /**
     * Richer version: returns zone_id, zone_name, zone_type for breach detection.
     */
    public static class ZoneHit {
        public int id;
        public String zoneName;
        public String zoneType;
    }

    public List<ZoneHit> findContaining(double lng, double lat) {
        String sql = "SELECT id, zone_name, zone_type FROM geofence_zones "
                   + "WHERE is_active = TRUE AND ST_Contains(geom, ST_SetSRID(ST_Point(?, ?), 4326))";
        List<ZoneHit> hits = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, lng);
            ps.setDouble(2, lat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ZoneHit h = new ZoneHit();
                    h.id = rs.getInt("id");
                    h.zoneName = rs.getString("zone_name");
                    h.zoneType = rs.getString("zone_type");
                    hits.add(h);
                }
            }
        } catch (SQLException e) {
            log.error("findContaining failed: {}", e.getMessage(), e);
        }
        return hits;
    }

    public String findAllAsGeoJson() {
        String sql = "SELECT json_build_object("
                   + "  'type', 'FeatureCollection',"
                   + "  'features', COALESCE(json_agg("
                   + "    json_build_object("
                   + "      'type', 'Feature',"
                   + "      'geometry', ST_AsGeoJSON(geom)::json,"
                   + "      'properties', json_build_object("
                   + "        'id', id, 'zoneName', zone_name, 'zoneType', zone_type,"
                   + "        'color', color, 'isActive', is_active"
                   + "      )"
                   + "    )"
                   + "  ), '[]'::json)"
                   + ") AS geojson FROM geofence_zones WHERE is_active = TRUE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("geojson");
        } catch (SQLException e) {
            log.error("findAllAsGeoJson failed: {}", e.getMessage(), e);
        }
        return "{\"type\":\"FeatureCollection\",\"features\":[]}";
    }

    public int countActive() {
        String sql = "SELECT COUNT(*) FROM geofence_zones WHERE is_active = TRUE";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("countActive failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    public String findDetailsAsJson(int id) {
        String sql = "SELECT json_build_object("
                   + "  'status', 'success',"
                   + "  'data', json_build_object("
                   + "    'id', gz.id,"
                   + "    'zoneName', gz.zone_name,"
                   + "    'zoneType', gz.zone_type,"
                   + "    'status', gz.status,"
                   + "    'createdAt', gz.created_at,"
                   + "    'coordinates', gz.coordinates,"
                   + "    'area', ROUND(ST_Area(gz.geom::geography)::numeric, 2),"
                   + "    'assetsInsideCount', ("
                   + "        SELECT COUNT(*) FROM ("
                   + "            SELECT DISTINCT ON (asset_id) asset_id, geom "
                   + "            FROM asset_positions "
                   + "            ORDER BY asset_id, recorded_at DESC"
                   + "        ) latest_pos "
                   + "        WHERE ST_Contains(gz.geom, latest_pos.geom)"
                   + "    ),"
                   + "    'alertsCount', ("
                   + "        SELECT COUNT(*) "
                   + "        FROM alerts "
                   + "        WHERE zone_id = gz.id AND acknowledged = FALSE"
                   + "    )"
                   + "  )"
                   + ") AS details FROM geofence_zones gz WHERE gz.id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("details");
            }
        } catch (SQLException e) {
            log.error("findDetailsAsJson failed: {}", e.getMessage(), e);
        }
        return null;
    }
}
